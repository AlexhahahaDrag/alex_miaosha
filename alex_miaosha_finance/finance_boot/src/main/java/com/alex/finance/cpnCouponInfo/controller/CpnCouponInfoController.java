package com.alex.finance.cpnCouponInfo.controller;

import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.annotations.LogRestRequest;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import org.springframework.validation.annotation.Validated;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import com.alex.base.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import com.alex.finance.cpnCouponInfo.service.CpnCouponInfoService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 消费券信息表 控制器
 * 
 * @author alex
 * @since 2025-12-17 17:54:42
 * @version 1.0.0
 */
@ApiSort(105)
@Api(value = "消费券信息表相关接口", tags = {"消费券信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/cpn-coupon-info")
public class CpnCouponInfoController {

    /**
     * 消费券信息表 服务
     */
    private final CpnCouponInfoService cpnCouponInfoService;

    /**
     * 分页查询消费券信息表
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param cpnCouponInfoVo 查询条件
     * @return 分页结果
     */
    @LogRestRequest(apiName = "获取消费券信息表分页")
    @ApiOperationSupport(order = 10, author = "alex")
    @ApiOperation(value = "获取消费券信息表分页", notes = "分页查询消费券信息表列表", response = Result.class)
    @PostMapping(value = "/page")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class, example = "1"),
            @ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class, example = "10"),
            @ApiImplicitParam(value = "查询条件", name = "cpnCouponInfoVo", dataTypeClass = CpnCouponInfoVo.class)}
    )
    public Result<Page<CpnCouponInfoVo>> getPage(@RequestParam(value = "pageNum", required = false, defaultValue = "1") Long pageNum,
                                                  @RequestParam(value = "pageSize", required = false, defaultValue = "10") Long pageSize,
                                                  @RequestBody(required = false) CpnCouponInfoVo cpnCouponInfoVo) {
        return Result.success(cpnCouponInfoService.getPage(pageNum, pageSize, cpnCouponInfoVo));
    }

    /**
     * 根据 ID查询消费券信息表详情
     * 
     * @param id 主键 ID
     * @return 消费券信息表详情
     */
    @LogRestRequest(apiName = "获取消费券信息表详情")
    @ApiOperationSupport(order = 20, author = "alex")
    @ApiOperation(value = "获取消费券信息表详情", notes = "根据ID 查询消费券信息表详细信息", response = Result.class)
    @GetMapping
    @ApiImplicitParam(value = "主键 ID", name = "id", required = true, dataTypeClass = Long.class, paramType = "query")
    public Result<CpnCouponInfoVo> query(@RequestParam Long id) {
        return Result.success(cpnCouponInfoService.queryCpnCouponInfo(id));
    }

    /**
     * 新增消费券信息表
     * 
     * @param cpnCouponInfoVo 消费券信息表信息
     * @return 操作结果
     */
    @LogRestRequest(apiName = "新增消费券信息表")
    @AvoidRepeatableCommit
    @ApiOperationSupport(order = 30, author = "alex")
    @ApiOperation(value = "新增消费券信息表", notes = "创建新的消费券信息表记录", response = Result.class)
    @PostMapping
    public Result<Boolean> add(@Validated({Insert.class}) @RequestBody CpnCouponInfoVo cpnCouponInfoVo) {
        return Result.success(cpnCouponInfoService.addCpnCouponInfo(cpnCouponInfoVo));
    }

    /**
     * 修改消费券信息表
     * 
     * @param cpnCouponInfoVo 消费券信息表信息
     * @return 操作结果
     */
    @LogRestRequest(apiName = "修改消费券信息表")
    @ApiOperationSupport(order = 40, author = "alex")
    @ApiOperation(value = "修改消费券信息表", notes = "更新消费券信息表信息", response = Result.class)
    @PutMapping
    public Result<Boolean> update(@Validated({Update.class}) @RequestBody CpnCouponInfoVo cpnCouponInfoVo) {
        return Result.success(cpnCouponInfoService.updateCpnCouponInfo(cpnCouponInfoVo));
    }

    /**
     * 删除消费券信息表
     * 
     * @param ids 主键ID列表，多个ID用逗号分隔
     * @return 操作结果
     */
    @LogRestRequest(apiName = "删除消费券信息表")
    @ApiOperationSupport(order = 50, author = "alex")
    @ApiOperation(value = "删除消费券信息表", notes = "根据ID 列表批量删除消费券信息表", response = Result.class)
    @DeleteMapping
    @ApiImplicitParam(value = "主键ID列表，多个ID用逗号分隔", name = "ids", required = true, dataTypeClass = String.class, example = "1,2,3")
    public Result<Boolean> delete(@RequestParam String ids) {
        return Result.success(cpnCouponInfoService.deleteCpnCouponInfo(ids));
    }

    /**
     * 导入消费券信息表
     * 
     * @param file 上传的 Excel文件
     * @return 操作结果
     * @throws Exception 异常
     */
    @LogRestRequest(apiName = "导入消费券信息表")
    @ApiOperationSupport(order = 60, author = "alex")
    @ApiOperation(value = "导入消费券信息表", notes = "通过Excel 文件导入消费券信息表", response = Result.class)
    @PostMapping(value = "/import")
    @ApiImplicitParam(value = "Excel 文件", name = "file", required = true, dataTypeClass = MultipartFile.class)
    public Result<Boolean> importCpnCouponInfo(@RequestPart("file") MultipartFile file) throws Exception {
        return Result.success(cpnCouponInfoService.importCpnCouponInfo(file));
    }

    /**
     * 下载消费券信息表导入模版
     * 
     * @param response HTTP 响应对象
     * @throws Exception 异常
     */
    @LogRestRequest(apiName = "下载消费券信息表导入模版")
    @ApiOperationSupport(order = 70, author = "alex")
    @ApiOperation(value = "下载消费券信息表导入模版", notes = "下载消费券信息表Excel 导入模版文件", response = Result.class)
    @GetMapping(value = "/template")
    public void downloadTemplate(HttpServletResponse response) throws Exception {
        cpnCouponInfoService.downloadTemplate(response);
    }
}
