package com.alex.user.orgUserInfo.service.impl;

import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.orgUserInfo.vo.OrgUserInfoVo;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.orgUserInfo.entity.OrgUserInfo;
import com.alex.user.orgUserInfo.mapper.OrgUserInfoMapper;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.rbac.service.PermissionContextCacheService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * description:  用户公司信息表服务实现类
 * author:       majf
 * createDate:   2024-01-15 15:12:05
 * version:      1.0.0
 */
@Service
@RequiredArgsConstructor
public class OrgUserInfoServiceImp extends ServiceImpl<OrgUserInfoMapper, OrgUserInfo> implements OrgUserInfoService {

    private final OrgUserInfoMapper orgUserInfoMapper;
    private final TransactionTemplate transactionTemplate;
    // RBAC-BE-RELATION-002: assign 成功后主动失效该用户的 permission_context 缓存
    private final PermissionContextCacheService permissionContextCacheService;
    private final Map<Long, Object> assignSingleOrgLocks = new ConcurrentHashMap<>();

    @Override
    public Page<OrgUserInfoVo> getPage(Long pageNum, Long pageSize, OrgUserInfoVo orgUserInfoVo) {
        Page<OrgUserInfoVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return orgUserInfoMapper.getPage(page, orgUserInfoVo);
    }

    @Override
    public OrgUserInfoVo queryOrgUserInfo(Long id) {
        return orgUserInfoMapper.queryOrgUserInfo(id);
    }

    @Override
    public Boolean addOrgUserInfo(OrgUserInfoVo orgUserInfoVo) {
        // I2 修复：新增用户机构关系口子已统一为「assign 有效机构」语义。
        // 若前端显式传入非有效状态（如失效），必须拒绝，不能静默地仍然按 assign 处理——
        // 否则运营以为自己只是新增了一条草稿/失效记录，实际却悄悄把该用户的有效机构范围改掉了。
        // status 为空/未传时视为"assign 有效机构"意图，保持原有行为不变。
        if (orgUserInfoVo != null && StringUtils.isNotEmpty(orgUserInfoVo.getStatus())
                && !SysConf.VALID_STATUS.equals(orgUserInfoVo.getStatus())) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "新增用户机构关系仅支持有效状态，如需失效请改用调岗接口");
        }
        // 公开写入口统一走「单用户唯一有效机构」语义，禁止裸插第二份有效关系
        return assignSingleOrg(
                requireUserId(orgUserInfoVo),
                requireOrgId(orgUserInfoVo),
                orgUserInfoVo.getSummary());
    }

    @Override
    public Boolean updateOrgUserInfo(OrgUserInfoVo orgUserInfoVo) {
        // 激活为有效时转调 assign，避免同一用户出现多条有效机构关系
        if (orgUserInfoVo != null && SysConf.VALID_STATUS.equals(orgUserInfoVo.getStatus())) {
            return activateViaAssign(orgUserInfoVo);
        }
        OrgUserInfo orgUserInfo = new OrgUserInfo();
        BeanUtils.copyProperties(orgUserInfoVo, orgUserInfo);
        orgUserInfoMapper.updateById(orgUserInfo);
        return true;
    }

    /**
     * I3 修复：编辑页把某条已存在的行（vo.getId()）从"失效"改为"有效"时，
     * assignSingleOrg 只会失效用户"当前处于有效状态"的旧记录、并总是插入一条全新的有效行——
     * 它并不知道也不会去动 vo.getId() 指向的这一行。
     * 如果被编辑的行本身此前就是失效状态（例如同一 (user, org) 的历史失效行），
     * assign 插入新有效行之后，这条旧的失效行会原样保留，造成同一 (user, org) 出现
     * 「一条失效 + 一条新有效」的重复数据，且旧行上编辑过的其他字段（如 summary）全部丢失。
     * 因此在 assign 完成后，显式清理这条与新有效行同属一个 (user, org) 的遗留失效行，
     * 保证激活后该 (user, org) 只剩一条记录。
     * 注意：assignSingleOrg/doAssignSingleOrg 已经会把该用户所有"当前有效"的旧记录一并失效，
     * 所以不会出现"插入新有效行的同时旧目标行仍为 status=1"的情况——这里只处理
     * "旧目标行本来就是失效状态、成为孤儿重复行"的场景。
     */
    private Boolean activateViaAssign(OrgUserInfoVo orgUserInfoVo) {
        Long userId = requireUserId(orgUserInfoVo);
        Long orgId = requireOrgId(orgUserInfoVo);
        Long editedId = orgUserInfoVo.getId();
        Boolean result = assignSingleOrg(userId, orgId, orgUserInfoVo.getSummary());
        if (editedId != null) {
            // 直接走 orgUserInfoMapper（与本类其它方法一致），不依赖 ServiceImpl#baseMapper，
            // 避免脱离 Spring 容器的纯单测环境中 baseMapper 未注入导致空指针。
            OrgUserInfo editedRow = orgUserInfoMapper.selectById(editedId);
            if (editedRow != null
                    && String.valueOf(userId).equals(editedRow.getUserId())
                    && String.valueOf(orgId).equals(editedRow.getOrgId())
                    && !SysConf.VALID_STATUS.equals(editedRow.getStatus())) {
                // 该行已经被 assign 新插入的有效行取代，物理删除以避免同一 (user, org) 残留重复记录
                orgUserInfoMapper.deleteById(editedId);
            }
        }
        return result;
    }

    private static Long requireUserId(OrgUserInfoVo vo) {
        if (vo == null || StringUtils.isEmpty(vo.getUserId())) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "用户机构分配参数错误");
        }
        return Long.valueOf(vo.getUserId());
    }

    private static Long requireOrgId(OrgUserInfoVo vo) {
        if (vo == null || StringUtils.isEmpty(vo.getOrgId())) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "用户机构分配参数错误");
        }
        return Long.valueOf(vo.getOrgId());
    }

    @Override
    public Boolean deleteOrgUserInfo(String ids) {
        if(StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        orgUserInfoMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public Boolean assignSingleOrg(Long userId, Long orgId) {
        return assignSingleOrg(userId, orgId, null);
    }

    /**
     * VO 驱动的 add/activate 路径携带 summary；其它调用方（如用户同步调岗）走两参数重载即可。
     */
    protected Boolean assignSingleOrg(Long userId, Long orgId, String summary) {
        if (userId == null || orgId == null) {
            throw new SystemException(ResultEnum.PARAM_ERROR, "用户机构分配参数错误:");
        }
        synchronized (assignSingleOrgLock(userId)) {
            return transactionTemplate.execute(status -> doAssignSingleOrg(userId, orgId, summary));
        }
    }

    protected Object assignSingleOrgLock(Long userId) {
        return assignSingleOrgLocks.computeIfAbsent(userId, key -> new Object());
    }

    private Boolean doAssignSingleOrg(Long userId, Long orgId, String summary) {
        List<OrgUserInfo> activeAssignments = list(Wrappers.<OrgUserInfo>lambdaQuery()
                .eq(OrgUserInfo::getUserId, String.valueOf(userId))
                .eq(OrgUserInfo::getStatus, SysConf.VALID_STATUS));
        for (OrgUserInfo orgUserInfo : activeAssignments) {
            orgUserInfo.setStatus(SysConf.INVALID_STATUS);
            if (!updateById(orgUserInfo)) {
                throw new SystemException(ResultEnum.SYSTEM_ERROR, "用户机构旧关系失效失败:");
            }
        }
        OrgUserInfo orgUserInfo = new OrgUserInfo();
        orgUserInfo.setUserId(String.valueOf(userId));
        orgUserInfo.setOrgId(String.valueOf(orgId));
        orgUserInfo.setStatus(SysConf.VALID_STATUS);
        if (summary != null) {
            orgUserInfo.setSummary(summary);
        }
        if (!save(orgUserInfo)) {
            throw new SystemException(ResultEnum.SYSTEM_ERROR, "用户机构新关系保存失败:");
        }
        // RBAC-BE-RELATION-002: 换机构成功后主动失效缓存，避免 1 小时 TTL 内数据权限仍按旧机构过滤
        permissionContextCacheService.invalidate(userId);
        return true;
    }

    @Override
    public List<OrgInfoVo> getOrgInfoList(Long userId) {
        return orgUserInfoMapper.getOrgInfoList(userId);
    }
}
