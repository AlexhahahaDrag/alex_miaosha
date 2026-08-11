package com.alex.api.user.handler;

import com.alex.api.user.annotation.DataPermission;
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
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
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

        // 判断当前用户角色决定权限
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

        if (isSuper) {
            return getSuperWhere(where);
        }
        if (annotation.scope() == DataPermission.Scope.ORG_ID) {
            return getOrgIdWhere(where, loginUser, annotation, isAdmin);
        }
        if (isAdmin) {
            return getAdminWhere(where, loginUser, annotation);
        }
        if (isUser || roleCodes.isEmpty()) {
            return getUserWhere(where, loginUser, annotation);
        }
        return getDefaultWhere(where, loginUser, annotation);
    }

    /**
     * 获取指定 mappedStatementId 的 DataPermission 注解（带高性能 ConcurrentHashMap 缓存）
     */
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
        EqualsTo useEqualsTo = new EqualsTo();
        useEqualsTo.setLeftExpression(new Column(new Table(annotation.table()), annotation.field()));
        useEqualsTo.setRightExpression(new LongValue(loginUser.getId()));
        return where == null ? useEqualsTo : new AndExpression(where, useEqualsTo);
    }

    /**
     * ORG_ID scope: filter by login user's org id.
     * - admin: selfOrgId ∪ 全部子孙机构 id（RBAC-BE-SCOPE-002），无子孙时退化为 equals。
     * - user/default: 仍仅本机构 equals。
     * Missing org → impossible equals so no rows leak.
     */
    private Expression getOrgIdWhere(Expression where, TUserVo loginUser, DataPermission annotation, boolean isAdmin) {
        if (loginUser == null || loginUser.getOrgInfoVo() == null || loginUser.getOrgInfoVo().getId() == null) {
            EqualsTo impossible = new EqualsTo();
            impossible.setLeftExpression(new Column(new Table(annotation.table()), annotation.field()));
            impossible.setRightExpression(new LongValue(-1L));
            return where == null ? impossible : new AndExpression(where, impossible);
        }
        Long selfOrgId = loginUser.getOrgInfoVo().getId();
        Expression condition = isAdmin
                ? buildOrgIdExpression(annotation, buildAdminOrgScopeIds(selfOrgId))
                : buildOrgIdExpression(annotation, Collections.singletonList(selfOrgId));
        return where == null ? condition : new AndExpression(where, condition);
    }

    /**
     * 一次性查出 selfOrgId 的全部子孙机构 id（去重，保序），失败时降级为仅本机构。
     */
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

    /**
     * 单个 id 退化为 EqualsTo，多个 id 使用 InExpression（保持与既有 JSQLParser 风格一致）。
     */
    private Expression buildOrgIdExpression(DataPermission annotation, List<Long> orgIds) {
        Column column = new Column(new Table(annotation.table()), annotation.field());
        if (orgIds.size() == 1) {
            EqualsTo eq = new EqualsTo();
            eq.setLeftExpression(column);
            eq.setRightExpression(new LongValue(orgIds.get(0)));
            return eq;
        }
        InExpression in = new InExpression();
        in.setLeftExpression(column);
        List<Expression> values = new ArrayList<>();
        for (Long id : orgIds) {
            values.add(new LongValue(id));
        }
        ExpressionList expressionList = new ExpressionList();
        expressionList.setExpressions(values);
        in.setRightItemsList(expressionList);
        return in;
    }

    private Expression getAdminWhere(Expression where, TUserVo loginUser, DataPermission annotation) {
        if (loginUser == null || loginUser.getOrgInfoVo() == null || loginUser.getOrgInfoVo().getId() == null) {
            log.warn("管理员未关联所属机构，降级为个人数据权限：userId={}", loginUser != null ? loginUser.getId() : "unknown");
            return getUserWhere(where, loginUser, annotation);
        }

        InExpression useEqualsTo = new InExpression();
        useEqualsTo.setLeftExpression(new Column(new Table(annotation.table()), annotation.field()));
        // 构建子查询
        SubSelect subSelect = new SubSelect();
        PlainSelect plainSelect = new PlainSelect();

        // 构建子查询中的 SELECT 部分
        SelectExpressionItem selectItem = new SelectExpressionItem();
        selectItem.setExpression(new Column("user_id"));
        plainSelect.addSelectItems(selectItem);

        // 构建子查询中的 FROM 部分
        Table table = new Table("alex_user.t_org_user_info");
        plainSelect.setFromItem(table);

        // WHERE: org_id = ? AND status = '1' AND is_delete = 0
        EqualsTo orgIdCondition = new EqualsTo();
        orgIdCondition.setLeftExpression(new Column("org_id"));
        orgIdCondition.setRightExpression(new LongValue(loginUser.getOrgInfoVo().getId()));

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

        // 将 PlainSelect 对象设置为 SubSelect 的 SelectBody
        subSelect.setSelectBody(plainSelect);

        // 设置右表达式为子查询
        useEqualsTo.setRightExpression(subSelect);

        return where == null ? useEqualsTo : new AndExpression(where, useEqualsTo);
    }

    private Expression getSuperWhere(Expression where) {
        return where;
    }

    private Expression getDefaultWhere(Expression where, TUserVo loginUser, DataPermission annotation) {
        EqualsTo useEqualsTo = new EqualsTo();
        useEqualsTo.setLeftExpression(new Column(annotation.field()));
        useEqualsTo.setRightExpression(new LongValue(loginUser.getId()));
        return where == null ? useEqualsTo : new AndExpression(where, useEqualsTo);
    }
}
