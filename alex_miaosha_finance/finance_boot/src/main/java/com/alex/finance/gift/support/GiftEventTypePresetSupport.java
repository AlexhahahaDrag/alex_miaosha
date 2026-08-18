package com.alex.finance.gift.support;

import com.alex.api.finance.gift.event.vo.GiftEventTypeItemVo;
import com.alex.finance.gift.eventoption.mapper.GiftEventTypeOptionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GiftEventTypePresetSupport {

    private static final List<GiftEventTypeItemVo> FALLBACK_PRESETS = List.of(
            item(9100000000000000001L, "婚礼", "WEDDING", "婚庆类", "💍", new BigDecimal("1000.00"), 1),
            item(9100000000000000002L, "满月", "BIRTH", "家庭类", "👶", new BigDecimal("500.00"), 2),
            item(9100000000000000003L, "乔迁", "HOUSEWARMING", "家庭类", "🏡", new BigDecimal("800.00"), 3),
            item(9100000000000000004L, "升学", "EDUCATION", "家庭类", "🎓", new BigDecimal("600.00"), 4),
            item(9100000000000000005L, "寿宴", "BIRTHDAY", "家庭类", "🎂", new BigDecimal("500.00"), 5),
            item(9100000000000000006L, "其他", "OTHER", "其他", "💬", new BigDecimal("200.00"), 6),
            item(9100000000000000007L, "春节", "FESTIVAL_SPRING", "节日类", "🏮", new BigDecimal("1000.00"), 7),
            item(9100000000000000008L, "中秋", "FESTIVAL_MID_AUTUMN", "节日类", "🥮", new BigDecimal("500.00"), 8),
            item(9100000000000000009L, "端午", "FESTIVAL_DRAGON_BOAT", "节日类", "🛶", new BigDecimal("300.00"), 9),
            item(9100000000000000010L, "白事", "FUNERAL", "其他", "🕯️", new BigDecimal("500.00"), 10),
            item(9100000000000000011L, "感谢", "THANKS", "其他", "🙏", new BigDecimal("300.00"), 11),
            item(9100000000000000012L, "拜访", "VISIT", "其他", "🤝", new BigDecimal("200.00"), 12));

    private static final Set<String> FALLBACK_CODES = Set.of(
            "WEDDING", "BIRTH", "HOUSEWARMING", "EDUCATION", "BIRTHDAY", "OTHER",
            "FESTIVAL_SPRING", "FESTIVAL_MID_AUTUMN", "FESTIVAL_DRAGON_BOAT", "FUNERAL", "THANKS", "VISIT");

    private static final Set<String> FALLBACK_LABELS = Set.of(
            "婚礼", "满月", "乔迁", "升学", "寿宴", "其他", "春节", "中秋", "端午", "白事", "感谢", "拜访");

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

    private static GiftEventTypeItemVo item(Long id, String name, String eventCode, String category, String icon, BigDecimal defaultAmount, Integer sortOrder) {
        return new GiftEventTypeItemVo()
                .setId(id)
                .setName(name)
                .setEventCode(eventCode)
                .setCategory(category)
                .setIcon(icon)
                .setDefaultAmount(defaultAmount)
                .setSortOrder(sortOrder)
                .setStatus(1)
                .setUseCount(0);
    }
}
