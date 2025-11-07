package com.alex.finance.contactsUserRelation.mapper;

import com.alex.api.finance.contactsUserRelation.vo.ContactsUserRelationVo;
import com.alex.finance.contactsUserRelation.entity.ContactsUserRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * description:  联系人关系分类字典 Mapper
 * author:       alex
 * createDate:   2025-11-07 10:00:00
 * version:      1.0.0
 * AI Agent
 */
@Mapper
public interface ContactsUserRelationMapper extends BaseMapper<ContactsUserRelation> {

	/**
	 * 分页查询关系分类列表（包括公共和用户的私有分类）
	 * 
	 * @param page 分页参数
	 * @param userId 用户ID（可为空，表示只查公共）
	 * @param vo 查询条件
	 * @return 分页数据
	 */
	Page<ContactsUserRelationVo> getPage(Page<ContactsUserRelationVo> page, 
										@Param("userId") Long userId, 
										@Param("vo") ContactsUserRelationVo vo);

	/**
	 * 查询关系分类详情
	 * 
	 * @param id 分类ID
	 * @return 分类详情
	 */
	ContactsUserRelationVo queryContactsUserRelation(@Param("id") Long id);

	/**
	 * 根据关系标签查询
	 * 
	 * @param relationshipTag 关系标签
	 * @return 分类详情
	 */
	ContactsUserRelationVo queryByRelationshipTag(@Param("relationshipTag") String relationshipTag);

	/**
	 * 查询所有启用的公共关系分类
	 * 
	 * @return 分类列表
	 */
	List<ContactsUserRelationVo> queryEnabledPublicRelations();

	/**
	 * 查询用户的所有启用的关系分类（公共+私有）
	 * 
	 * @param userId 用户ID
	 * @return 分类列表
	 */
	List<ContactsUserRelationVo> queryEnabledRelationsByUser(@Param("userId") Long userId);
}

