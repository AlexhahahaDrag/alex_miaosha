package com.alex.finance.gift;

import com.alex.api.finance.gift.person.query.GiftPersonQuery;
import com.alex.api.finance.gift.person.vo.GiftPersonBusinessVo;
import com.alex.api.finance.gift.person.vo.GiftPersonProfileVo;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.finance.gift.summary.vo.GiftPersonBalanceVo;
import com.alex.api.finance.gift.summary.vo.GiftReturnRecommendationReq;
import com.alex.api.finance.gift.summary.vo.GiftReturnRecommendationVo;
import com.alex.common.exception.SystemException;
import com.alex.common.utils.date.DateUtils;
import com.alex.finance.gift.analysis.service.impl.GiftAnalysisServiceImpl;
import com.alex.finance.gift.event.service.GiftEventInfoService;
import com.alex.finance.gift.person.service.GiftPersonInfoService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftRecordConstants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("人情往来智能分析 - 互赠收支平衡矩阵与AI回礼推荐单测")
class GiftAnalysisBalanceMatrixTest {

    @Mock
    private GiftRecordInfoService giftRecordInfoService;

    @Mock
    private GiftEventInfoService giftEventInfoService;

    @Mock
    private GiftPersonInfoService giftPersonInfoService;

    private GiftAnalysisServiceImpl giftAnalysisService;

    @BeforeEach
    void setUp() {
        giftAnalysisService = new GiftAnalysisServiceImpl(
                giftRecordInfoService,
                giftEventInfoService,
                giftPersonInfoService
        );
    }

    @Test
    @DisplayName("balanceMatrix: 准确核算双向差额，正确标记 SURPLUS, DEFICIT, BALANCED 与大额失衡预警")
    void testBalanceMatrixCalculationAndStatus() {
        LocalDateTime twoHundredDaysAgo = DateUtils.now().minusDays(200);
        LocalDateTime tenDaysAgo = DateUtils.now().minusDays(10);

        GiftPersonBusinessVo person1 = new GiftPersonBusinessVo();
        person1.setId(101L);
        person1.setPersonName("表哥张伟");
        person1.setRelationType("relatives");
        person1.setTotalReceiveAmount(new BigDecimal("1500.00"));
        person1.setTotalGiveAmount(new BigDecimal("500.00"));
        person1.setLatestRecordTime(twoHundredDaysAgo); // 超过 180 天

        GiftPersonBusinessVo person2 = new GiftPersonBusinessVo();
        person2.setId(102L);
        person2.setPersonName("同事李雷");
        person2.setRelationType("colleague");
        person2.setTotalReceiveAmount(new BigDecimal("200.00"));
        person2.setTotalGiveAmount(new BigDecimal("800.00"));
        person2.setLatestRecordTime(tenDaysAgo);

        GiftPersonBusinessVo person3 = new GiftPersonBusinessVo();
        person3.setId(103L);
        person3.setPersonName("同学韩梅梅");
        person3.setRelationType("classmate");
        person3.setTotalReceiveAmount(new BigDecimal("600.00"));
        person3.setTotalGiveAmount(new BigDecimal("500.00")); // 差额 100 <= 200
        person3.setLatestRecordTime(tenDaysAgo);

        Page<GiftPersonBusinessVo> page = new Page<>(1, 1000);
        page.setRecords(List.of(person1, person2, person3));

        when(giftPersonInfoService.getBusinessPage(anyLong(), anyLong(), any(GiftPersonQuery.class)))
                .thenReturn(page);

        List<GiftPersonBalanceVo> matrix = giftAnalysisService.balanceMatrix(null, null);

        assertNotNull(matrix);
        assertEquals(3, matrix.size());

        // Person 1: net = +1000, SURPLUS, 满足 >=800 且超 180 天 -> isImbalancedAlert = true
        GiftPersonBalanceVo vo1 = matrix.stream().filter(v -> v.getPersonId().equals(101L)).findFirst().orElseThrow();
        assertEquals(new BigDecimal("1000.00"), vo1.getNetBalance());
        assertEquals("SURPLUS", vo1.getBalanceStatus());
        assertEquals("亲戚", vo1.getRelationTypeName());
        assertTrue(vo1.getIsImbalancedAlert());
        assertNotNull(vo1.getAlertReason());
        assertTrue(vo1.getAlertReason().contains("1000.00"));

        // Person 2: net = -600, DEFICIT
        GiftPersonBalanceVo vo2 = matrix.stream().filter(v -> v.getPersonId().equals(102L)).findFirst().orElseThrow();
        assertEquals(new BigDecimal("-600.00"), vo2.getNetBalance());
        assertEquals("DEFICIT", vo2.getBalanceStatus());
        assertEquals("同事", vo2.getRelationTypeName());
        assertFalse(vo2.getIsImbalancedAlert());

        // Person 3: net = +100, BALANCED
        GiftPersonBalanceVo vo3 = matrix.stream().filter(v -> v.getPersonId().equals(103L)).findFirst().orElseThrow();
        assertEquals(new BigDecimal("100.00"), vo3.getNetBalance());
        assertEquals("BALANCED", vo3.getBalanceStatus());
        assertEquals("同学", vo3.getRelationTypeName());
        assertFalse(vo3.getIsImbalancedAlert());
    }

    @Test
    @DisplayName("balanceMatrix: 空数据与异常值安全防御")
    void testBalanceMatrixEmptySafety() {
        Page<GiftPersonBusinessVo> emptyPage = new Page<>(1, 1000);
        emptyPage.setRecords(Collections.emptyList());

        when(giftPersonInfoService.getBusinessPage(anyLong(), anyLong(), any(GiftPersonQuery.class)))
                .thenReturn(emptyPage);

        List<GiftPersonBalanceVo> matrix = giftAnalysisService.balanceMatrix("all", "不存在");
        assertNotNull(matrix);
        assertTrue(matrix.isEmpty());
    }

    @Test
    @DisplayName("recommendReturnGift: 亲友不存在或参数为空时抛出参数异常")
    void testRecommendReturnGiftValidation() {
        assertThrows(SystemException.class, () -> giftAnalysisService.recommendReturnGift(null));

        GiftReturnRecommendationReq emptyReq = new GiftReturnRecommendationReq();
        assertThrows(SystemException.class, () -> giftAnalysisService.recommendReturnGift(emptyReq));

        emptyReq.setPersonId(9999L);
        when(giftPersonInfoService.getProfile(9999L)).thenReturn(null);
        assertThrows(SystemException.class, () -> giftAnalysisService.recommendReturnGift(emptyReq));
    }

    @Test
    @DisplayName("recommendReturnGift: 结婚事件与近亲加权，向上匹配吉利数字阶梯")
    void testRecommendReturnGiftWeddingRelative() {
        Long personId = 888L;
        GiftPersonProfileVo profile = new GiftPersonProfileVo();
        GiftPersonBusinessVo person = new GiftPersonBusinessVo();
        person.setId(personId);
        person.setPersonName("堂姐王芳");
        person.setRelationType("close_relative");
        profile.setPerson(person);

        GiftRecordInfoVo record1 = new GiftRecordInfoVo();
        record1.setDirection(GiftRecordConstants.DIRECTION_RECEIVE);
        record1.setAmount(new BigDecimal("800.00")); // 历史收礼 800
        profile.setRecords(List.of(record1));

        when(giftPersonInfoService.getProfile(personId)).thenReturn(profile);

        GiftReturnRecommendationReq req = new GiftReturnRecommendationReq()
                .setPersonId(personId)
                .setEventType("WEDDING");

        GiftReturnRecommendationVo vo = giftAnalysisService.recommendReturnGift(req);

        assertNotNull(vo);
        assertEquals("堂姐王芳", vo.getPersonName());
        assertEquals("近亲属", vo.getRelationTypeName());
        assertEquals(new BigDecimal("800.00"), vo.getHistoricalBaseAmount());
        // 目标金额 = 800 * 1.2 (结婚) * 1.3 (近亲) = 1248 -> 匹配吉利档位 1288
        assertEquals(new BigDecimal("1288"), vo.getRecommendedAmount());
        assertEquals(new BigDecimal("1200"), vo.getMinDecentAmount());
        assertEquals(new BigDecimal("1688"), vo.getGenerousAmount());
        assertTrue(vo.getBlessingWords().contains("新婚快乐"));
        assertTrue(vo.getRuleSummary().contains("堂姐王芳"));
        assertNotNull(vo.getAuspiciousTierList());
    }

    @Test
    @DisplayName("recommendReturnGift: 白事事由与同事关系，系数下调且输出慰问话术")
    void testRecommendReturnGiftFuneralColleague() {
        Long personId = 777L;
        GiftPersonProfileVo profile = new GiftPersonProfileVo();
        GiftPersonBusinessVo person = new GiftPersonBusinessVo();
        person.setId(personId);
        person.setPersonName("同事小刘");
        person.setRelationType("colleague");
        person.setTotalReceiveAmount(new BigDecimal("500.00"));
        profile.setPerson(person);
        profile.setRecords(Collections.emptyList());

        when(giftPersonInfoService.getProfile(personId)).thenReturn(profile);

        GiftReturnRecommendationReq req = new GiftReturnRecommendationReq()
                .setPersonId(personId)
                .setEventType("FUNERAL");

        GiftReturnRecommendationVo vo = giftAnalysisService.recommendReturnGift(req);

        assertNotNull(vo);
        assertEquals("同事", vo.getRelationTypeName());
        // 目标金额 = 500 * 0.85 (白事) * 1.0 (同事) = 425 -> 匹配吉利档位 500
        assertEquals(new BigDecimal("500"), vo.getRecommendedAmount());
        assertEquals(new BigDecimal("200"), vo.getMinDecentAmount());
        assertEquals(new BigDecimal("600"), vo.getGenerousAmount());
        assertTrue(vo.getBlessingWords().contains("节哀顺变"));
    }
}
