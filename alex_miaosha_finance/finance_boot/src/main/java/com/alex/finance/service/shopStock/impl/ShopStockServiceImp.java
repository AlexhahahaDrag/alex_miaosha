package com.alex.finance.service.shopStock.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.hutool.core.bean.BeanUtil;
import com.alex.api.finance.vo.shopStock.ImportShopStockInfoVo;
import com.alex.api.finance.vo.shopStock.ShopStockVo;
import com.alex.base.constants.SysConf;
import com.alex.common.redis.key.ShopStockKey;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.entity.shopStock.ShopStock;
import com.alex.finance.handler.IExcelDictHandlerImpl;
import com.alex.finance.mapper.shopStock.ShopStockMapper;
import com.alex.finance.service.shopStock.ShopStockService;
import com.alex.finance.shopStockAttrs.entity.ShopStockAttrs;
import com.alex.finance.shopStockAttrs.service.ShopStockAttrsService;
import com.alex.finance.utils.CodeUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * description:  商店库存表服务实现类
 * author:       alex
 * createDate:   2024-03-12 16:49:20
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopStockServiceImp extends ServiceImpl<ShopStockMapper, ShopStock> implements ShopStockService {

    private final ShopStockMapper shopStockMapper;

    private final IExcelDictHandlerImpl iExcelDictHandler;

    private final ShopStockAttrsService shopStockAttrsService;

    private final CodeUtils codeUtils;

    @Override
    public Page<ShopStockVo> getPage(Long pageNum, Long pageSize, ShopStockVo shopStockVo) {
        Page<ShopStockVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return shopStockMapper.getPage(page, shopStockVo);
    }

    @Override
    public ShopStockVo queryShopStock(Long id) {
        return shopStockMapper.queryShopStock(id);
    }

    @Override
    public Boolean addShopStock(ShopStockVo shopStockVo) {
        ShopStock shopStock = new ShopStock();
        BeanUtil.copyProperties(shopStockVo, shopStock);
        String saleDate = DateUtils.getTimeStr(shopStockVo.getSaleDate(), "yyyyMMdd");
        shopStock.setShopCode(codeUtils.getCode(ShopStockKey.shopStockKey, saleDate, shopStockVo.getPurchasePlace(), shopStockVo.getCategory()));
        shopStockMapper.insert(shopStock);
        List<ShopStockAttrs> attrsList = Lists.newArrayList();
        getShopStockAttrs(attrsList, shopStockVo.getSize(), shopStockVo.getColor(), shopStockVo.getStyle(), shopStock.getId());
        if (!attrsList.isEmpty()) {
            shopStockAttrsService.saveBatch(attrsList);
        }
        return true;
    }

    private void getShopStockAttrs( List<ShopStockAttrs> attrsList, String size, String color, String style, Long id) {
        if (StringUtils.isNotEmpty(size)) {
            attrsList.add(getAttr("size", "尺码", size, id));
        }
        if (StringUtils.isNotEmpty(size)) {
            attrsList.add(getAttr("color", "颜色", color, id));
        }
        if (StringUtils.isNotEmpty(size)) {
            attrsList.add(getAttr("style", "款式", style, id));
        }
    }

    /**
     * param: shopStockVo
     * description: 更新商品库存属性
     * author:      majf
     * return:      java.lang.Boolean
    */
    @Override
    public Boolean updateShopStock(ShopStockVo shopStockVo) {
        ShopStock shopStock = new ShopStock();
        BeanUtil.copyProperties(shopStockVo, shopStock);
        shopStockMapper.updateById(shopStock);
        List<ShopStockAttrs> attrsList = Lists.newArrayList();
        getShopStockAttrs(attrsList, shopStockVo.getSize(), shopStockVo.getColor(), shopStockVo.getStyle(), shopStock.getId());
        if (!attrsList.isEmpty()) {
            shopStockAttrsService.deleteShopStockAttrsByStockId(shopStock.getId());
            shopStockAttrsService.saveBatch(attrsList);
        }
        return true;
    }

    @Override
    public Boolean deleteShopStock(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        shopStockMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public List<ShopStockVo> getShopList(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return Lists.newArrayList();
        }
        return shopStockMapper.getShopList(Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean importShopStockInfo(MultipartFile file) throws Exception {
        List<ImportShopStockInfoVo> excelInfo = getExcelInfo(file);
        if (excelInfo == null || excelInfo.isEmpty()) {
            return true;
        }
        List<ShopStockAttrs> attrsList = Lists.newArrayList();
        //将导入文件转化为bean
        excelInfo.forEach(item -> {
            ShopStock stock = new ShopStock();
            BeanUtils.copyProperties(item, stock);
            stock.setIsValid(SysConf.VALID_STATUS);
            stock.insert();
            getShopStockAttrs(attrsList, item.getSize(), item.getColor(), item.getStyle(), stock.getId());
        });
        if (!attrsList.isEmpty()) {
            shopStockAttrsService.saveBatch(attrsList);
        }
        return true;
    }

    /**
     * @param code
     * @param name
     * @param value
     * @param stockId
     * @description: 拼接商品库存属性数据
     * @author:      alex
     * @return:      com.alex.finance.shopStockAttrs.entity.ShopStockAttrs
    */

    private ShopStockAttrs getAttr(String code, String name, String value, Long stockId) {
        ShopStockAttrs shopStockAttrs = new ShopStockAttrs();
        shopStockAttrs.setAttrCode(code).setAttrName(name)
                .setAttrValue(value).setStockId(stockId).setIsValid(SysConf.VALID_STATUS);
        return shopStockAttrs;
    }

    private List<ImportShopStockInfoVo> getExcelInfo(MultipartFile file) throws Exception {
        ExcelImportResult<ImportShopStockInfoVo> result;
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
        result = ExcelImportUtil.importExcelMore(file.getInputStream(), ImportShopStockInfoVo.class, importParams);
        return result.getList();
    }
}
