package com.alex.mission.service;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.OrderDTO;
import com.alex.mission.pojo.vo.OrderDetailVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *description:  订单service
 *author:       majf
 *createDate:   2022/7/12 17:23
 *version:      1.0.0
 */
public interface OrderService {

    Result<List<OrderDetailVo>> getOrderList(HttpServletRequest request);

    Result<Page<OrderDTO>> findByOrderId(Integer page, Integer pageSize, Long orderId);
}
