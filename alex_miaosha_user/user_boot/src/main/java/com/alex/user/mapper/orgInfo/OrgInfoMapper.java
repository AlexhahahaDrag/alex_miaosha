package com.alex.user.mapper.orgInfo;

import com.alex.user.entity.orgInfo.OrgInfo;
import com.alex.api.user.vo.orgInfo.OrgInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description:  机构表 mapper
 * @author:       alex
 * @createDate:   2023-12-15 12:00:32
 * @version:      1.0.0
 */
@Mapper
public interface OrgInfoMapper extends BaseMapper<OrgInfo> {

    Page<OrgInfoVo> getPage(Page<OrgInfoVo> page, @Param("orgInfoVo") OrgInfoVo orgInfoVo);

    OrgInfoVo queryOrgInfo(@Param("id") String id);
}
