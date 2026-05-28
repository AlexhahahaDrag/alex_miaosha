package com.alex.api.user.handler;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
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
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
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
            if (code.contains("super")) {
                isSuper = true;
            } else if (code.contains("admin")) {
                isAdmin = true;
            } else if (code.contains("user")) {
                isUser = true;
            }
        }

        if (isSuper) {
            return getSuperWhere(where);
        } else if (isAdmin) {
            return getAdminWhere(where, loginUser, annotation);
        } else if (isUser || roleCodes.isEmpty()) {
            return getUserWhere(where, loginUser, annotation);
        } else {
            return getDefaultWhere(where, loginUser, annotation);
        }
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

        // 构建 WHERE 子句
        EqualsTo whereCondition = new EqualsTo();
        whereCondition.setLeftExpression(new Column("org_id"));
        whereCondition.setRightExpression(new LongValue(loginUser.getOrgInfoVo().getId())); // 设置你想查询的机构 ID
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
