package com.alex.finance.client.finance;
;
import java.util.List;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.finance.service.finance.FinanceInfoService;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:  财务信息表controller
 * @author:       majf
 * @createDate:   2022-10-10 16:56:00
 * @version:      1.0.0
 */
@Api(value = "财务信息表相关接口", tags = {"财务信息表相关接口"})
@RestController
@RequestMapping("/finance-info")
public class FinanceInfoFeignClient {


}
