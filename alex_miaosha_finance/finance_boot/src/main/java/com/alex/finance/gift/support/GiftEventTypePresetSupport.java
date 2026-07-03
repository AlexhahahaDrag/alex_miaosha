package com.alex.finance.gift.support;

import com.alex.api.finance.gift.event.vo.GiftEventTypeItemVo;
import com.alex.finance.gift.eventoption.mapper.GiftEventTypeOptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GiftEventTypePresetSupport {

    private static final List<GiftEventTypeItemVo> FALLBACK_PRESETS = List.of(
            item(9100000000000000001L, "婚礼"),
            item(9100000000000000002L, "满月"),
            item(9100000000000000003L, "乔迁"),
            item(9100000000000000004L, "升学"),
            item(9100000000000000005L, "寿宴"),
            item(9100000000000000006L, "其他"));

    private static final Set<String> FALLBACK_CODES = Set.of(
            "WEDDING", "BIRTH", "HOUSEWARMING", "EDUCATION", "BIRTHDAY", "OTHER");

    private static final Set<String> FALLBACK_LABELS = Set.of(
            "婚礼", "满月", "乔迁", "升学", "寿宴", "其他");

    private final GiftEventTypeOptionMapper giftEventTypeOptionMapper;

    public List<GiftEventTypeItemVo> ensurePresets(List<GiftEventTypeItemVo> presets) {
        return presets == null || presets.isEmpty() ? FALLBACK_PRESETS : presets;
    }

    public List<GiftEventTypeItemVo> listSystemPresets() {
        List<GiftEventTypeItemVo> presets = giftEventTypeOptionMapper.listSystemPresets();
        return presets == null || presets.isEmpty() ? FALLBACK_PRESETS : presets;
    }

    public boolean isPresetCode(String eventType) {
        if (eventType == null) {
            return false;
        }
        if (FALLBACK_CODES.contains(eventType)) {
            return true;
        }
        return giftEventTypeOptionMapper.countSystemByEventCode(eventType) > 0;
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

    private static GiftEventTypeItemVo item(Long id, String name) {
        return new GiftEventTypeItemVo().setId(id).setName(name);
    }
}
