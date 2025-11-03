package com.alex.finance.contactsUser.controller;

import com.alex.api.finance.contactsUser.vo.ContactsUserVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.contactsUser.service.ContactsUserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSort;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * description:  联系人信息表restApi
 * author:       alex
 * createDate:   2025-11-03 10:01:28
 * version:      1.0.0
 */
@ApiSort(106)
@Api(value = "联系人信息表相关接口", tags = {"联系人信息表相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contacts-user")
public class ContactsUserController {

	private final ContactsUserService contactsUserService;

	@ApiOperationSupport(order = 10, author = "alex")
	@ApiOperation(value = "获取联系人信息表分页", notes = "获取联系人信息表分页", response = Result.class)
	@PostMapping(value = "/page")
	@ApiImplicitParams({
			@ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
			@ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
			@ApiImplicitParam(value = "查询条件", name = "contactsUserVo", dataTypeClass = ContactsUserVo.class)}
	)
	public Result<Page<ContactsUserVo>> getPage(@RequestParam(value = "pageNum", required = false) Long pageNum,
											   @RequestParam(value = "pageSize", required = false) Long pageSize,
											   @RequestBody(required = false) ContactsUserVo contactsUserVo) {
		return Result.success(contactsUserService.getPage(pageNum, pageSize, contactsUserVo));
	}

	@ApiOperationSupport(order = 20, author = "alex")
	@ApiOperation(value = "获取联系人信息表详情", notes = "获取联系人信息表详情", response = Result.class)
	@GetMapping
	public Result<ContactsUserVo> query(@RequestParam(value = "id") Long id) {
		return Result.success(contactsUserService.queryContactsUser(id));
	}

	@AvoidRepeatableCommit
	@ApiOperationSupport(order = 30, author = "alex")
	@ApiOperation(value = "新增联系人信息表", notes = "新增联系人信息表", response = Result.class)
	@PostMapping
	public Result<Boolean> add(@Validated({Insert.class}) @RequestBody ContactsUserVo contactsUserVo) {
		return Result.success(contactsUserService.addContactsUser(contactsUserVo));
	}

	@ApiOperationSupport(order = 40, author = "alex")
	@ApiOperation(value = "修改联系人信息表", notes = "修改联系人信息表", response = Result.class)
	@PutMapping
	public Result<Boolean> update(@Validated({Update.class}) @RequestBody ContactsUserVo contactsUserVo) {
		return Result.success(contactsUserService.updateContactsUser(contactsUserVo));
	}

	@ApiOperationSupport(order = 50, author = "alex")
	@ApiOperation(value = "删除联系人信息表", notes = "删除联系人信息表", response = Result.class)
	@DeleteMapping
	public Result<Boolean> delete(@RequestParam("ids") String ids) {
		return Result.success(contactsUserService.deleteContactsUser(ids));
	}

	@ApiOperationSupport(order = 60, author = "alex")
	@ApiOperation(value = "导入联系人信息", notes = "导入联系人信息", response = Result.class)
	@PostMapping(value = "/import")
	public Result<Boolean> importContactsUser(@RequestPart("file") MultipartFile file) throws Exception {
		return Result.success(contactsUserService.importContactsUser(file));
	}

}
