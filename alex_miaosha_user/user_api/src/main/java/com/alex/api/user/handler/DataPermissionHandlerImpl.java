package com.alex.api.user.handler;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermissionScope;
import com.alex.api.user.rbac.RbacRoleCodes;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DataPermissionHandlerImpl implements DataPermissionHandler {

    private final UserUtils userUtils;

    /**
     * RBAC-BE-SCOPE-002: 机构子孙节点查询，默认 {@link OrgSubtreeLookup#NOOP}（不扩展范围）。
     */
    private final OrgSubtreeLookup orgSubtreeLookup;

    /**
     * 兼容既有调用方（product/oss/finance boot 的 MybatisPlusConfig 仍以单参构造），
     * 未接入机构子树能力时保持原有「仅本机构」语义。
     */
    public DataPermissionHandlerImpl(UserUtils userUtils) {
        this(userUtils, OrgSubtreeLookup.NOOP);
    }

    public DataPermissionHandlerImpl(UserUtils userUtils, OrgSubtreeLookup orgSubtreeLookup) {
        this.userUtils = userUtils;
        this.orgSubtreeLookup = orgSubtreeLookup == null ? OrgSubtreeLookup.NOOP : orgSubtreeLookup;
    }

    /**
     * 线程本地变量，防止在获取当前登录用户时触发其他 SQL 查询导致无限递归
     */
    private static final ThreadLocal<Boolean> IS_PROCESSING = ThreadLocal.withInitial(() -> false);

    /**
     * 缓存 MappedStatementId 对应的 DataPermission 注解，避免高频反射产生的性能损耗
     */
    private static final Map<String, Optional<DataPermission>> ANNOTATION_CACHE = new ConcurrentHashMap<>();

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        if (IS_PROCESSING.get()) {
            return where;
        }
        try {
            IS_PROCESSING.set(true);
            return doGetSqlSegment(where, mappedStatementId);
        } finally {
            IS_PROCESSING.remove();
        }
    }

    private Expression doGetSqlSegment(Expression where, String mappedStatementId) {
        DataPermission annotation = getDataPermission(mappedStatementId);
        if (annotation == null) {
            return where;
        }

        TUserVo loginUser = null;
        if (userUtils != null) {
            try {
                loginUser = userUtils.getLoginUser();
            } catch (Exception e) {
                log.error("获取登录用户异常", e);
            }
        }
        if (loginUser == null) {
            return where;
        }

        RoleFlags roleFlags = resolveRoleFlags(loginUser);
        if (roleFlags.isSuper()) {
            return getSuperWhere(where);
        }

        DataPermissionScope scope = annotation.scope();
        if (scope == DataPermissionScope.ORG_ID) {
            return getOrgIdWhere(where, loginUser, annotation, roleFlags.isAdmin());
        }
        if (scope == DataPermissionScope.ORG_SHARED) {
            return getOrgSharedWhere(where, loginUser, annotation, roleFlags.isAdmin());
        }

        if (roleFlags.isAdmin()) {
            return getAdminWhere(where, loginUser, annotation);
        }
        if (roleFlags.isUser() || roleFlags.isEmpty()) {
            return getUserWhere(where, loginUser, annotation);
        }
        return getDefaultWhere(where, loginUser, annotation);
    }

    private DataPermission getDataPermission(String mappedStatementId) {
        if (mappedStatementId == null) {
            return null;
        }
        return ANNOTATION_CACHE.computeIfAbsent(mappedStatementId, msId -> {
            try {
                int lastDot = msId.lastIndexOf(".");
                if (lastDot == -1) {
                    return Optional.empty();
                }
                String className = msId.substring(0, lastDot);
                String methodName = msId.substring(lastDot + 1);
                Class<?> clazz = Class.forName(className);
                for (Method method : clazz.getDeclaredMethods()) {
                    if (Objects.equals(method.getName(), methodName)) {
                        DataPermission annotation = method.getAnnotation(DataPermission.class);
                        if (annotation != null) {
                            return Optional.of(annotation);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                log.warn("未找到对应的 Mapper 类，mappedStatementId: {}", msId);
            } catch (Exception e) {
                log.error("解析数据权限注解失败，mappedStatementId: {}", msId, e);
            }
            return Optional.empty();
        }).orElse(null);
    }

    private Expression getUserWhere(Expression where, TUserVo loginUser, DataPermission annotation) {
        return appendFieldEquals(where, resolveTargetTable(annotation), annotation.field(), loginUser.getId());
    }

    /**
     * ORG_ID scope: filter by login user's org id on {@link DataPermission#field()}.
     * admin: selfOrgId ∪ 全部子孙机构；user: 仅本机构。
     */
    private Expression getOrgIdWhere(Expression where, TUserVo loginUser, DataPermission annotation, boolean isAdmin) {
        Long selfOrgId = resolveLoginOrgId(loginUser);
        if (selfOrgId == null) {
            EqualsTo impossible = new EqualsTo();
            impossible.setLeftExpression(new Column(new Table(resolveTargetTable(annotation)), annotation.field()));
            impossible.setRightExpression(new LongValue(-1L));
            return where == null ? impossible : new AndExpression(where, impossible);
        }
        Expression condition = isAdmin
                ? buildIdListExpression(resolveTargetTable(annotation), annotation.field(), buildAdminOrgScopeIds(selfOrgId))
                : buildIdListExpression(resolveTargetTable(annotation), annotation.field(), Collections.singletonList(selfOrgId));
        return where == null ? condition : new AndExpression(where, condition);
    }

    private Expression getOrgSharedWhere(Expression where, TUserVo loginUser, DataPermission annotation, boolean isAdmin) {
        Long orgId = resolveLoginOrgId(loginUser);
        if (orgId == null) {
            log.warn("家庭组共享模式未解析到机构，降级为个人数据权限：userId={}", loginUser.getId());
            return getUserWhere(where, loginUser, annotation);
        }
        if (StringUtils.hasText(annotation.orgField())) {
            List<Long> orgIds = isAdmin ? buildAdminOrgScopeIds(orgId) : Collections.singletonList(orgId);
            Expression condition = buildIdListExpression(resolveTargetTable(annotation), annotation.orgField(), orgIds);
            return where == null ? condition : new AndExpression(where, condition);
        }
        return appendOrgMembersIn(where, annotation, orgId);
    }

    private String resolveTargetTable(DataPermission annotation) {
        if (annotation != null && StringUtils.hasText(annotation.alias())) {
            return annotation.alias().trim();
        }
        return annotation != null ? annotation.table() : "";
    }

    private List<Long> buildAdminOrgScopeIds(Long selfOrgId) {
        Set<Long> scopeIds = new LinkedHashSet<>();
        scopeIds.add(selfOrgId);
        try {
            List<Long> descendants = orgSubtreeLookup.findDescendantOrgIds(selfOrgId);
            if (descendants != null) {
                for (Long id : descendants) {
                    if (id != null) {
                        scopeIds.add(id);
                    }
                }
            }
        } catch (Exception e) {
            log.error("查询机构子孙节点异常，降级为仅本机构：orgId={}", selfOrgId, e);
        }
        return new ArrayList<>(scopeIds);
    }

    private Expression buildIdListExpression(String tableName, String fieldName, List<Long> ids) {
        Column column = new Column(new Table(tableName), fieldName);
        if (ids.size() == 1) {
            EqualsTo eq = new EqualsTo();
            eq.setLeftExpression(column);
            eq.setRightExpression(new LongValue(ids.get(0)));
            return eq;
        }
        InExpression in = new InExpression();
        in.setLeftExpression(column);
        List<Expression> values = new ArrayList<>();
        for (Long id : ids) {
            values.add(new LongValue(id));
        }
        in.setRightExpression(new ParenthesedExpressionList<>(values));
        return in;
    }

    private Expression getAdminWhere(Expression where, TUserVo loginUser, DataPermission annotation) {
        Long orgId = resolveLoginOrgId(loginUser);
        if (orgId == null) {
            log.warn("管理员未关联所属机构，降级为个人数据权限：userId={}", loginUser.getId());
            return getUserWhere(where, loginUser, annotation);
        }
        return appendOrgMembersIn(where, annotation, orgId);
    }

    private Expression appendOrgMembersIn(Expression where, DataPermission annotation, Long orgId) {
        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(new Column(new Table(resolveTargetTable(annotation)), annotation.field()));

        PlainSelect plainSelect = new PlainSelect();
        plainSelect.addSelectItems(new SelectItem<>(new Column("user_id")));

        Table table = new Table("alex_user.t_org_user_info");
        plainSelect.setFromItem(table);

        EqualsTo orgIdCondition = new EqualsTo();
        orgIdCondition.setLeftExpression(new Column("org_id"));
        orgIdCondition.setRightExpression(new LongValue(orgId));

        EqualsTo statusCondition = new EqualsTo();
        statusCondition.setLeftExpression(new Column("status"));
        statusCondition.setRightExpression(new StringValue("1"));

        EqualsTo deleteCondition = new EqualsTo();
        deleteCondition.setLeftExpression(new Column("is_delete"));
        deleteCondition.setRightExpression(new LongValue(0L));

        Expression whereCondition = new AndExpression(
                new AndExpression(orgIdCondition, statusCondition),
                deleteCondition
        );
        plainSelect.setWhere(whereCondition);

        ParenthesedSelect subSelect = new ParenthesedSelect();
        subSelect.setSelect(plainSelect);
        inExpression.setRightExpression(subSelect);

        return where == null ? inExpression : new AndExpression(where, inExpression);
    }

    private Expression appendFieldEquals(Expression where, String tableName, String fieldName, Long value) {
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column(new Table(tableName), fieldName));
        equalsTo.setRightExpression(new LongValue(value));
        return where == null ? equalsTo : new AndExpression(where, equalsTo);
    }

    private Expression getSuperWhere(Expression where) {
        return where;
    }

    private Expression getDefaultWhere(Expression where, TUserVo loginUser, DataPermission annotation) {
        return appendFieldEquals(where, resolveTargetTable(annotation), annotation.field(), loginUser.getId());
    }

    private Long resolveLoginOrgId(TUserVo loginUser) {
        if (loginUser == null) {
            return null;
        }
        if (loginUser.getOrgInfoVo() != null && loginUser.getOrgInfoVo().getId() != null) {
            return loginUser.getOrgInfoVo().getId();
        }
        return loginUser.getOrgId();
    }

    private RoleFlags resolveRoleFlags(TUserVo loginUser) {
        List<RoleInfoVo> roleList = loginUser.getRoleInfoVoList();
        List<String> roleCodes = new ArrayList<>();
        if (roleList != null) {
            roleList.stream()
                    .filter(role -> role != null && role.getRoleCode() != null)
                    .map(RoleInfoVo::getRoleCode)
                    .forEach(roleCodes::add);
        }
        boolean isSuper = false;
        boolean isAdmin = false;
        boolean isUser = false;
        for (String code : roleCodes) {
            if (RbacRoleCodes.SUPER.equals(code)) {
                isSuper = true;
            } else if (RbacRoleCodes.ADMIN.equals(code)) {
                isAdmin = true;
            } else if (RbacRoleCodes.USER.equals(code)) {
                isUser = true;
            }
        }
        return new RoleFlags(isSuper, isAdmin, isUser, roleCodes.isEmpty());
    }

    private record RoleFlags(boolean isSuper, boolean isAdmin, boolean isUser, boolean isEmpty) {
    }
}
