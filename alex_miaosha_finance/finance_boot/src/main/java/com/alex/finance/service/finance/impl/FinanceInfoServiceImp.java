package com.alex.finance.service.finance.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.hutool.core.bean.BeanUtil;
import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.finance.vo.finance.ImportFinanceInfoVo;
import com.alex.api.user.api.UserApi;
import com.alex.api.user.vo.user.TUserVo;
import com.alex.base.common.Result;
import com.alex.finance.entity.finance.FinanceInfo;
import com.alex.finance.handler.IExcelDictHandlerImpl;
import com.alex.finance.mapper.finance.FinanceInfoMapper;
import com.alex.finance.service.finance.FinanceInfoService;
import com.alex.common.utils.bean.BeanUtils;
import com.alex.common.utils.string.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * description: 财务信息表服务实现类
 * author: majf
 * createDate: 2022-10-10 18:02:03
 * version: 1.0.0
 */
@Service
@RequiredArgsConstructor
public class FinanceInfoServiceImp extends ServiceImpl<FinanceInfoMapper, FinanceInfo> implements FinanceInfoService {

    private final FinanceInfoMapper financeInfoMapper;

    private final IExcelDictHandlerImpl iExcelDictHandler;

    private final UserApi userApi;

    @Override
    public IPage<FinanceInfoVo> getPage(Long pageNum, Long pageSize, FinanceInfoVo financeInfoVo) {
        IPage<FinanceInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        Result<List<TUserVo>> list = userApi.getList(new TUserVo());
        Map<Long, TUserVo> userMap = Optional.ofNullable(list)
                .map(item -> item.getData().stream()
                        .collect(Collectors.toMap(TUserVo::getId, vo -> vo, (newVal, oldVal) -> newVal)))
                .orElse(new HashMap<>());
        IPage<FinanceInfoVo> result = financeInfoMapper.getPage(page, financeInfoVo);
        List<FinanceInfoVo> financeInfoVos = Optional.ofNullable(result)
                .map(item -> item.getRecords().stream().peek(
                        finance -> {
                            TUserVo tUserVo = userMap.get(finance.getBelongTo());
                            finance.setBelongToName(tUserVo == null ? null : (StringUtils.isEmpty(tUserVo.getNickName()) ? tUserVo.getUsername() : tUserVo.getNickName()));
                        }).collect(Collectors.toList()))
                .orElse(null);
        if (result != null) {
            result.setRecords(financeInfoVos);
        }
        return result;
    }

    @Override
    public List<FinanceInfoVo> getList(FinanceInfoVo financeInfoVo) {
        return financeInfoMapper.getList(financeInfoVo);
    }

    @Override
    public FinanceInfoVo queryFinanceInfo(String id) {
        return financeInfoMapper.queryFinanceInfo(id);
    }

    @Override
    public FinanceInfo addFinanceInfo(FinanceInfoVo financeInfoVo) {
        FinanceInfo financeInfo = new FinanceInfo();
        BeanUtil.copyProperties(financeInfoVo, financeInfo);
        financeInfoMapper.insert(financeInfo);
        return financeInfo;
    }

    @Override
    public FinanceInfo updateFinanceInfo(FinanceInfoVo financeInfoVo) {
        FinanceInfo financeInfo = new FinanceInfo();
        BeanUtil.copyProperties(financeInfoVo, financeInfo);
        financeInfoMapper.updateById(financeInfo);
        return financeInfo;
    }

    @Override
    public Boolean deleteFinanceInfo(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        financeInfoMapper.deleteBatchIds(Arrays.asList(ids.split(",")));
        return true;
    }


    @Override
    public boolean importFinance(MultipartFile file) throws Exception {
        List<ImportFinanceInfoVo> excelInfo = getExcelInfo(file);
        if (excelInfo == null || excelInfo.isEmpty()) {
            return true;
        }
        //将导入文件转化为bean
        List<FinanceInfo> financeList = excelInfo.parallelStream()
                .map(item -> {
                    FinanceInfo financeInfo = new FinanceInfo();
                    BeanUtils.copyProperties(item, financeInfo);
                    return financeInfo;
                }).collect(Collectors.toList());
        this.saveBatch(financeList);
        return true;
    }

    /**
     * @param file
     * description: 获取导入文件数据
     * author: majf
     * return: java.util.List<com.alex.finance.vo.finance.ImportFinanceInfoVo>
     */
    private List<ImportFinanceInfoVo> getExcelInfo(MultipartFile file) throws Exception {
        ExcelImportResult<ImportFinanceInfoVo> result;
        ImportParams importParams = new ImportParams();
        //设置导入位置
        importParams.setHeadRows(1);
        //设置首行
        importParams.setTitleRows(0);
        importParams.setStartRows(0);
        importParams.setStartSheetIndex(0);
        //是否需要校验上传的Excel
        importParams.setNeedVerify(false);
        //告诉easypoi我们自定义的验证器
        importParams.setDictHandler(iExcelDictHandler);
        result = ExcelImportUtil.importExcelMore(file.getInputStream(), ImportFinanceInfoVo.class, importParams);
        List<ImportFinanceInfoVo> list = result.getList();
        return list;
    }
}
