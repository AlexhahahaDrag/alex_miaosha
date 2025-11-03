package com.alex.finance.contactsUser.mapper;

import com.alex.api.finance.contactsUser.vo.CheckContactsVo;
import com.alex.api.finance.contactsUser.vo.ContactsUserVo;
import com.alex.api.user.annotation.DataPermission;
import com.alex.finance.contactsUser.entity.ContactsUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * description:  联系人信息表 Mapper
 * author:       alex
 * createDate:   2025-11-03 10:01:28
 * version:      1.0.0
 */
@Mapper
public interface ContactsUserMapper extends BaseMapper<ContactsUser> {

	@DataPermission(table = "t_contacts_user")
	Page<ContactsUserVo> getPage(Page<ContactsUserVo> page, @Param("contactsUserVo") ContactsUserVo contactsUserVo);

	ContactsUserVo queryContactsUser(@Param("id") Long id);

	/**
	 * 查询重复的名称和电话
	 * 返回 SQL 查询结果的 Map，包含 nameCount 和 phoneCount
	 *
	 * @param name 联系人名称
	 * @param phone 联系电话
	 * @param excludeId 排除的ID（可选，为 null 时表示不排除任何ID）
	 * @return 查询结果 Map，包含 nameCount 和 phoneCount 字段
	 */
	CheckContactsVo checkDuplicate(@Param("name") String name,
                                   @Param("phone") String phone,
                                   @Param("excludeId") Long excludeId
	);
}
