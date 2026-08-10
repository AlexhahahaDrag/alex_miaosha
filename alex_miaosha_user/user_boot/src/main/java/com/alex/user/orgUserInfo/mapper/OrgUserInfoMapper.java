package com.alex.user.orgUserInfo.mapper;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  用户公司信息表mapper
 * author:       majf
 * createDate:   2024-01-15 15:12:05
 * version:      1.0.0
 */
@Mapper
public interface OrgUserInfoMapper extends BaseMapper<OrgUserInfo> {

    @DataPermission(table = "t_org_user_info", field = "user_id", scope = DataPermission.Scope.USER_IDS)
    Page<OrgUserInfoVo> getPage(Page<OrgUserInfoVo> page, @Param("orgUserInfoVo") OrgUserInfoVo orgUserInfoVo);

    OrgUserInfoVo queryOrgUserInfo(@Param("id") Long id);

    List<OrgInfoVo> getOrgInfoList(@Param("userId") Long userId);
}
