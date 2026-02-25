package com.alex.finance.contactsUserRelation.controller;

import com.alex.api.finance.contactsUserRelation.vo.ContactsUserRelationVo;
import com.alex.base.common.Result;
import com.alex.common.annotations.AvoidRepeatableCommit;
import com.alex.common.annotations.LogRestRequest;
import com.alex.common.validator.group.Insert;
import com.alex.common.validator.group.Update;
import com.alex.finance.contactsUserRelation.service.ContactsUserRelationService;
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

import java.util.List;

/**
 * description:  联系人关系分类字典restApi
 * author:       alex
 * createDate:   2025-11-07 10:00:00
 * version:      1.0.0
 */
@ApiSort(107)
@Api(value = "联系人关系分类字典相关接口", tags = {"联系人关系分类字典相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/contacts-user-relation")
public class ContactsUserRelationController {

	private final ContactsUserRelationService contactsUserRelationService;

	@LogRestRequest(apiName = "获取关系分类分页")
	@ApiOperationSupport(order = 10, author = "alex")
	@ApiOperation(value = "获取关系分类分页", notes = "获取关系分类分页，包括公共和用户私有分类", response = Result.class)
	@PostMapping(value = "/page")
	@ApiImplicitParams({
			@ApiImplicitParam(value = "页码", name = "pageNum", dataTypeClass = Integer.class),
			@ApiImplicitParam(value = "每页大小", name = "pageSize", dataTypeClass = Integer.class),
			@ApiImplicitParam(value = "用户ID（可选）", name = "userId", dataTypeClass = Long.class),
			@ApiImplicitParam(value = "查询条件", name = "vo", dataTypeClass = ContactsUserRelationVo.class)}
	)
	public Result<Page<ContactsUserRelationVo>> getPage(
			@RequestParam(value = "pageNum", required = false) Long pageNum,
			@RequestParam(value = "pageSize", required = false) Long pageSize,
			@RequestBody(required = false) ContactsUserRelationVo vo) {
		return Result.success(contactsUserRelationService.getPage(pageNum, pageSize, vo));
	}

	@LogRestRequest(apiName = "获取关系分类详情")
	@ApiOperationSupport(order = 20, author = "alex")
	@ApiOperation(value = "获取关系分类详情", notes = "获取关系分类详情", response = Result.class)
	@GetMapping
	public Result<ContactsUserRelationVo> query(@RequestParam(value = "id") Long id) {
		return Result.success(contactsUserRelationService.queryContactsUserRelation(id));
	}

	@AvoidRepeatableCommit
	@LogRestRequest(apiName = "新增关系分类")
	@ApiOperationSupport(order = 30, author = "alex")
	@ApiOperation(value = "新增关系分类", notes = "新增关系分类（公共或用户私有）", response = Result.class)
	@PostMapping
	public Result<Boolean> add(@Validated({Insert.class}) @RequestBody ContactsUserRelationVo vo) {
		return Result.success(contactsUserRelationService.addContactsUserRelation(vo));
	}

	@LogRestRequest(apiName = "修改关系分类")
	@ApiOperationSupport(order = 40, author = "alex")
	@ApiOperation(value = "修改关系分类", notes = "修改关系分类", response = Result.class)
	@PutMapping
	public Result<Boolean> update(@Validated({Update.class}) @RequestBody ContactsUserRelationVo vo) {
		return Result.success(contactsUserRelationService.updateContactsUserRelation(vo));
	}

	@LogRestRequest(apiName = "删除关系分类")
	@ApiOperationSupport(order = 50, author = "alex")
	@ApiOperation(value = "删除关系分类", notes = "删除关系分类", response = Result.class)
	@DeleteMapping
	public Result<Boolean> delete(@RequestParam("ids") String ids) {
		return Result.success(contactsUserRelationService.deleteContactsUserRelation(ids));
	}

	@LogRestRequest(apiName = "查询启用的公共关系分类")
	@ApiOperationSupport(order = 60, author = "alex")
	@ApiOperation(value = "查询启用的公共关系分类", notes = "查询所有启用的公共关系分类，用于下拉选择", response = Result.class)
	@GetMapping(value = "/public-enabled")
	public Result<List<ContactsUserRelationVo>> queryEnabledPublicRelations() {
		return Result.success(contactsUserRelationService.queryEnabledPublicRelations());
	}

	@LogRestRequest(apiName = "查询用户关系分类")
	@ApiOperationSupport(order = 70, author = "alex")
	@ApiOperation(value = "查询用户的启用关系分类", notes = "查询用户的所有启用关系分类（公共+私有）", response = Result.class)
	@GetMapping(value = "/user-enabled")
	public Result<List<ContactsUserRelationVo>> queryEnabledRelationsByUser(@RequestParam(value = "userId") Long userId) {
		return Result.success(contactsUserRelationService.queryEnabledRelationsByUser(userId));
	}

}

