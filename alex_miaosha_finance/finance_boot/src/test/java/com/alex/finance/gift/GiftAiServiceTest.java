package com.alex.finance.gift;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.api.finance.gift.event.vo.GiftRecordRecommendAmountVo;
import com.alex.api.finance.gift.record.query.GiftRecordAiParseReq;
import com.alex.api.finance.gift.record.vo.GiftRecordAiParseVo;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.base.common.Result;
import com.alex.finance.gift.ai.GiftAiService;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.eventoption.mapper.GiftEventTypeOptionMapper;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.personoption.mapper.GiftPersonRelationOptionMapper;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import com.alex.finance.gift.support.GiftEventTypePresetSupport;
import com.alex.finance.gift.support.GiftRelationPresetSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GiftAiServiceTest {

    @Mock
    private AiAnalyzeApi aiAnalyzeApi;
    @Mock
    private GiftPersonInfoMapper giftPersonInfoMapper;
    @Mock
    private GiftPersonRelationOptionMapper giftPersonRelationOptionMapper;
    @Mock
    private GiftEventTypeOptionMapper giftEventTypeOptionMapper;
    @Mock
    private GiftEventInfoMapper giftEventInfoMapper;
    @Mock
    private GiftRecordInfoMapper giftRecordInfoMapper;
    @Mock
    private GiftEventTypePresetSupport giftEventTypePresetSupport;
    @Mock
    private GiftRelationPresetSupport giftRelationPresetSupport;
    @Mock
    private GiftDataScopeSupport giftDataScopeSupport;

    private GiftAiService giftAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        giftAiService = new GiftAiService(
                aiAnalyzeApi,
                giftPersonInfoMapper,
                giftPersonRelationOptionMapper,
                giftEventTypeOptionMapper,
                giftEventInfoMapper,
                giftRecordInfoMapper,
                giftEventTypePresetSupport,
                giftRelationPresetSupport,
                giftDataScopeSupport,
                objectMapper
        );
    }

    @Test
    @DisplayName("AI记账解析：成功通过AI结构化抽取并完成默认填充")
    void testParseQuickLedgerWithAiSuccess() {
        TUserVo mockUser = new TUserVo();
        mockUser.setId(1001L);
        mockUser.setOrgId(2001L);
        when(giftDataScopeSupport.requireLoginUser()).thenReturn(mockUser);
        when(giftDataScopeSupport.loginOrgId(mockUser)).thenReturn(2001L);

        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setSummary("{\"personName\":\"王五\",\"relationName\":\"大学同学\",\"direction\":\"GIVE\",\"amount\":800.00,\"eventTypeName\":\"婚礼\"}");
        when(aiAnalyzeApi.chat(any(AiAnalyzeReq.class))).thenReturn(Result.success(resp));
        when(giftPersonInfoMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(giftPersonRelationOptionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(giftEventTypeOptionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(giftRelationPresetSupport.listSystemPresets()).thenReturn(Collections.emptyList());
        when(giftEventTypePresetSupport.listSystemPresets()).thenReturn(Collections.emptyList());

        GiftRecordAiParseReq req = new GiftRecordAiParseReq().setContent("随了大学同学王五结婚礼金800元");
        GiftRecordAiParseVo vo = giftAiService.parseQuickLedger(req);

        assertNotNull(vo);
        assertEquals("王五", vo.getPersonName());
        assertEquals(0, new BigDecimal("800.00").compareTo(vo.getAmount()));
        assertEquals("GIVE", vo.getDirection());
        assertTrue(vo.getIsNewPerson());
        assertNotNull(vo.getPayTime());
    }

    @Test
    @DisplayName("AI记账解析：当AI不可用时，走本地规则与正则抽取保底")
    void testParseQuickLedgerWithRegexFallback() {
        TUserVo mockUser = new TUserVo();
        mockUser.setId(1001L);
        when(giftDataScopeSupport.requireLoginUser()).thenReturn(mockUser);
        when(giftDataScopeSupport.loginOrgId(mockUser)).thenReturn(null);

        when(aiAnalyzeApi.chat(any(AiAnalyzeReq.class))).thenThrow(new RuntimeException("AI Feign service offline"));
        when(giftPersonInfoMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(giftPersonRelationOptionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(giftEventTypeOptionMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(giftRelationPresetSupport.listSystemPresets()).thenReturn(Collections.emptyList());
        when(giftEventTypePresetSupport.listSystemPresets()).thenReturn(Collections.emptyList());

        GiftRecordAiParseReq req = new GiftRecordAiParseReq().setContent("收到张三给的乔迁红包 1000 块");
        GiftRecordAiParseVo vo = giftAiService.parseQuickLedger(req);

        assertNotNull(vo);
        assertEquals("张三", vo.getPersonName());
        assertEquals(0, new BigDecimal("1000").compareTo(vo.getAmount()));
        assertEquals("RECEIVE", vo.getDirection());
        assertEquals("乔迁", vo.getEventTypeName());
    }

    @Test
    @DisplayName("智能礼金推荐：AI 生成推荐理由与祝福贺词")
    void testEnrichRecommendWithAi() {
        GiftRecordRecommendAmountVo vo = new GiftRecordRecommendAmountVo()
                .setDefaultAmount(new BigDecimal("600.00"))
                .setRecommendations(List.of(new BigDecimal("500.00"), new BigDecimal("600.00"), new BigDecimal("800.00")));

        AiAnalyzeResp resp = new AiAnalyzeResp();
        resp.setSummary("{\"aiReasoning\":\"推荐选用600元或800元吉利双数档位，既合乎当代社交礼节，亦显长久交情。\",\"aiGreetingTip\":\"新婚燕尔，百年好合！祝二位永结同心，白头偕老！\"}");
        when(aiAnalyzeApi.chat(any(AiAnalyzeReq.class))).thenReturn(Result.success(resp));

        giftAiService.enrichRecommendWithAi(vo, null, "WEDDING", "GIVE", 2001L, 1001L);

        assertTrue(vo.getAiReasoning().contains("吉利双数"));
        assertTrue(vo.getAiGreetingTip().contains("百年好合"));
    }

    @Test
    @DisplayName("智能礼金推荐：当AI降级时，本地人情规则与文化祝福语保底")
    void testEnrichRecommendWithFallback() {
        GiftRecordRecommendAmountVo vo = new GiftRecordRecommendAmountVo()
                .setDefaultAmount(new BigDecimal("800.00"))
                .setRecommendations(List.of(new BigDecimal("800.00"), new BigDecimal("1000.00")));

        when(aiAnalyzeApi.chat(any(AiAnalyzeReq.class))).thenThrow(new RuntimeException("AI service timeout"));

        giftAiService.enrichRecommendWithAi(vo, null, "HOUSEWARMING", "GIVE", 2001L, 1001L);

        assertNotNull(vo.getAiReasoning());
        assertTrue(vo.getAiReasoning().contains("推荐档位") || vo.getAiReasoning().contains("HOUSEWARMING"));
        assertNotNull(vo.getAiGreetingTip());
        assertTrue(vo.getAiGreetingTip().contains("乔迁大吉") || vo.getAiGreetingTip().contains("新居"));
    }
}
