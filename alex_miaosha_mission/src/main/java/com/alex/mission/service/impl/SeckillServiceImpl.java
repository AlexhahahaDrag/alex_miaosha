package com.alex.mission.service.impl;

import com.alex.common.common.Result;
import com.alex.mission.service.SeckillService;

import javax.servlet.http.HttpServletRequest;

public class SeckillServiceImpl implements SeckillService {



    @Override
    public Result<Integer> deSeckill(Long goodsId, String path, HttpServletRequest request) {
        return null;
    }

    @Override
    public Result<Long> seckillResult(Long goodsId, HttpServletRequest request) {
        return null;
    }

    @Override
    public Result<String> getSeckillPath(Long goodsId, HttpServletRequest request) {
        return null;
    }
}
