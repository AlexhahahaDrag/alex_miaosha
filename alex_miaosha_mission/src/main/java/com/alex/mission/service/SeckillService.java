package com.alex.mission.service;

import com.alex.base.common.Result;

import javax.servlet.http.HttpServletRequest;

public interface SeckillService {

    /**
     * @param goodsId
     * @param path
     * @param request
     * description: 执行秒杀
     * author:      majf
     * createDate:  2022/7/13 17:39
     * return:      com.alex.common.common.Result<java.lang.Integer>
    */
    Result<Integer> doSeckill(Long goodsId, String path, HttpServletRequest request);

    /**
     * @param goodsId
     * @param request
     * description: 客户端轮询查询是否下单成功
     * author:      majf
     * createDate:  2022/7/13 17:39
     * return:      com.alex.common.common.Result<java.lang.Long>
    */
    Result<Long> seckillResult(Long goodsId, HttpServletRequest request);

    /**
     * @param goodsId
     * @param request
     * description: 返回一个唯一的path的id
     * author:      majf
     * createDate:  2022/7/13 17:39
     * return:      com.alex.common.common.Result<java.lang.String>
    */
    Result<String> getSeckillPath(Long goodsId, HttpServletRequest request);
}
