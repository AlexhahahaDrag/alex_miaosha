package com.alex.finance.gift.support;

import com.alex.api.finance.gift.person.vo.GiftPersonRelationItemVo;
import com.alex.finance.gift.personoption.mapper.GiftPersonRelationOptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统预设关系：优先读库，库空时回退内置种子。
 */
@Component
@RequiredArgsConstructor
public class GiftRelationPresetSupport {

    private static final List<GiftPersonRelationItemVo> FALLBACK_PRESETS = List.of(
            item(9000000000000000001L, "亲属"),
            item(9000000000000000002L, "朋友"),
            item(9000000000000000003L, "同事"),
            item(9000000000000000004L, "邻里"),
            item(9000000000000000005L, "其他"));

    private static final Set<String> FALLBACK_CODES = Set.of(
            "RELATIVE", "FRIEND", "COLLEAGUE", "NEIGHBOR", "OTHER");

    private static final Set<String> FALLBACK_LABELS = Set.of(
            "亲属", "朋友", "同事", "邻里", "其他");

    private final GiftPersonRelationOptionMapper giftPersonRelationOptionMapper;

    public List<GiftPersonRelationItemVo> ensurePresets(List<GiftPersonRelationItemVo> presets) {
        return presets == null || presets.isEmpty() ? FALLBACK_PRESETS : presets;
    }

    public List<GiftPersonRelationItemVo> listSystemPresets() {
        List<GiftPersonRelationItemVo> presets = giftPersonRelationOptionMapper.listSystemPresets();
        return presets == null || presets.isEmpty() ? FALLBACK_PRESETS : presets;
    }

    public boolean isPresetCode(String relationType) {
        if (relationType == null) {
            return false;
        }
        if (FALLBACK_CODES.contains(relationType)) {
            return true;
        }
        return giftPersonRelationOptionMapper.countSystemByRelationCode(relationType) > 0;
    }

    public boolean isPresetLabel(String label) {
        if (label == null) {
            return false;
        }
        String trimmed = label.trim();
        if (FALLBACK_LABELS.contains(trimmed)) {
            return true;
        }
        return listSystemPresets().stream()
                .anyMatch(item -> trimmed.equals(item.getName()));
    }

    public Set<String> presetCodes() {
        return giftPersonRelationOptionMapper.listSystemPresets().stream()
                .map(item -> resolvePresetCode(item.getName()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static String resolvePresetCode(String name) {
        return switch (name) {
            case "亲属" -> "RELATIVE";
            case "朋友" -> "FRIEND";
            case "同事" -> "COLLEAGUE";
            case "邻里" -> "NEIGHBOR";
            case "其他" -> "OTHER";
            default -> name;
        };
    }

    private static GiftPersonRelationItemVo item(Long id, String name) {
        return new GiftPersonRelationItemVo().setId(id).setName(name);
    }
}
