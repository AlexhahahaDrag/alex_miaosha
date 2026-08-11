package com.alex.user.orgInfo.service;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.user.orgInfo.entity.OrgInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 机构表服务接口
 * author: alex
 * createDate: 2023-12-15 12:00:32
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface OrgInfoService extends IService<OrgInfo> {

    Page<OrgInfoVo> getPage(Long pageNum, Long pageSize, OrgInfoVo orgInfoVo);

    /**
     * RBAC-BE-ORG-003: scoped org list assembled into a tree (children by parentId).
     */
    List<OrgInfoVo> getTree(OrgInfoVo orgInfoVo);

    OrgInfoVo queryOrgInfo(String id);

    Boolean addOrgInfo(OrgInfoVo orgInfoVo);

    Boolean updateOrgInfo(OrgInfoVo orgInfoVo);

    Boolean deleteOrgInfo(String ids);
}
