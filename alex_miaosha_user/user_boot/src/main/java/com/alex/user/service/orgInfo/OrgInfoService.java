package com.alex.user.service.orgInfo;

import com.alex.api.user.vo.orgInfo.OrgInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.user.entity.orgInfo.OrgInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 机构表 服务类
 * author: alex
 * createDate: 2023-12-15 12:00:32
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface OrgInfoService extends IService<OrgInfo> {

    Page<OrgInfoVo> getPage(Long pageNum, Long pageSize, OrgInfoVo orgInfoVo);

    OrgInfoVo queryOrgInfo(String id);

    Boolean addOrgInfo(OrgInfoVo orgInfoVo);

    Boolean updateOrgInfo(OrgInfoVo orgInfoVo);

    Boolean deleteOrgInfo(String ids);
}
