package com.alex.mission.service.impl;

import cn.hutool.core.lang.UUID;
import com.alex.base.common.Result;
import com.alex.base.enums.RedisCacheTimeEnum;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SeckillException;
import com.alex.common.obj.SeckillMessage;
import com.alex.common.redis.key.SeckillGoodsKey;
import com.alex.common.redis.key.SeckillKey;
import com.alex.common.utils.redis.LuaUtils;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.mission.manager.OrderManager;
import com.alex.mission.manager.SeckillGoodsManager;
import com.alex.mission.pojo.entity.Order;
import com.alex.mission.pojo.entity.SeckillGoods;
import com.alex.mission.rabbitmq.ackmodel.manual.ManualAckPublisher;
import com.alex.mission.service.SeckillService;
import com.alex.utils.sm3.SM3Utils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *description:  秒杀服务实现类
 * @PostConstruct 在类创建自己构造器和@Autowire 之后执行
 *author:       majf
 *createDate:   2022/7/14 9:24
 *version:      1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    private final SeckillGoodsManager seckillGoodsManager;

    private final RedisUtils redisUtils;

    private final Map<Long, Boolean> localOverMap = new ConcurrentHashMap<>();

    private final OrderManager orderManager;

    private final ManualAckPublisher manualAckPublisher;

    private final LuaUtils redisLua;

    /**
     * description: 初始化库存数量
     * author:      majf
     * createDate:  2022/7/14 10:08
     * @return:      void
     */
    @PostConstruct
    public void initSekillGoodsNumber() {
        List<SeckillGoods> list = seckillGoodsManager.list();
        if (list == null || list.isEmpty()) {
            return;
        }
        list.forEach(seckillGoods -> {
            redisUtils.set(SeckillGoodsKey.seckillCount, "" + seckillGoods.getGoodsId(), seckillGoods.getGoodsStock(), RedisCacheTimeEnum.GOODS_LIST_EXTIME.getValue());
            localOverMap.put(seckillGoods.getGoodsId(), seckillGoods.getGoodsStock() > 0);
        });
    }

    /**
     * @param goodsId
     * @param path
     * @param request
     * description:  执行秒杀
     * author:      majf
     * createDate:  2022/7/14 10:07
     * @return:      com.alex.common.common.Result<java.lang.Integer>
     */
    @Override
    @Transactional
    public Result<Integer> doSeckill(Long goodsId, String path, HttpServletRequest request) {
        Long userId = 0l;
        // TODO: 2022/8/30 验证重复秒杀 
        //验证path
//        checkPath(goodsId, path, userId);
        //校验是否超卖
        // TODO: 2022/8/18 校验超卖的解决方案 
        isCountOver(goodsId);
        //使用幂等机制，根据用户号和商品id生成订单。防止重复秒杀
        // TODO: 2022/8/13 生成订单方式 
        Long orderId = goodsId * 1000000 + userId;
        Order order = orderManager.getOne(Wrappers.<Order>lambdaQuery().eq(Order::getId, orderId));
        if (order != null) {
            throw new SeckillException(ResultEnum.SECKILL_REPEAT.getCode(), ResultEnum.SECKILL_REPEAT.getValue());
        }
        //lua脚本判断库存和预减库存
        Long stock = luaCheckAndReduceStock(goodsId);
        //入队
        doMQ(goodsId, userId);
        return Result.success("用户：" + userId + "秒杀成功，剩余库存：" + stock);
    }

    /**
     * @param goodsId
     * @param request
     * description: 前端服务轮询查询是否下单成功
     * author:      majf
     * createDate:  2022/7/14 10:07
     * @return:      com.alex.common.common.Result<java.lang.Long>
     */
    @Override
    public Result<Long> seckillResult(Long goodsId, HttpServletRequest request) {
        Long userId = 0l;
        //秒杀结果： orderId：成功， 0：排队中， -1：秒杀失败
        long result = 0;
        //查询订单数据
        Order order = orderManager.getOne(
                Wrappers.<Order>lambdaQuery().
                        eq(Order::getUserId, userId)
                        .eq(Order::getGoodsId, goodsId
                        ));
        if (order != null) {
            result = order.getId();
        } else if (getGoodsOver(goodsId)) {
            //判断秒杀是否结束
            result = -1;
        }
        return Result.success(result);
    }

    /**
     * @param goodsId
     * @param request
     * description: 返回唯一的path
     * author:      majf
     * createDate:  2022/7/14 17:10
     * @return:      com.alex.common.common.Result<java.lang.String>
    */
    @Override
    public Result<String> getSeckillPath(Long goodsId, HttpServletRequest request) {
        Long userId = 0l;
        return Result.success(ResultEnum.SUCCESS.getValue(), createSeckillPath(userId, goodsId));
    }

    /**
     * @param goodsId
     * @param path
     * @param userId
     * description: 校验路径
     * author:      majf
     * createDate:  2022/7/14 10:08
     * @return:      void
     */
    private void checkPath(Long goodsId, String path, Long userId) {
        if (!checkPath(userId, goodsId, path)) {
            throw new SeckillException(ResultEnum.REQUIRE_ILLEGAL.getCode(), ResultEnum.REQUIRE_ILLEGAL.getValue());
        }
    }

    /**
     * @param goodsId
     * description: 判断是否超卖
     * author:      majf
     * createDate:  2022/7/14 10:08
     * @return:      void
     */
    private void isCountOver(Long goodsId) {
        // TODO: 2022/8/13 判断是否超卖方法 
        if (!localOverMap.get(goodsId)) {
            throw new SeckillException(ResultEnum.SECKILL_OVER.getCode(), ResultEnum.SECKILL_OVER.getValue());
        }
    }
    /**
     * @param goodsId
     * description: lua脚本判断库存是否超卖，并减小库存
     * author:      majf
     * createDate:  2022/7/14 10:09
     * @return:      void
     */
    private Long luaCheckAndReduceStock(Long goodsId) {
        Long count = redisLua.judgeStockAndDecrStock(goodsId);
        if (count == -1) {
            localOverMap.put(goodsId, false);
            throw new SeckillException(ResultEnum.SECKILL_OVER.getCode(), ResultEnum.SECKILL_OVER.getValue());
        }
        return count;
    }

    /**
     * @param goodsId
     * @param userId
     * description: 使用mq发送消息
     * author:      majf
     * createDate:  2022/7/14 10:09
     * @return:      void
     */
    private void doMQ(Long goodsId, Long userId) {
        SeckillMessage seckillMessage = SeckillMessage.builder().goodsId(goodsId).userId(userId).build();
        //这里使用rabiitMQ多消费者实例，增加并发能力
        manualAckPublisher.sendMsg(seckillMessage);
    }

    /**
     * @param userId
     * @param goodsId
     * @param path
     * description: 在redis中验证path
     * author:      majf
     * createDate:  2022/7/14 10:10
     * @return:      boolean
     */
    private boolean checkPath(Long userId, Long goodsId, String path) {
        if (userId == null || StringUtils.isEmpty(path)) {
            return false;
        }
        String redisPath = redisUtils.get(SeckillKey.getSeckillPath, userId + "_" + goodsId, String.class);
        return path.equals(redisPath);
    }

    /**
     * @param goodsId
     * description: 判断秒杀是否结束
     * author:      majf
     * createDate:  2022/7/14 17:08
     * @return:      boolean
    */
    private boolean getGoodsOver(Long goodsId) {
        return redisUtils.exists(SeckillKey.isGoodOver, "" + goodsId);
    }

    /**
     * @param userId
     * @param goodsId
     * description: 动态生成url
     * author:      majf
     * createDate:  2022/7/14 17:12
     * @return:      java.lang.String
    */
    private String createSeckillPath(Long userId, Long goodsId) {
        if (userId == null || goodsId == null) {
            return null;
        }
        // TODO: 2022/8/25 判断秒杀是否开始
        Object stock = redisUtils.get(SeckillGoodsKey.seckillCount, "" + goodsId);
        if (stock == null) {
            throw new SeckillException(ResultEnum.SECKILL_NO_START);
        }
        if (Long.parseLong(stock + "") <= 0) {
            throw new SeckillException(ResultEnum.SECKILL_OVER);
        }
        String str = SM3Utils.sm3(UUID.randomUUID() + "123456");
        redisUtils.set(SeckillKey.getSeckillPath, userId + "_" + goodsId, str, RedisCacheTimeEnum.GOODS_LIST_EXTIME.getValue());
        log.info("库存数量:" + stock);
        return str;
    }
}
