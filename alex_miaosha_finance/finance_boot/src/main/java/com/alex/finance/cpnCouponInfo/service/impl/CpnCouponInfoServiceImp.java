package com.alex.finance.cpnCouponInfo.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoImportVo;
import com.alex.api.finance.cpnCouponInfo.vo.CpnCouponInfoVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.cpnCouponInfo.entity.CpnCouponInfo;
import com.alex.finance.cpnCouponInfo.mapper.CpnCouponInfoMapper;
import com.alex.finance.cpnCouponInfo.service.CpnCouponInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * @description:  消费券信息表服务实现类
 * @author:       alex
 * @createDate:   2025-12-17 17:54:42
 * @version:      1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CpnCouponInfoServiceImp extends ServiceImpl<CpnCouponInfoMapper, CpnCouponInfo> implements CpnCouponInfoService {

    private final CpnCouponInfoMapper cpnCouponInfoMapper;

    @Override
    public Page<CpnCouponInfoVo> getPage(Long pageNum, Long pageSize, CpnCouponInfoVo cpnCouponInfoVo) {
        Page<CpnCouponInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        Page<CpnCouponInfoVo> result = cpnCouponInfoMapper.getPage(page, cpnCouponInfoVo);
        fillExpireStatus(result == null ? null : result.getRecords());
        return result;
    }

    @Override
    public List<CpnCouponInfoVo> getList(CpnCouponInfoVo cpnCouponInfoVo) {
        List<CpnCouponInfoVo> list = cpnCouponInfoMapper.getList(cpnCouponInfoVo);
        fillExpireStatus(list);
        return list;
    }

    @Override
    public CpnCouponInfoVo queryCpnCouponInfo(Long id) {
        CpnCouponInfoVo vo = cpnCouponInfoMapper.queryCpnCouponInfo(id);
        if (vo != null) {
            vo.setExpireStatus(calcExpireStatus(vo.getEndDate(), LocalDateTime.now()));
        }
        return vo;
    }

    @Override
    public Boolean addCpnCouponInfo(CpnCouponInfoVo cpnCouponInfoVo) {
        CpnCouponInfo cpnCouponInfo = new CpnCouponInfo();
        BeanUtils.copyProperties(cpnCouponInfoVo, cpnCouponInfo);
        cpnCouponInfoMapper.insert(cpnCouponInfo);
        return true;
    }

    @Override
    public Boolean updateCpnCouponInfo(CpnCouponInfoVo cpnCouponInfoVo) {
        CpnCouponInfo cpnCouponInfo = new CpnCouponInfo();
        BeanUtils.copyProperties(cpnCouponInfoVo, cpnCouponInfo);
        cpnCouponInfoMapper.updateById(cpnCouponInfo);
        return true;
    }

    @Override
    public Boolean deleteCpnCouponInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        cpnCouponInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    /**
     * 填充“过期倒计时状态”展示字段
     */
    private void fillExpireStatus(List<CpnCouponInfoVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (CpnCouponInfoVo item : list) {
            if (item == null) {
                continue;
            }
            item.setExpireStatus(calcExpireStatus(item.getEndDate(), now));
            item.setExpireRangeStatus(calcExpireRangeStatus(item.getEndDate(), now));
        }
    }

    /**
     * 计算过期状态：离过期还有三天 / 离过期还有一天 / 离过期还有X小时 / 离过期还有X分钟 / 过期
     */
    private String calcExpireStatus(LocalDateTime endDate, LocalDateTime now) {
        if (endDate == null) {
            return "";
        }
        // 统一使用“分钟”为最小粒度，避免小时/天之间反复换算造成误差
        final long MINUTES_PER_HOUR = 60L;
        final long MINUTES_PER_DAY = 24L * MINUTES_PER_HOUR;

        long minutes = Duration.between(now, endDate).toMinutes();
        if (minutes <= 0L) {
            return "已过期";
        }

        // >= 1天：展示剩余天数（向上取整，避免“还剩1.2天”显示为“剩余1天”造成误解）
        if (minutes >= MINUTES_PER_DAY) {
            long days = (minutes + MINUTES_PER_DAY - 1) / MINUTES_PER_DAY;
            return "剩余" + days + "天";
        }

        // >= 1小时：展示剩余小时（向上取整）
        if (minutes >= MINUTES_PER_HOUR) {
            long hours = (minutes + MINUTES_PER_HOUR - 1) / MINUTES_PER_HOUR;
            return "剩余" + hours + "小时";
        }

        // < 1小时：展示分钟
        return "剩余" + minutes + "分钟";
    }

    /**
     * 计算“过期区间状态”数字编码：
     * - 0：已过期
     * - 1：<1天
     * - 2：1-3天
     * - 3：>3天
     */
    private Integer calcExpireRangeStatus(LocalDateTime endDate, LocalDateTime now) {
        if (endDate == null) {
            return null;
        }

        // 与 calcExpireStatus 保持一致：用分钟做统一粒度
        final long MINUTES_PER_HOUR = 60L;
        final long MINUTES_PER_DAY = 24L * MINUTES_PER_HOUR;
        final long THREE_DAYS_MINUTES = 3L * MINUTES_PER_DAY;

        long minutes = Duration.between(now, endDate).toMinutes();
        if (minutes <= 0L) {
            return 0;
        }
        if (minutes < MINUTES_PER_DAY) {
            return 1;
        }
        if (minutes <= THREE_DAYS_MINUTES) {
            return 2;
        }
        return 3;
    }

    /**
     * AI Agent：导入消费券信息表
     * 
     * @param file 上传的Excel文件
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importCpnCouponInfo(MultipartFile file) {
        log.info("开始导入消费券信息表");
        
        // 验证文件
        if (file == null || file.isEmpty()) {
            log.warn("上传的文件为空");
            throw new RuntimeException("上传的文件不能为空");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            log.warn("文件格式不正确: {}", fileName);
            throw new RuntimeException("只支持 .xlsx 或 .xls 格式的Excel文件");
        }
        
        // 解析Excel文件
        List<CpnCouponInfoImportVo> excelInfo = getExcelInfo(file);
        if (excelInfo == null || excelInfo.isEmpty()) {
            log.warn("Excel文件内容为空");
            return true;
        }
        
        log.info("Excel读取数据成功: 共{}条记录", excelInfo.size());
        
        // 转换为实体并保存
        List<CpnCouponInfo> cpnCouponInfoList = excelInfo.stream()
                .map(this::convertImportVoToEntity)
                .filter(Objects::nonNull)
                .toList();
        
        if (cpnCouponInfoList.isEmpty()) {
            log.warn("没有有效的导入数据");
            return false;
        }
        
        // 批量保存
        this.saveBatch(cpnCouponInfoList);
        log.info("导入消费券信息表成功: 导入{}条记录", cpnCouponInfoList.size());
        
        return true;
    }

    /**
     * AI Agent：下载消费券信息表导入模版
     * 
     * @param response HTTP响应对象
     */
    @Override
    public void downloadTemplate(HttpServletResponse response) {
        log.info("下载消费券信息表导入模版");
        try {
            // 直接下载预设的模版文件
            String templatePath = "templates/cpn_coupon_info_template.xlsx";
            ClassPathResource resource = new ClassPathResource(templatePath);
            
            if (!resource.exists()) {
                log.warn("模版文件不存在: path={}", templatePath);
                response.setStatus(404);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"404\",\"message\":\"模版文件不存在\"}");
                return;
            }
            
            // 设置响应头 - 必须在写入数据前设置
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=cpn_coupon_info_template.xlsx");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            // 设置文件长度，防止 Excel 文件打开报错
            response.setContentLength((int) resource.getFile().length());
            
            // 读取文件并写入响应流
            // 注意：不能在 try-with-resources 中关闭 response.getOutputStream()
            try (java.io.InputStream inputStream = resource.getInputStream()) {
                java.io.OutputStream outputStream = response.getOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
                log.info("下载消费券信息表导入模版成功");
            }
        } catch (Exception e) {
            log.error("下载消费券信息表导入模版失败", e);
            try {
                response.setStatus(500);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"500\",\"message\":\"下载模版失败: " + e.getMessage() + "\"}");
            } catch (Exception ex) {
                log.error("写入错误响应失败", ex);
            }
        }
    }

    /**
     * AI Agent：解析 Excel 文件
     * 
     * @param file 上传的文件
     * @return Excel 中的数据列表
     */
    private List<CpnCouponInfoImportVo> getExcelInfo(MultipartFile file) {
        ExcelImportResult<CpnCouponInfoImportVo> result;
        ImportParams importParams = new ImportParams();
        // 设置导入位置
        importParams.setHeadRows(1);
        // 设置首行
        importParams.setTitleRows(0);
        importParams.setStartRows(0);
        importParams.setStartSheetIndex(0);
        // 是否需要校验上传的 Excel
        importParams.setNeedVerify(false);
        try {
            result = ExcelImportUtil.importExcelMore(file.getInputStream(), CpnCouponInfoImportVo.class, importParams);
            log.info("Excel解析成功: 共{}条数据", result.getList().size());
            return result.getList();
        } catch (Exception e) {
            log.error("Excel 解析失败", e);
            throw new RuntimeException("Excel文件解析失败: " + e.getMessage());
        }
    }

    /**
     * AI Agent：将 CpnCouponInfoImportVo 转换为 CpnCouponInfo 实体
     * 
     * @param importVo 导入VO
     * @return 实体对象
     */
    private CpnCouponInfo convertImportVoToEntity(CpnCouponInfoImportVo importVo) {
        if (importVo == null) {
            return null;
        }
        
        CpnCouponInfo entity = new CpnCouponInfo();
        entity.setCouponName(importVo.getCouponName());
        entity.setTotalQuantity(importVo.getTotalQuantity() == null ? 1 : importVo.getTotalQuantity());
        entity.setUnitValue(importVo.getUnitValue());
        entity.setMinSpend(importVo.getMinSpend());
        
        // 解析日期时间字符串
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            // AI Agent：有效期开始时间不填写时，默认为当前时间
            if (StringUtils.isNotEmpty(importVo.getStartDate())) {
                entity.setStartDate(LocalDateTime.parse(importVo.getStartDate(), formatter));
            } else {
                entity.setStartDate(LocalDateTime.now());
            }
            if (StringUtils.isNotEmpty(importVo.getEndDate())) {
                entity.setEndDate(LocalDateTime.parse(importVo.getEndDate(), formatter));
            }
        } catch (Exception e) {
            log.warn("日期解析失败: startDate={}, endDate={}, error={}", 
                    importVo.getStartDate(), importVo.getEndDate(), e.getMessage());
            // 日期解析失败时，如果开始时间为空，设置为当前时间
            if (entity.getStartDate() == null) {
                entity.setStartDate(LocalDateTime.now());
            }
        }
        
        return entity;
    }
}
