package com.alex.finance.personalGift.service.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import com.alex.api.finance.personalGift.vo.ImportPersonalGiftInfoVo;
import com.alex.api.finance.personalGift.vo.PersonalGiftVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.handler.IExcelDictHandlerImpl;
import com.alex.finance.personalGift.entity.PersonalGift;
import com.alex.finance.personalGift.mapper.PersonalGiftMapper;
import com.alex.finance.personalGift.service.PersonalGiftService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * description:  个人随礼信息表服务实现类
 * author:       alex
 * createDate:   2024-07-10 10:01:28
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class PersonalGiftServiceImp extends ServiceImpl<PersonalGiftMapper, PersonalGift> implements PersonalGiftService {

    private final PersonalGiftMapper personalGiftMapper;

    private final IExcelDictHandlerImpl iExcelDictHandler;

    @Override
    public Page<PersonalGiftVo> getPage(Long pageNum, Long pageSize, PersonalGiftVo personalGiftVo) {
        Page<PersonalGiftVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return personalGiftMapper.getPage(page, personalGiftVo);
    }

    @Override
    public PersonalGiftVo queryPersonalGift(Long id) {
        return personalGiftMapper.queryPersonalGift(id);
    }

    @Override
    public Boolean addPersonalGift(PersonalGiftVo personalGiftVo) {
        PersonalGift personalGift = new PersonalGift();
        BeanUtils.copyProperties(personalGiftVo, personalGift);
        personalGiftMapper.insert(personalGift);
        return true;
    }

    @Override
    public Boolean updatePersonalGift(PersonalGiftVo personalGiftVo) {
        PersonalGift personalGift = new PersonalGift();
        BeanUtils.copyProperties(personalGiftVo, personalGift);
        personalGiftMapper.updateById(personalGift);
        return true;
    }

    @Override
    public Boolean deletePersonalGift(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        personalGiftMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public Boolean noticePersonalGift(Long id) {
        PersonalGift personalGift = personalGiftMapper.selectById(id);
        personalGift.setNoticeNum(personalGift.getNoticeNum() == null ? 1 : personalGift.getNoticeNum() + 1);
        personalGift.updateById();
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importPersonalGift(MultipartFile file) throws Exception {
        List<ImportPersonalGiftInfoVo> excelInfo = getExcelInfo(file);
        if (excelInfo == null || excelInfo.isEmpty()) {
            return true;
        }
        //将导入文件转化为bean
        List<PersonalGift> personalGiftList = excelInfo.parallelStream()
                .map(item -> {
                    PersonalGift personalGift = new PersonalGift();
                    BeanUtils.copyProperties(item, personalGift);
                    personalGift.setNoticeNum(0);
                    return personalGift;
                }).toList();
        this.saveBatch(personalGiftList);
        return true;
    }

    private List<ImportPersonalGiftInfoVo> getExcelInfo(MultipartFile file) throws Exception {
        ExcelImportResult<ImportPersonalGiftInfoVo> result;
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
        result = ExcelImportUtil.importExcelMore(file.getInputStream(), ImportPersonalGiftInfoVo.class, importParams);
        return result.getList();
    }
}
