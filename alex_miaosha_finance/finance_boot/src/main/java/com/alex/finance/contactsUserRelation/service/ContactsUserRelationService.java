package com.alex.finance.contactsUserRelation.service;

import com.alex.api.finance.contactsUserRelation.vo.ContactsUserRelationVo;
import com.alex.finance.contactsUserRelation.entity.ContactsUserRelation;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 联系人关系分类字典 服务类
 * author: alex
 * createDate: 2025-11-07 10:00:00
 * description: 联系人关系分类字典管理服务
 * version: 1.0.0
 * AI Agent
 */
public interface ContactsUserRelationService extends IService<ContactsUserRelation> {

	/**
	 * 获取关系分类分页列表（包括公共和用户的私有分类）
	 *
	 * @param pageNum 页码
	 * @param pageSize 每页大小
	 * @param userId 用户ID（可为空）
	 * @param vo 查询条件
	 * @return 分页结果
	 */
	Page<ContactsUserRelationVo> getPage(Long pageNum, Long pageSize, Long userId, ContactsUserRelationVo vo);

	/**
	 * 获取关系分类详情
	 *
	 * @param id 分类ID
	 * @return 分类详情
	 */
	ContactsUserRelationVo queryContactsUserRelation(Long id);

	/**
	 * 新增关系分类
	 *
	 * @param vo 分类信息
	 * @return 是否成功
	 */
	Boolean addContactsUserRelation(ContactsUserRelationVo vo);

	/**
	 * 修改关系分类
	 *
	 * @param vo 分类信息
	 * @return 是否成功
	 */
	Boolean updateContactsUserRelation(ContactsUserRelationVo vo);

	/**
	 * 删除关系分类
	 *
	 * @param ids 分类ID集合，多个ID用逗号分隔
	 * @return 是否成功
	 */
	Boolean deleteContactsUserRelation(String ids);

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
	List<ContactsUserRelationVo> queryEnabledRelationsByUser(Long userId);

}

