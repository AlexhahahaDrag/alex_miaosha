package com.alex.mission.service;

import com.alex.base.common.Result;
import com.alex.common.pojo.dto.GoodsDTO;
import com.alex.mission.pojo.vo.GoodsDetailVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface GoodsService {

    /**
     * description: 显示商品列表
     * author:      majf
     * createDate:  2022/7/11 16:10
     * return:      com.alex.common.common.Result<java.util.List<com.alex.mission.pojo.vo.GoodsDetailVo>>
    */
    Result<List<GoodsDetailVo>> getGoodsList();

    /**
     * @param goodsId
     * description: 显示秒杀商品详情
     * author:      majf
     * createDate:  2022/7/11 16:11
     * return:      com.alex.common.common.Result<com.alex.mission.pojo.vo.GoodsDetailVo>
    */
    Result<GoodsDetailVo> getDetail(Long goodsId);

    /**
     * @param page
     * @param pageSize
     * @param goodsName
     * description: 查询所有货物分页
     * author:      majf
     * createDate:  2022/7/11 16:12
     * return:      com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.alex.common.pojo.dto.GoodsDTO>
    */
    Result<Page<GoodsDTO>> findGoods(Long page, Long pageSize, String goodsName);

    /**
     * @param id
     * description: 单个删除商品
     * author:      majf
     * createDate:  2022/7/11 16:12
     * return:      boolean
    */
    boolean delete(Long id);

    /**
     * @param goodsDTO
     * description: 新增商品
     * author:      majf
     * createDate:  2022/7/11 16:13
     * return:      com.alex.common.common.Result<java.lang.Object>
    */
    Result<GoodsDTO> create(GoodsDTO goodsDTO);

    /**
     * @param goodsDTO
     * description: 更新商品
     * author:      majf
     * createDate:  2022/7/11 16:13
     * return:      com.alex.common.common.Result<java.lang.Object>
     */
    Result<GoodsDTO> update(GoodsDTO goodsDTO);

    /**
     * @param id
     * description: 根据id查询商品
     * author:      majf
     * createDate:  2022/7/11 16:14
     * return:      com.alex.common.pojo.dto.GoodsDTO
    */
    GoodsDTO selectById(Long id);

    /**
     * @param id
     * description: 更新是否可用
     * author:      majf
     * createDate:  2022/7/11 16:15
     * return:      boolean
    */
    boolean updateUsingById(Long id);

    /**
     * @param ids
     * description: 批量删除
     * author:      majf
     * createDate:  2022/7/11 16:15
     * return:      boolean
    */
    boolean deletes(String ids);
}
