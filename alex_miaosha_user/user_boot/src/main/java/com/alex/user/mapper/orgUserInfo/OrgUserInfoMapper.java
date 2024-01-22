package com.alex.user.mapper.orgUserInfo;

import com.alex.api.user.vo.orgInfo.OrgInfoVo;
import com.alex.api.user.vo.orgUserInfo.OrgUserInfoVo;
import com.alex.user.entity.orgUserInfo.OrgUserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  用户公司信息表 mapper
 * author:       majf
 * createDate:   2024-01-15 15:12:05
 * version:      1.0.0
 */
@Mapper
public interface OrgUserInfoMapper extends BaseMapper<OrgUserInfo> {

    Page<OrgUserInfoVo> getPage(Page<OrgUserInfoVo> page, @Param("orgUserInfoVo") OrgUserInfoVo orgUserInfoVo);

    OrgUserInfoVo queryOrgUserInfo(@Param("id") Long id);

    List<OrgInfoVo> getOrgInfoList(@Param("userId") Long userId);
}
