package com.alex.api.user.handler;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.annotation.DataPermissionScope;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataPermissionHandlerImpl implements DataPermissionHandler {

    private final UserUtils userUtils;

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

        if (annotation.scope() == DataPermissionScope.ORG_SHARED) {
            return getOrgSharedWhere(where, loginUser, annotation);
        }

        if (roleFlags.isAdmin()) {
            return getAdminWhere(where, loginUser, annotation);
        }
        if (roleFlags.isUser() || roleFlags.isEmpty()) {
            return getUserWhere(where, loginUser, annotation);
        }
        return getDefaultWhere(where, loginUser, annotation);
    }

    private Expression getOrgSharedWhere(Expression where, TUserVo loginUser, DataPermission annotation) {
        Long orgId = resolveLoginOrgId(loginUser);
        if (orgId == null) {
            log.warn("家庭组共享模式未解析到机构，降级为个人数据权限：userId={}", loginUser.getId());
            return getUserWhere(where, loginUser, annotation);
        }
        if (StringUtils.hasText(annotation.orgField())) {
            return appendFieldEquals(where, annotation.table(), annotation.orgField(), orgId);
        }
        return appendOrgMembersIn(where, loginUser, annotation, orgId);
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
        return appendFieldEquals(where, annotation.table(), annotation.field(), loginUser.getId());
    }

    private Expression getAdminWhere(Expression where, TUserVo loginUser, DataPermission annotation) {
        Long orgId = resolveLoginOrgId(loginUser);
        if (orgId == null) {
            log.warn("管理员未关联所属机构，降级为个人数据权限：userId={}", loginUser.getId());
            return getUserWhere(where, loginUser, annotation);
        }
        return appendOrgMembersIn(where, loginUser, annotation, orgId);
    }

    private Expression appendOrgMembersIn(Expression where, TUserVo loginUser, DataPermission annotation, Long orgId) {
        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(new Column(new Table(annotation.table()), annotation.field()));

        PlainSelect plainSelect = new PlainSelect();
        plainSelect.addSelectItems(new SelectItem<>(new Column("user_id")));

        Table table = new Table("alex_user.t_org_user_info");
        plainSelect.setFromItem(table);

        EqualsTo whereCondition = new EqualsTo();
        whereCondition.setLeftExpression(new Column("org_id"));
        whereCondition.setRightExpression(new LongValue(orgId));
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
        EqualsTo useEqualsTo = new EqualsTo();
        useEqualsTo.setLeftExpression(new Column(annotation.field()));
        useEqualsTo.setRightExpression(new LongValue(loginUser.getId()));
        return where == null ? useEqualsTo : new AndExpression(where, useEqualsTo);
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
            if (code.contains("super")) {
                isSuper = true;
            } else if (code.contains("admin")) {
                isAdmin = true;
            } else if (code.contains("user")) {
                isUser = true;
            }
        }
        return new RoleFlags(isSuper, isAdmin, isUser, roleCodes.isEmpty());
    }

    private record RoleFlags(boolean isSuper, boolean isAdmin, boolean isUser, boolean isEmpty) {
    }
}
