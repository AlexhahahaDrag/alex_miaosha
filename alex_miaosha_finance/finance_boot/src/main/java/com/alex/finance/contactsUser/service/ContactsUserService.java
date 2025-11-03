package com.alex.finance.contactsUser.service;

import com.alex.api.finance.contactsUser.vo.ContactsUserVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alex.finance.contactsUser.entity.ContactsUser;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 联系人信息表 服务类
 * author: alex
 * createDate: 2025-11-03 10:01:28
 * description: 联系人信息管理服务
 * version: 1.0.0
 */
public interface ContactsUserService extends IService<ContactsUser> {

	/**
	 * 获取联系人分页列表
	 *
	 * @param pageNum 页码
	 * @param pageSize 每页大小
	 * @param contactsUserVo 查询条件
	 * @return 分页结果
	 */
	Page<ContactsUserVo> getPage(Long pageNum, Long pageSize, ContactsUserVo contactsUserVo);

	/**
	 * 获取联系人详情
	 *
	 * @param id 联系人ID
	 * @return 联系人详情
	 */
	ContactsUserVo queryContactsUser(Long id);

	/**
	 * 新增联系人
	 *
	 * @param contactsUserVo 联系人信息
	 * @return 是否成功
	 */
	Boolean addContactsUser(ContactsUserVo contactsUserVo);

	/**
	 * 修改联系人
	 *
	 * @param contactsUserVo 联系人信息
	 * @return 是否成功
	 */
	Boolean updateContactsUser(ContactsUserVo contactsUserVo);

	/**
	 * 删除联系人
	 *
	 * @param ids 联系人ID集合，多个ID用逗号分隔
	 * @return 是否成功
	 */
	Boolean deleteContactsUser(String ids);

	/**
	 * 导入联系人信息
	 *
	 * @param file 上传的文件
	 * @return 是否成功
	 * @throws Exception 异常
	 */
	Boolean importContactsUser(MultipartFile file) throws Exception;
}
