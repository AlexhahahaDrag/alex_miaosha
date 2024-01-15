package com.alex.user.service.orgUserInfo;

import com.alex.api.user.vo.orgInfo.OrgInfoVo;
import com.alex.api.user.vo.orgUserInfo.OrgUserInfoVo;
import com.alex.user.entity.orgUserInfo.OrgUserInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户公司信息表 服务类
 * author: majf
 * createDate: 2024-01-15 15:12:05
 * description: 我是由代码生成器生成
 * version: 1.0.0
 */
public interface OrgUserInfoService extends IService<OrgUserInfo> {

    Page<OrgUserInfoVo> getPage(Long pageNum, Long pageSize, OrgUserInfoVo orgUserInfoVo);

    OrgUserInfoVo queryOrgUserInfo(Long id);

    Boolean addOrgUserInfo(OrgUserInfoVo orgUserInfoVo);

    Boolean updateOrgUserInfo(OrgUserInfoVo orgUserInfoVo);

    Boolean deleteOrgUserInfo(String ids);

    List<OrgInfoVo> getOrgInfoList(Long userId);
}
