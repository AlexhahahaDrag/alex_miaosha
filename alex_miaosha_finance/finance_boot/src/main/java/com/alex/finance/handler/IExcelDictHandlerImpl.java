package com.alex.finance.handler;

import cn.afterturn.easypoi.handler.inter.IExcelDictHandler;
import com.alex.finance.dict.entity.DictInfo;
import com.alex.finance.dict.service.DictInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * description:  excel字典handler配置
 * author:       majf
 * createDate:   2022/10/14 10:43
 * version:      1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IExcelDictHandlerImpl implements IExcelDictHandler {

    private final DictInfoService dictInfoService;

    @SuppressWarnings("rawtypes")
    @Override
    public List<Map> getList(String dict) {
        return Collections.emptyList();
    }

    @Override
    public String toName(String s, Object o, String s1, Object value) {
        DictInfo dictInfo = dictInfoService.queryDictInfoByTypeCode(String.valueOf(value));
        if (dictInfo == null) {
            throw new IllegalArgumentException("字典项不存在");
        }
        return dictInfo.getTypeName();
    }

    @Override
    public String toValue(String s, Object o, String s1, Object value) {
        DictInfo dictInfo = dictInfoService.queryDictInfoByTypeCode(String.valueOf(value));
        if (dictInfo == null) {
            throw new IllegalArgumentException("错误:" + value + "在字典中不存在!");
        }
        return dictInfo.getTypeCode();
    }
}
