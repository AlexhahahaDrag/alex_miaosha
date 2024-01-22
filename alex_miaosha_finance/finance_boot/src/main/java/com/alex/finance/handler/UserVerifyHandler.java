package com.alex.finance.handler;

import cn.afterturn.easypoi.excel.entity.result.ExcelVerifyHandlerResult;
import cn.afterturn.easypoi.handler.inter.IExcelVerifyHandler;
import com.alex.api.finance.vo.finance.ImportFinanceInfoVo;

/**
 * description:
 * author:       majf
 * createDate:   2022/10/14 14:59
 * version:      1.0.0
 */
public class UserVerifyHandler implements IExcelVerifyHandler<ImportFinanceInfoVo> {

    @Override
    public ExcelVerifyHandlerResult verifyHandler(ImportFinanceInfoVo importFinanceInfoVo) {
        //假设我们要添加用户，现在去数据库查询realName，如果存在则表示校验不通过。
//        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getRealName, vo.getRealName()));
//        if (user != null) {
//            result.setMsg("唯一校验失败");
//            result.setSuccess(false);
//            return result;
//        }
//        result.setSuccess(true);
        return null;
    }
}
