package com.alex.finance.contactsUserRelation.service.impl;

import com.alex.api.finance.contactsUserRelation.vo.ContactsUserRelationVo;
import com.alex.api.finance.contactsUserRelation.vo.UserRoleSetVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.vo.roleInfo.RoleInfoVo;
import com.alex.api.user.vo.user.TUserVo;
import com.alex.common.utils.string.StringUtils;
import com.alex.finance.contactsUserRelation.entity.ContactsUserRelation;
import com.alex.finance.contactsUserRelation.mapper.ContactsUserRelationMapper;
import com.alex.finance.contactsUserRelation.service.ContactsUserRelationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * description:  联系人关系表服务实现类
 * author:       alex
 * createDate:   2025-11-07 10:00:00
 * version:      1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactsUserRelationServiceImpl extends ServiceImpl<ContactsUserRelationMapper, ContactsUserRelation> implements ContactsUserRelationService {

    private final ContactsUserRelationMapper contactsUserRelationMapper;

    private final UserUtils userUtils;

    @Override
    public Page<ContactsUserRelationVo> getPage(Long pageNum, Long pageSize, ContactsUserRelationVo vo) {
        Page<ContactsUserRelationVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        // 获取当前登录用户角色信息
        UserRoleSetVo userRoleInfo = getCurrentUserRoleInfo(vo == null ? null : vo.getUserId());
        return contactsUserRelationMapper.getPage(page, userRoleInfo.getUserId(), userRoleInfo.getRoleCode(), vo);
    }

    @Override
    public ContactsUserRelationVo queryContactsUserRelation(Long id) {
        if (id == null || id <= 0) {
            log.warn("关系ID无效: id={}", id);
            return null;
        }
        return contactsUserRelationMapper.queryContactsUserRelation(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addContactsUserRelation(ContactsUserRelationVo vo) {
        // 参数校验
        if (!validateContactsUserRelation(vo)) {
            log.warn("关系分类信息验证失败: {}", vo);
            return false;
        }
        // 检查是否已存在相同的标签（同一用户或公共标签）
        ContactsUserRelationVo existRelation = contactsUserRelationMapper.queryByRelationshipTag(
                vo.getRelationshipTag()
        );
        if (existRelation != null && (existRelation.getUserId() == null || (vo.getUserId() != null && existRelation.getUserId().equals(vo.getUserId())))) {
            log.warn("关系分类标签已存在: relationshipTag={}", vo.getRelationshipTag());
            throw new RuntimeException("该关系分类标签已存在!");
        }
        ContactsUserRelation relation = new ContactsUserRelation();
        BeanUtils.copyProperties(vo, relation);
        // 新增时默认启用
        if (relation.getIsEnabled() == null) {
            relation.setIsEnabled(1);
        }
        contactsUserRelationMapper.insert(relation);
        log.info("新增关系分类成功: id={}, relationshipTag={}", relation.getId(), relation.getRelationshipTag());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateContactsUserRelation(ContactsUserRelationVo vo) {
        // 参数校验
        if (vo.getId() == null || vo.getId() <= 0) {
            log.warn("分类ID无效: id={}", vo.getId());
            return false;
        }

        if (!validateContactsUserRelation(vo)) {
            log.warn("关系分类信息验证失败: {}", vo);
            return false;
        }
        ContactsUserRelation relation = new ContactsUserRelation();
        BeanUtils.copyProperties(vo, relation);
        contactsUserRelationMapper.updateById(relation);
        log.info("修改关系分类成功: id={}", relation.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteContactsUserRelation(String ids) {
        if (StringUtils.isEmpty(ids)) {
            log.warn("删除分类ID为空");
            return true;
        }

        List<String> idArr = Arrays.asList(ids.split(","));
        int deleteCount = contactsUserRelationMapper.deleteBatchIds(idArr);

        log.info("删除关系分类成功: 删除{}条记录", deleteCount);
        return true;
    }

    @Override
    public List<ContactsUserRelationVo> queryEnabledPublicRelations() {
        return contactsUserRelationMapper.queryEnabledPublicRelations();
    }

    @Override
    public List<ContactsUserRelationVo> queryEnabledRelationsByUser(Long userId) {
        // 获取当前登录用户角色信息
        UserRoleSetVo userRoleInfo = getCurrentUserRoleInfo(userId);
        return contactsUserRelationMapper.queryEnabledRelationsByUser(userRoleInfo.getUserId(), userRoleInfo.getRoleCode());
    }

    /**
     * 获取当前登录用户角色信息
     *
     * @param userId 用户id（可选，用于获取vo中的userId）
     * @return 用户角色信息（包含userId和roleCode）
     */
    private UserRoleSetVo getCurrentUserRoleInfo(Long userId) {
        TUserVo loginUser = userUtils.getLoginUser();
        RoleInfoVo roleInfoVo = loginUser != null ? loginUser.getRoleInfoVo() : null;
        String roleCode = roleInfoVo != null ? roleInfoVo.getRoleCode() : null;
        Long userIdNow = userId == null ? loginUser == null ? null : loginUser.getId() : userId;
        log.info("当前用户角色: userId={}, roleCode={}", userIdNow, roleCode);
        return new UserRoleSetVo(userIdNow, roleCode);
    }

    /**
     * 参数校验
     *
     * @param vo 关系分类信息
     * @return 是否有效
     */
    private boolean validateContactsUserRelation(ContactsUserRelationVo vo) {
        // 验证必填字段
        if (vo == null) {
            log.warn("关系分类对象为空");
            return false;
        }
        if (StringUtils.isEmpty(vo.getRelationshipTag())) {
            log.warn("关系标签为空");
            return false;
        }
        // 验证重要程度
        if (vo.getImportance() == null || vo.getImportance() < 1 || vo.getImportance() > 3) {
            log.warn("重要程度不合法: importance={}", vo.getImportance());
            return false;
        }
        // 验证关系标签长度
        if (vo.getRelationshipTag().length() > 100) {
            log.warn("关系标签过长: relationshipTag={}", vo.getRelationshipTag());
            return false;
        }
        // 验证描述长度
        if (vo.getDescription() != null && vo.getDescription().length() > 500) {
            log.warn("描述过长: description={}", vo.getDescription());
            return false;
        }
        // 验证备注长度
        if (vo.getRemarks() != null && vo.getRemarks().length() > 500) {
            log.warn("备注过长: remarks={}", vo.getRemarks());
            return false;
        }
        return true;
    }
}

