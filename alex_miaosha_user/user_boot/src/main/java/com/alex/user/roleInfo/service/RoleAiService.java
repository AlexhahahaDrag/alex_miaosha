package com.alex.user.roleInfo.service;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.api.user.menuInfo.vo.MenuInfoVo;
import com.alex.api.user.roleInfo.vo.RoleAiRecommendReq;
import com.alex.api.user.roleInfo.vo.RoleAiRecommendVo;
import com.alex.base.common.Result;
import com.alex.user.menuInfo.service.MenuInfoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 岗位角色权限 AI 智能推荐服务
 */
@Slf4j
@Service
public class RoleAiService {

    private final MenuInfoService menuInfoService;
    private final AiAnalyzeApi aiAnalyzeApi;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RoleAiService(MenuInfoService menuInfoService,
                           @Autowired(required = false) AiAnalyzeApi aiAnalyzeApi) {
        this.menuInfoService = menuInfoService;
        this.aiAnalyzeApi = aiAnalyzeApi;
    }

    public RoleAiRecommendVo recommendPermissions(RoleAiRecommendReq req) {
        List<MenuInfoVo> allMenus = Collections.emptyList();
        try {
            allMenus = menuInfoService.getList(new MenuInfoVo());
        } catch (Exception ex) {
            log.warn("Failed to get menu list for AI recommendation: {}", ex.getMessage());
        }

        RoleAiRecommendVo fallbackVo = buildFallbackRecommendation(req, allMenus);

        if (aiAnalyzeApi == null || allMenus.isEmpty()) {
            return fallbackVo;
        }

        try {
            List<Map<String, Object>> menuCandidates = allMenus.stream().map(m -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", String.valueOf(m.getId()));
                map.put("name", m.getName());
                map.put("title", m.getTitle());
                map.put("permissionCode", m.getPermissionCode());
                map.put("parentId", m.getParentId() != null ? String.valueOf(m.getParentId()) : null);
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> ctx = new HashMap<>();
            ctx.put("roleName", req != null ? req.getRoleName() : null);
            ctx.put("roleCode", req != null ? req.getRoleCode() : null);
            ctx.put("description", req != null ? req.getDescription() : null);

            String prompt = String.format(
                    "作为企业级 RBAC 权限与组织架构管理专家，请根据以下角色职责和岗位信息，从系统现有菜单/权限列表中精选出该角色最应该拥有的菜单与按钮权限：\n" +
                    "- 角色名称：%s\n" +
                    "- 角色编码：%s\n" +
                    "- 职责描述/工作范围：%s\n" +
                    "- 系统现有菜单候选列表(JSON)：%s\n" +
                    "请以标准 JSON 格式输出，包含以下字段：\n" +
                    "recommendedMenuIds(字符串ID数组),\n" +
                    "recommendedMenuNames(菜单名称数组),\n" +
                    "recommendedPermissionCodes(权限编码数组),\n" +
                    "reasoning(岗位职责匹配理由分析，100字左右)。\n" +
                    "只需返回纯 JSON，不要 markdown 格式代码块。",
                    (req != null && StringUtils.hasText(req.getRoleName())) ? req.getRoleName() : "通用业务岗位",
                    (req != null && StringUtils.hasText(req.getRoleCode())) ? req.getRoleCode() : "GENERAL_ROLE",
                    (req != null && StringUtils.hasText(req.getDescription())) ? req.getDescription() : "日常业务与系统功能操作",
                    objectMapper.writeValueAsString(menuCandidates)
            );

            AiAnalyzeReq aiReq = new AiAnalyzeReq();
            aiReq.setBizType("rbac-role-ai-recommend");
            aiReq.setContent(prompt);
            aiReq.setContext(ctx);

            Result<AiAnalyzeResp> result = aiAnalyzeApi.chat(aiReq);
            if (result != null && result.getData() != null && StringUtils.hasText(result.getData().getSummary())) {
                String text = result.getData().getSummary().trim();
                if (text.startsWith("```json")) {
                    text = text.substring(7);
                } else if (text.startsWith("```")) {
                    text = text.substring(3);
                }
                if (text.endsWith("```")) {
                    text = text.substring(0, text.length() - 3);
                }
                text = text.trim();
                JsonNode root = objectMapper.readTree(text);
                if (root != null) {
                    RoleAiRecommendVo aiVo = new RoleAiRecommendVo();
                    List<String> ids = new ArrayList<>();
                    if (root.has("recommendedMenuIds") && root.get("recommendedMenuIds").isArray()) {
                        for (JsonNode idNode : root.get("recommendedMenuIds")) {
                            if (idNode != null && StringUtils.hasText(idNode.asText())) {
                                ids.add(idNode.asText());
                            }
                        }
                    }
                    List<String> names = new ArrayList<>();
                    if (root.has("recommendedMenuNames") && root.get("recommendedMenuNames").isArray()) {
                        for (JsonNode nameNode : root.get("recommendedMenuNames")) {
                            if (nameNode != null && StringUtils.hasText(nameNode.asText())) {
                                names.add(nameNode.asText());
                            }
                        }
                    }
                    List<String> perms = new ArrayList<>();
                    if (root.has("recommendedPermissionCodes") && root.get("recommendedPermissionCodes").isArray()) {
                        for (JsonNode permNode : root.get("recommendedPermissionCodes")) {
                            if (permNode != null && StringUtils.hasText(permNode.asText())) {
                                perms.add(permNode.asText());
                            }
                        }
                    }
                    String reason = root.hasNonNull("reasoning") ? root.get("reasoning").asText() : fallbackVo.getReasoning();

                    if (!ids.isEmpty()) {
                        // 确保包含选中的所有父级节点ID
                        Set<String> expandedIds = expandParentIds(ids, allMenus);
                        aiVo.setRecommendedMenuIds(new ArrayList<>(expandedIds));
                        aiVo.setRecommendedMenuNames(names);
                        aiVo.setRecommendedPermissionCodes(perms);
                        aiVo.setReasoning(reason);
                        return aiVo;
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("RoleAiService AI recommendation degraded to fallback heuristics: {}", ex.getMessage());
        }

        return fallbackVo;
    }

    public RoleAiRecommendVo buildFallbackRecommendation(RoleAiRecommendReq req, List<MenuInfoVo> allMenus) {
        String roleName = req != null && req.getRoleName() != null ? req.getRoleName().toLowerCase() : "";
        String desc = req != null && req.getDescription() != null ? req.getDescription().toLowerCase() : "";
        String targetText = roleName + " " + desc;

        List<String> keywords = new ArrayList<>();
        if (targetText.contains("财务") || targetText.contains("礼金") || targetText.contains("账") || targetText.contains("finance") || targetText.contains("gift")) {
            keywords.addAll(Arrays.asList("财务", "礼金", "礼尚往来", "优惠券", "记账", "finance", "gift"));
        }
        if (targetText.contains("商品") || targetText.contains("秒杀") || targetText.contains("产品") || targetText.contains("product") || targetText.contains("seckill")) {
            keywords.addAll(Arrays.asList("商品", "秒杀", "品", "product", "seckill"));
        }
        if (targetText.contains("用户") || targetText.contains("员工") || targetText.contains("组织") || targetText.contains("机构") || targetText.contains("角色") || targetText.contains("user") || targetText.contains("role")) {
            keywords.addAll(Arrays.asList("用户", "角色", "机构", "组织", "user", "role", "org", "权限"));
        }
        if (targetText.contains("系统") || targetText.contains("管理员") || targetText.contains("运维") || targetText.contains("admin") || targetText.contains("root")) {
            keywords.addAll(Arrays.asList("系统", "配置", "字典", "日志", "system"));
        }

        Set<String> matchedIds = new LinkedHashSet<>();
        List<String> matchedNames = new ArrayList<>();
        List<String> matchedPerms = new ArrayList<>();

        Map<Long, MenuInfoVo> menuMap = new HashMap<>();
        if (allMenus != null) {
            for (MenuInfoVo m : allMenus) {
                if (m.getId() != null) {
                    menuMap.put(m.getId(), m);
                }
            }

            for (MenuInfoVo menu : allMenus) {
                String menuText = (menu.getTitle() != null ? menu.getTitle() : "") + " " +
                                  (menu.getName() != null ? menu.getName() : "") + " " +
                                  (menu.getPermissionCode() != null ? menu.getPermissionCode() : "");
                boolean match = false;
                if (keywords.isEmpty()) {
                    // 没有明确关键词时，默认开放基础查看类菜单
                    if (menuText.contains("查看") || menuText.contains("列表") || menuText.contains("首页") || menuText.contains("dashboard")) {
                        match = true;
                    }
                } else {
                    for (String kw : keywords) {
                        if (menuText.toLowerCase().contains(kw.toLowerCase())) {
                            match = true;
                            break;
                        }
                    }
                }

                if (match && menu.getId() != null) {
                    matchedIds.add(String.valueOf(menu.getId()));
                    if (StringUtils.hasText(menu.getTitle())) {
                        matchedNames.add(menu.getTitle());
                    } else if (StringUtils.hasText(menu.getName())) {
                        matchedNames.add(menu.getName());
                    }
                    if (StringUtils.hasText(menu.getPermissionCode())) {
                        matchedPerms.add(menu.getPermissionCode());
                    }
                }
            }
        }

        // 补全父节点
        Set<String> expandedIds = expandParentIds(new ArrayList<>(matchedIds), allMenus);

        String reasoning;
        if (!matchedIds.isEmpty()) {
            reasoning = String.format("已依据角色【%s】的职责描述，自动推导关键词并匹配了 %d 项最契合的业务功能与操作权限，遵循最小特权与职责分离原则。",
                    req != null && StringUtils.hasText(req.getRoleName()) ? req.getRoleName() : "指定角色",
                    matchedIds.size());
        } else {
            reasoning = "未找到精确匹配的功能权限，建议根据具体业务需求在下方权限树中按需勾选。";
        }

        return new RoleAiRecommendVo()
                .setRecommendedMenuIds(new ArrayList<>(expandedIds))
                .setRecommendedMenuNames(matchedNames)
                .setRecommendedPermissionCodes(matchedPerms)
                .setReasoning(reasoning);
    }

    private Set<String> expandParentIds(List<String> initialIds, List<MenuInfoVo> allMenus) {
        Set<String> result = new LinkedHashSet<>(initialIds);
        if (allMenus == null || allMenus.isEmpty()) {
            return result;
        }

        Map<Long, Long> parentMap = new HashMap<>();
        for (MenuInfoVo m : allMenus) {
            if (m.getId() != null && m.getParentId() != null) {
                parentMap.put(m.getId(), m.getParentId());
            }
        }

        for (String idStr : initialIds) {
            try {
                Long curId = Long.parseLong(idStr);
                while (parentMap.containsKey(curId)) {
                    Long pId = parentMap.get(curId);
                    if (pId != null && pId != 0L) {
                        result.add(String.valueOf(pId));
                        curId = pId;
                    } else {
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return result;
    }
}
