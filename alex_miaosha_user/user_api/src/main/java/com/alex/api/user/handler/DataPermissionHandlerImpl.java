package com.alex.api.user.handler;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.vo.roleInfo.RoleInfoVo;
import com.alex.api.user.vo.user.TUserVo;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SubSelect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
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
            TUserVo loginUser = userUtils.getLoginUser();
            if (loginUser == null) {
                return where;
            }
            for (Method method : methods) {
                if (Objects.equals(method.getName(), methodName)) {
                    DataPermission annotation = method.getAnnotation(DataPermission.class);
                    if (ObjectUtils.isNotEmpty(annotation)) {
                        // TODO: 2024/1/15 添加用户机构表信息 
                        // // TODO: 2024/1/15 添加用户角色表信息 
                        // 判断当前用户角色决定权限
                        RoleInfoVo roleInfoVo = loginUser.getRoleInfoVo();
                        if (roleInfoVo == null || roleInfoVo.getRoleCode().equals("user")) {
                            EqualsTo useEqualsTo = new EqualsTo();
                            useEqualsTo.setLeftExpression(new Column(annotation.field()));
                            useEqualsTo.setRightExpression(new LongValue(loginUser.getId()));
                            return new AndExpression(where, useEqualsTo);
                        } else if (roleInfoVo.getRoleCode().contains("admin")) {
                            EqualsTo useEqualsTo = new EqualsTo();
                            useEqualsTo.setLeftExpression(new Column(annotation.field()));

                            // 构建子查询
                            SubSelect subSelect = new SubSelect();
                            PlainSelect plainSelect = new PlainSelect();

                            // 构建子查询中的 SELECT 部分
                            SelectExpressionItem selectItem = new SelectExpressionItem();
                            selectItem.setExpression(new Column("user_id")); // 假设你想选择 user_id 字段
                            plainSelect.addSelectItems(selectItem);

                            // 构建子查询中的 FROM 部分
                            Table table = new Table("t_org_user_info"); // 假设子查询的表名为 organization_users
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

                            useEqualsTo.setRightExpression(new LongValue(loginUser.getId()));
                            return new AndExpression(where, useEqualsTo);
                        } else if (roleInfoVo.getRoleCode().contains("super")) {
                            return where;
                        } else {
                            EqualsTo useEqualsTo = new EqualsTo();
                            useEqualsTo.setLeftExpression(new Column(annotation.field()));
                            useEqualsTo.setRightExpression(new LongValue(loginUser.getId()));
                            return new AndExpression(where, useEqualsTo);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return where;
    }


    /**
     * 构建过滤条件
     *
     * @param user  当前登录用户
     * @param where 当前查询条件
     * @return 构建后查询条件
     */
    public static Expression dataScopeFilter(TUserVo user, String tableAlias, Expression where) {
        Expression expression = null;
        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(buildColumn(tableAlias, "dept_id"));
        SubSelect subSelect = new SubSelect();
        PlainSelect select = new PlainSelect();
        select.setSelectItems(Collections.singletonList(new SelectExpressionItem(new Column("dept_id"))));
        select.setFromItem(new Table("t_sys_org"));
        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(new Column("id"));
        equalsTo.setRightExpression(new LongValue(user.getOrgInfoVo().getId()));
        select.setWhere(equalsTo);
        subSelect.setSelectBody(select);
        inExpression.setRightExpression(subSelect);
        expression = ObjectUtils.isNotEmpty(expression) ? new OrExpression(expression, inExpression) : inExpression;
//        for (SysRole role : user.getRoles()) {
//            String dataScope = role.getDataScope();
//            if (DataScopeAspect.DATA_SCOPE_ALL.equals(dataScope)) {
//                return where;
//            }
//            if (DataScopeAspect.DATA_SCOPE_CUSTOM.equals(dataScope)) {
//                InExpression inExpression = new InExpression();
//                inExpression.setLeftExpression(buildColumn(tableAlias, "dept_id"));
//                SubSelect subSelect = new SubSelect();
//                PlainSelect select = new PlainSelect();
//                select.setSelectItems(Collections.singletonList(new SelectExpressionItem(new Column("dept_id"))));
//                select.setFromItem(new Table("sys_role_dept"));
//                EqualsTo equalsTo = new EqualsTo();
//                equalsTo.setLeftExpression(new Column("role_id"));
//                equalsTo.setRightExpression(new LongValue(role.getRoleId()));
//                select.setWhere(equalsTo);
//                subSelect.setSelectBody(select);
//                inExpression.setRightExpression(subSelect);
//                expression = ObjectUtils.isNotEmpty(expression) ? new OrExpression(expression, inExpression) : inExpression;
//            }
//            if (DataScopeAspect.DATA_SCOPE_DEPT.equals(dataScope)) {
//                EqualsTo equalsTo = new EqualsTo();
//                equalsTo.setLeftExpression(buildColumn(tableAlias, "dept_id"));
//                equalsTo.setRightExpression(new LongValue(user.getDeptId()));
//                expression = ObjectUtils.isNotEmpty(expression) ? new OrExpression(expression, equalsTo) : equalsTo;
//            }
//            if (DataScopeAspect.DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope)) {
//                InExpression inExpression = new InExpression();
//                inExpression.setLeftExpression(buildColumn(tableAlias, "dept_id"));
//                SubSelect subSelect = new SubSelect();
//                PlainSelect select = new PlainSelect();
//                select.setSelectItems(Collections.singletonList(new SelectExpressionItem(new Column("dept_id"))));
//                select.setFromItem(new Table("sys_dept"));
//                EqualsTo equalsTo = new EqualsTo();
//                equalsTo.setLeftExpression(new Column("dept_id"));
//                equalsTo.setRightExpression(new LongValue(user.getDeptId()));
//                Function function = new Function();
//                function.setName("find_in_set");
//                function.setParameters(new ExpressionList(new LongValue(user.getDeptId()) , new Column("ancestors")));
//                select.setWhere(new OrExpression(equalsTo, function));
//                subSelect.setSelectBody(select);
//                inExpression.setRightExpression(subSelect);
//                expression = ObjectUtils.isNotEmpty(expression) ? new OrExpression(expression, inExpression) : inExpression;
//            }
//            if (DataScopeAspect.DATA_SCOPE_SELF.equals(dataScope)) {
//                EqualsTo equalsTo = new EqualsTo();
//                equalsTo.setLeftExpression(buildColumn(tableAlias, "create_by"));
//                equalsTo.setRightExpression(new StringValue(user.getUserName()));
//                expression = ObjectUtils.isNotEmpty(expression) ? new OrExpression(expression, equalsTo) : equalsTo;
//            }
//        }
        return ObjectUtils.isNotEmpty(where) ? new AndExpression(where, new Parenthesis(expression)) : expression;
    }

    /**
     * 构建Column
     *
     * @param tableAlias 表别名
     * @param columnName 字段名称
     * @return 带表别名字段
     */
    public static Column buildColumn(String tableAlias, String columnName) {
        if (StringUtils.isNotEmpty(tableAlias)) {
            columnName = tableAlias + "." + columnName;
        }
        return new Column(columnName);
    }
}
