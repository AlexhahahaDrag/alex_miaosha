package com.alex.user.orgInfo.mapper;

import com.alex.api.user.annotation.DataPermission;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.user.orgInfo.entity.OrgInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * description:  机构表mapper
 * author:       alex
 * createDate:   2023-12-15 12:00:32
 * version:      1.0.0
 */
@Mapper
public interface OrgInfoMapper extends BaseMapper<OrgInfo> {

    @DataPermission(table = "t_org_info", field = "id", scope = DataPermission.Scope.ORG_ID)
    Page<OrgInfoVo> getPage(Page<OrgInfoVo> page, @Param("orgInfoVo") OrgInfoVo orgInfoVo);

    @DataPermission(table = "t_org_info", field = "id", scope = DataPermission.Scope.ORG_ID)
    OrgInfoVo queryOrgInfo(@Param("id") String id);
}
