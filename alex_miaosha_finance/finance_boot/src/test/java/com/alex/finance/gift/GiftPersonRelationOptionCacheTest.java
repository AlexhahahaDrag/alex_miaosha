package com.alex.finance.gift;

import com.alex.api.finance.gift.person.vo.GiftPersonRelationItemVo;
import com.alex.api.finance.gift.person.vo.GiftPersonRelationOptionRowVo;
import com.alex.api.finance.gift.person.vo.GiftPersonRelationOptionsVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.cache.multilevel.HotKeyDetector;
import com.alex.common.cache.multilevel.MultiLevelCacheProperties;
import com.alex.common.cache.multilevel.MultiLevelCacheService;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.personoption.entity.GiftPersonRelationOption;
import com.alex.finance.gift.personoption.mapper.GiftPersonRelationOptionMapper;
import com.alex.finance.gift.personoption.service.impl.GiftPersonRelationOptionServiceImp;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftOptionConstants;
import com.alex.finance.gift.support.GiftRelationPresetSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("礼尚往来亲友圈层字典多级缓存 (L1 Caffeine + L2) 接入与失效测试")
class GiftPersonRelationOptionCacheTest {

    private static final Long LOGIN_USER_ID = 1001L;
    private static final Long LOGIN_ORG_ID = 2001L;

    @Mock
    private GiftDataScopeSupport giftDataScopeSupport;

    @Mock
    private GiftPersonInfoMapper giftPersonInfoMapper;

    @Mock
    private GiftRelationPresetSupport giftRelationPresetSupport;

    @Mock
    private GiftPersonRelationOptionMapper optionMapper;

    private MultiLevelCacheService multiLevelCacheService;
    private GiftPersonRelationOptionServiceImp optionService;

    @BeforeEach
    void setUp() {
        MultiLevelCacheProperties properties = new MultiLevelCacheProperties();
        properties.setL1MaximumSize(1000L);
        properties.setL1DefaultTtlSeconds(300L);
        HotKeyDetector detector = new HotKeyDetector(properties);
        multiLevelCacheService = new MultiLevelCacheService(properties, null, detector);

        optionService = new GiftPersonRelationOptionServiceImp(
                giftDataScopeSupport,
                giftPersonInfoMapper,
                giftRelationPresetSupport,
                multiLevelCacheService
        );
        ReflectionTestUtils.setField(optionService, "baseMapper", optionMapper);

        TUserVo user = new TUserVo();
        user.setId(LOGIN_USER_ID);
        lenient().when(giftDataScopeSupport.requireLoginUser()).thenReturn(user);
        lenient().when(giftDataScopeSupport.isSuper(user)).thenReturn(false);
        lenient().when(giftDataScopeSupport.loginOrgId(user)).thenReturn(LOGIN_ORG_ID);
        lenient().when(giftRelationPresetSupport.ensurePresets(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("1. 首次查询回源数据库并写入多级缓存，二次查询直接命中缓存无需查库")
    void testListRelationOptionsMultiLevelCacheHit() {
        List<GiftPersonRelationOptionRowVo> mockRows = new ArrayList<>();
        GiftPersonRelationOptionRowVo row1 = new GiftPersonRelationOptionRowVo();
        row1.setId(1L);
        row1.setOptionType(GiftOptionConstants.OPTION_TYPE_SYSTEM);
        row1.setRelationLabel("亲属");
        mockRows.add(row1);

        when(optionMapper.listRelationOptionRows(LOGIN_USER_ID, LOGIN_ORG_ID, false)).thenReturn(mockRows);

        // 第一次调用：回源查询
        GiftPersonRelationOptionsVo result1 = optionService.listRelationOptions(null);
        assertNotNull(result1);
        assertEquals(1, result1.getPresets().size());
        verify(optionMapper, times(1)).listRelationOptionRows(LOGIN_USER_ID, LOGIN_ORG_ID, false);

        // 第二次调用：直接命中 L1 Caffeine 本地缓存，不再调用 optionMapper
        GiftPersonRelationOptionsVo result2 = optionService.listRelationOptions(null);
        assertNotNull(result2);
        assertEquals(1, result2.getPresets().size());
        verify(optionMapper, times(1)).listRelationOptionRows(LOGIN_USER_ID, LOGIN_ORG_ID, false);
    }

    @Test
    @DisplayName("2. 记住新关系后主动驱逐缓存，后续查询重新回源保证双写一致性")
    void testRememberCustomRelationEvictsCache() {
        List<GiftPersonRelationOptionRowVo> mockRows = new ArrayList<>();
        GiftPersonRelationOptionRowVo row1 = new GiftPersonRelationOptionRowVo();
        row1.setId(1L);
        row1.setOptionType(GiftOptionConstants.OPTION_TYPE_SYSTEM);
        row1.setRelationLabel("亲属");
        mockRows.add(row1);

        when(optionMapper.listRelationOptionRows(LOGIN_USER_ID, LOGIN_ORG_ID, false)).thenReturn(mockRows);
        when(giftRelationPresetSupport.isPresetCode(anyString())).thenReturn(false);
        when(giftRelationPresetSupport.isPresetLabel(anyString())).thenReturn(false);

        // 第一次调用：生成并缓存
        optionService.listRelationOptions(null);
        verify(optionMapper, times(1)).listRelationOptionRows(LOGIN_USER_ID, LOGIN_ORG_ID, false);

        // 模拟添加新关系触发缓存失效
        lenient().when(optionMapper.selectOne(any())).thenReturn(null);
        lenient().when(optionMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        lenient().when(optionMapper.insert(any(GiftPersonRelationOption.class))).thenReturn(1);
        optionService.rememberCustomRelation(LOGIN_USER_ID, LOGIN_ORG_ID, "表舅");

        // 第三次调用：由于已被 evict，必须重新触发回源查询
        optionService.listRelationOptions(null);
        verify(optionMapper, times(2)).listRelationOptionRows(LOGIN_USER_ID, LOGIN_ORG_ID, false);
    }
}
