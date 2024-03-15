package com.alex.api.user.handler;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.vo.roleInfo.RoleInfoVo;
import com.alex.api.user.vo.user.TUserVo;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class DataPermissionHandlerImpl implements DataPermissionHandler {

    private final UserUtils userUtils;

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        try {
            Class<?> clazz = Class.forName(mappedStatementId.substring(0, mappedStatementId.lastIndexOf(".")));
            Method[] methods = clazz.getDeclaredMethods();
            String methodName = mappedStatementId.substring(mappedStatementId.lastIndexOf(".") + 1);
            TUserVo loginUser = null;
            if (userUtils != null) {
                loginUser = userUtils.getLoginUser();
            }
            if (loginUser == null) {
                return where;
            }
            for (Method method : methods) {
                if (Objects.equals(method.getName(), methodName)) {
                    DataPermission annotation = method.getAnnotation(DataPermission.class);
                    if (ObjectUtils.isNotEmpty(annotation)) {
                        // 判断当前用户角色决定权限
                        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
                        if (roleInfoVo == null || roleInfoVo.getRoleCode().contains("user")) {
                            return getUserWhere(where, loginUser, annotation);
                        } else if (roleInfoVo.getRoleCode().contains("admin")) {
                            return getAdminWhere(where, loginUser, annotation);
                        } else if (roleInfoVo.getRoleCode().contains("super")) {
                            return getSuperWhere(where);
                        } else {
                            return getDefaultWhere(where, loginUser, annotation);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return where;
    }

    private Expression getUserWhere(Expression where, TUserVo loginUser, DataPermission annotation) {
        EqualsTo useEqualsTo = new EqualsTo();
        useEqualsTo.setLeftExpression(new Column(new Table(annotation.table()), annotation.field()));
        useEqualsTo.setRightExpression(new LongValue(loginUser.getId()));
        return where == null ? useEqualsTo : new AndExpression(where, useEqualsTo);
    }

    private Expression getAdminWhere(Expression where, TUserVo loginUser, DataPermission annotation) {
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
        whereCondition.setRightExpression(new LongValue(loginUser.getOrgInfoVo().getId())); // 设置你想查询的机构ID
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
