package com.alex.mission.service.impl;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.OrderDTO;
import com.alex.common.redis.key.UserKey;
import com.alex.common.redis.manager.RedisService;
import com.alex.mission.manager.GoodsManager;
import com.alex.mission.manager.OrderManager;
import com.alex.mission.mapper.OrderMapper;
import com.alex.mission.pojo.entity.Goods;
import com.alex.mission.pojo.entity.Order;
import com.alex.mission.pojo.vo.OrderDetailVo;
import com.alex.mission.service.OrderService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderManager orderManager;

    private final GoodsManager goodsManager;

    private final RedisService redisService;

    private final OrderMapper orderMapper;

    @Override
    public Result<List<OrderDetailVo>> getOrderList(HttpServletRequest request) {
        Long userId = getUserId(request);
        return getListResult(userId);
    }

    @Override
    public Result<Page<OrderDTO>> findByOrderId(Integer page, Integer pageSize, Long orderId) {
        Page<OrderDTO> pageInfo = new Page<>(page, pageSize);
        Page<OrderDTO> orderDTOPage = orderMapper.findPage(pageInfo, orderId);
        return Result.success(orderDTOPage);
    }

    private Long getUserId(HttpServletRequest request) {
        String authInfo = request.getHeader("Authorization");
        String loginToken = authInfo.split("Bearer")[1];
        return redisService.get(UserKey.getById, loginToken, Long.class);
    }

    private Result<List<OrderDetailVo>> getListResult(Long userId) {
        //获取订单数据
        List<Order> orderList = orderManager.list(Wrappers.<Order>lambdaQuery().eq(Order::getUserId, userId));
        //获取商品id列表及将商品信息转化成map
        List<Long> goodsIdList = orderList.parallelStream().map(Order::getGoodsId).collect(Collectors.toList());
        List<Goods> goodsList = goodsManager.list(Wrappers.<Goods>lambdaQuery().in(Goods::getId, goodsIdList));
        Map<Long, String> goodsMap = goodsList.parallelStream().collect(Collectors.toMap(Goods::getId, Goods::getGoodsName, (oldValue, newValue) -> newValue));
        List<OrderDetailVo> orderDetailVoList = orderList.parallelStream().map(order ->
                OrderDetailVo.builder()
                        .orderId(order.getId())
                        .goodsId(order.getGoodsId())
                        .createdTime(order.getCreatedAt())
                        .updatedTime(order.getUpdatedAt())
                        .goodsName(goodsMap.get(order.getGoodsId()))
                        .build()
        ).collect(Collectors.toList());
        return Result.success(orderDetailVoList);
    }
}
