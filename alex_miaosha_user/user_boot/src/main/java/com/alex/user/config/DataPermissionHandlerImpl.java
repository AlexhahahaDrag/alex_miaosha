package com.alex.user.config;

import com.alex.api.user.vo.user.TUserVo;
import com.alex.user.utils.user.UserUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
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
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.util.Collections;

public class DataPermissionHandlerImpl implements DataPermissionHandler {

    @Autowired
    private UserUtils userUtils;

    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        try {
            Class<?> clazz = Class.forName(mappedStatementId.substring(0, mappedStatementId.lastIndexOf(".")));
            String methodName = mappedStatementId.substring(mappedStatementId.lastIndexOf(".") + 1);
            Method[] methods = clazz.getDeclaredMethods();
//            TUserVo loginUser = userUtils.getLoginUser();
//            System.out.println(loginUser);
            for (Method method : methods) {
                DataPermission annotation = method.getAnnotation(DataPermission.class);
                if (ObjectUtils.isNotEmpty(annotation) && (method.getName().equals(methodName) || (method.getName() + "_COUNT").equals(methodName))) {
                    // 获取当前的用户
//                    SecurityUser loginUser = UserUtils.getLoginUser();
//                    System.out.println(loginUser);
//                    LoginUser loginUser = SpringUtils.getBean(TokenService.class).getLoginUser(ServletUtils.getRequest());
//                    if (ObjectUtils.isNotEmpty(loginUser) && ObjectUtils.isNotEmpty(loginUser.getUser()) && !loginUser.getUser().isAdmin()) {
//                        return dataScopeFilter(loginUser.getUser(), annotation.value(), where);
//                    }
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
     * @param user 当前登录用户
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
