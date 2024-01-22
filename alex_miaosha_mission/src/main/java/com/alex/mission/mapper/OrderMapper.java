package com.alex.mission.mapper;

import com.alex.common.pojo.dto.OrderDTO;
import com.alex.mission.pojo.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

public interface OrderMapper extends BaseMapper<Order> {

    Page<OrderDTO> findPage(Page<OrderDTO> pageInfo, @Param("orderId") Long orderId);
}
