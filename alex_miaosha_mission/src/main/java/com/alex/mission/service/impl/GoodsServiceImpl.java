package com.alex.mission.service.impl;

import com.alex.base.common.Result;
import com.alex.base.enums.RedisCacheTimeEnum;
import com.alex.base.enums.ResultEnum;
import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.common.pojo.dto.SeckillGoodsDTO;
import com.alex.common.redis.key.GoodsKey;
import com.alex.common.redis.key.SeckillGoodsKey;
import com.alex.common.redis.manager.RedisService;
import com.alex.common.utils.POJOConverter;
import com.alex.common.utils.qiniu.ImageScalaKit;
import com.alex.mission.manager.GoodsManager;
import com.alex.mission.manager.SeckillGoodsManager;
import com.alex.mission.mapper.GoodsMapper;
import com.alex.mission.pojo.entity.Goods;
import com.alex.mission.pojo.vo.GoodsDetailVo;
import com.alex.mission.service.GoodsService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private final GoodsMapper goodsMapper;

    private final GoodsManager goodsManagerService;

    private final RedisService redisService;

    private final SeckillGoodsManager seckillGoodsManager;

    private final ImageScalaKit imageScalaKit;

    public void initGoodsInfo() {
        List<Goods> goodsList = goodsManagerService.list();
        if (goodsList == null ||goodsList.isEmpty()) {
            return;
        }
        for (Goods goods : goodsList) {
            redisService.set(GoodsKey.goodsKey, "" + goods.getId(), goods, RedisCacheTimeEnum.GOODS_LIST_EXTIME.getValue());
        }
    }

    @Override
    public Result<List<GoodsDetailVo>> getGoodsList() {
        return Result.success(getGoodsDetailVos());
    }

    @Override
    public Result<GoodsDetailVo> getDetail(Long goodsId) {
        GoodsDTO goods = redisService.get(GoodsKey.goodsKey, "" + goodsId, GoodsDTO.class);
        if (goods == null) {
            Goods goodData = goodsManagerService.getById(goodsId);
            goods = goodsToGoodsDTO(goodData);
        }
        //商品不可用
        if (!goods.getIsUsing()) {
            return Result.success(null);
        }
        //用goodsId取出存在redis中的秒杀商品的库存值
        return getGoodsDetailVoResult(goodsId, goods);
    }

    @Override
    public Result<Page<GoodsDTO>> findGoods(Integer page, Integer pageSize, String goodsName) {
        Page<GoodsDTO> pageInfo = new Page<>(page, pageSize);
        Page<GoodsDTO> goodsPage = goodsMapper.findGoodsPage(pageInfo, goodsName);
        return Result.success(goodsPage);
    }

    @Override
    public boolean delete(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (StringUtils.isNotEmpty(goods.getGoodsImg())) {
            try {
                URL url = new URL(goods.getGoodsImg());
                imageScalaKit.delete(url.getPath().replaceFirst("/", ""));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        //删除缓存
        redisService.delete(GoodsKey.goodsKey, "" + id);
        redisService.delete(SeckillGoodsKey.seckillCount, "" + id);
        //删除商品
        goodsMapper.deleteById(id);
        seckillGoodsManager.deleteSeckillGoods(id);
        return true;
    }

    @Override
    public Result<GoodsDTO> create(GoodsDTO goodsDTO) {
        Goods goods = goodsDTOToGoods(goodsDTO, "id");
        goodsMapper.insert(goods);
        goodsDTO.setId(goods.getId());
        //添加商品到秒杀
        addGoodsToSeckillDatabase(goodsDTO);
        //添加商品到缓存
        addGoodsToCache(goodsDTO);
        return Result.success();
    }

    @Override
    public Result<GoodsDTO> update(GoodsDTO goodsDTO) {
        if (goodsDTO == null || goodsDTO.getId() == null) {
            return Result.error(ResultEnum.GOODS_UPDATE_ERROR);
        }
        Goods goods = goodsMapper.selectById(goodsDTO.getId());
        if (goods == null) {
            return Result.error(ResultEnum.GOODS_NOT_EXISTS);
        }
        BeanUtils.copyProperties(goodsDTO, goods);
        int i = goodsMapper.updateById(goods);
        if (i > 0) {
            //更新缓存信息
            addGoodsToCache(goodsDTO);
            //更新秒杀库
            updateGoodsToSeckillDatabase(goodsDTO);
        }
        return Result.success();
    }

    @Override
    public GoodsDTO selectById(Long id) {
        Goods goods = goodsMapper.selectById(id);
        return goodsToGoodsDTO(goods);
    }

    @Override
    public boolean updateUsingById(Long id) {
        GoodsDTO goodsDTO = redisService.get(GoodsKey.goodsKey, "" + id, GoodsDTO.class);
        goodsDTO.setIsUsing(!goodsDTO.getIsUsing());
        Goods goods = goodsDTOToGoods(goodsDTO);
        //更新商品信息
        goodsMapper.updateById(goods);
        //更新商品缓存信息
        redisService.set(GoodsKey.goodsKey, "" + id, goodsDTO, RedisCacheTimeEnum.GOODS_LIST_EXTIME.getValue());
        return true;
    }

    @Override
    public boolean deletes(String ids) {
        String[] idList = ids.split(",");
        List<String> goodsIdList = Arrays.asList(idList);
        deleteGoods(goodsIdList);
        return true;
    }

    private List<GoodsDetailVo> getGoodsDetailVos() {
        List<GoodsDTO> keys = redisService.keys(GoodsKey.goodsKey, GoodsDTO.class);
        return keys.parallelStream().map(item -> getGoodsDetailVoResult(item.getId(), item).getData()).collect(Collectors.toList());
    }

    private Result<GoodsDetailVo> getGoodsDetailVoResult(Long goodsId, GoodsDTO goods) {
        //从redis中获取库存信息
        int stockCount = redisService.get(SeckillGoodsKey.seckillCount, "" + goodsId, Integer.class);
        long startTime = Timestamp.valueOf(goods.getStartTime()).getTime();
        long endTime = Timestamp.valueOf(goods.getEndTime()).getTime();
        long now = System.currentTimeMillis();
        int remainSeconds;
        if (now < startTime) {
            //秒杀还没开始
            remainSeconds = (int)((startTime - now) / 1000);
        } else if (now > endTime) {
            //秒杀已经结束
            remainSeconds = -1;
        } else {
            //正在进行秒杀
            remainSeconds = 0;
        }
        return Result.success(GoodsDetailVo.builder()
                .goods(goods).stockCount(stockCount).remainSeconds(remainSeconds).build());
    }

    private void addGoodsToSeckillDatabase(GoodsDTO goodsDTO) {
        SeckillGoodsDTO seckillGoodsDTO = POJOConverter.converter(goodsDTO);
        seckillGoodsManager.addSeckillGoods(seckillGoodsDTO);
    }

    private void addGoodsToCache(GoodsDTO goodsDTO) {
        redisService.set(GoodsKey.goodsKey, "" + goodsDTO.getId(), goodsDTO, RedisCacheTimeEnum.GOODS_LIST_EXTIME.getValue());
        redisService.set(SeckillGoodsKey.seckillCount, "" + goodsDTO.getId(), goodsDTO.getGoodsStock(), RedisCacheTimeEnum.GOODS_LIST_EXTIME.getValue());
    }

    // TODO: 2022/7/13 测试注解是否好用 
    @Async(value = "myExecutor")
    void deleteGoods(List<String> goodsIdList) {
        goodsIdList.forEach(id -> {
            log.info("==========异步删除商品信息==========");
            delete(Long.valueOf(id));
        });
    }

    public void updateGoodsToSeckillDatabase(GoodsDTO goodsDTO) {
        SeckillGoodsDTO seckillGoodsDTO = POJOConverter.converter(goodsDTO);
        seckillGoodsManager.updateSeckillGoods(seckillGoodsDTO);
    }

    private Goods goodsDTOToGoods(GoodsDTO goodsDTO, String... ignore) {
        Goods goods = new Goods();
        BeanUtils.copyProperties(goodsDTO, goods, ignore);
        return goods;
    }

    private GoodsDTO goodsToGoodsDTO(Goods goods, String... ignore) {
        GoodsDTO goodsDTO = new GoodsDTO();
        BeanUtils.copyProperties(goods, goodsDTO, ignore);
        return goodsDTO;
    }
}
