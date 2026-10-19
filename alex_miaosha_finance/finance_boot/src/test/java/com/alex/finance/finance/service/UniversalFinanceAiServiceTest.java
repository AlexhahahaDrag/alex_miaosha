package com.alex.finance.finance.service;

import com.alex.api.ai.api.AiAnalyzeApi;
import com.alex.api.finance.vo.finance.UniversalFinanceAiParseReq;
import com.alex.api.finance.vo.finance.UniversalFinanceAiParseVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.finance.finance.service.impl.UniversalFinanceAiServiceImpl;
import com.alex.finance.prepaidCardInfoT.entity.PrepaidCardInfoT;
import com.alex.finance.prepaidCardInfoT.mapper.PrepaidCardInfoTMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * UniversalFinanceAiService 单元测试 (覆盖日常记账、小票多单品、预付卡匹配与 100% 本地规则降级)
 */
@ExtendWith(MockitoExtension.class)
class UniversalFinanceAiServiceTest {

    @InjectMocks
    private UniversalFinanceAiServiceImpl universalFinanceAiService;

    @Mock
    private AiAnalyzeApi aiAnalyzeApi;

    @Mock
    private UserUtils userUtils;

    @Mock
    private PrepaidCardInfoTMapper prepaidCardInfoTMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(universalFinanceAiService, "aiAnalyzeApi", aiAnalyzeApi);
        ReflectionTestUtils.setField(universalFinanceAiService, "userUtils", userUtils);
        ReflectionTestUtils.setField(universalFinanceAiService, "prepaidCardInfoTMapper", prepaidCardInfoTMapper);
    }

    @Test
    @DisplayName("空请求或者空文本能够安全返回默认初始化结构")
    void testBlankRequestReturnsDefault() {
        UniversalFinanceAiParseVo vo = universalFinanceAiService.parseLedger(new UniversalFinanceAiParseReq());
        assertNotNull(vo);
        assertEquals("DAILY_EXPENSE", vo.getTargetScene());
        assertEquals("expense", vo.getIncomeAndExpenses());
        assertEquals(BigDecimal.ZERO, vo.getAmount());
        assertEquals("LOCAL_REGEX_FALLBACK", vo.getParseEngine());
    }

    @Test
    @DisplayName("日常餐饮支出本地规则快速解析 - 包含微信与金额")
    void testDailyExpenseCateringParse() {
        TUserVo mockUser = new TUserVo();
        mockUser.setId(10086L);
        mockUser.setNickName("测试用户");
        when(userUtils.getLoginUser()).thenReturn(mockUser);

        UniversalFinanceAiParseReq req = UniversalFinanceAiParseReq.builder()
                .text("昨天中午和同事在肯德基聚餐微信付了128.5元，备注汉堡套餐")
                .scene("DAILY_EXPENSE")
                .build();

        UniversalFinanceAiParseVo vo = universalFinanceAiService.parseLedger(req);

        assertNotNull(vo);
        assertEquals("DAILY_EXPENSE", vo.getTargetScene());
        assertEquals("expense", vo.getIncomeAndExpenses());
        assertEquals(new BigDecimal("128.5"), vo.getAmount());
        assertEquals("catering", vo.getTypeCode());
        assertEquals("餐饮美食", vo.getTypeName());
        assertEquals("wx", vo.getFromSource());
        assertEquals(10086L, vo.getBelongTo());
        assertEquals("测试用户", vo.getBelongToName());
        assertEquals("LOCAL_REGEX_FALLBACK", vo.getParseEngine());
    }

    @Test
    @DisplayName("日常收入解析 - 工资到账银行卡")
    void testDailyIncomeSalaryParse() {
        UniversalFinanceAiParseReq req = UniversalFinanceAiParseReq.builder()
                .text("今天收到发工资到招商银行卡18500元")
                .build();

        UniversalFinanceAiParseVo vo = universalFinanceAiService.parseLedger(req);

        assertNotNull(vo);
        assertEquals("income", vo.getIncomeAndExpenses());
        assertEquals(new BigDecimal("18500"), vo.getAmount());
        assertEquals("salary", vo.getTypeCode());
        assertEquals("工资收入", vo.getTypeName());
        assertEquals("yhk", vo.getFromSource());
    }

    @Test
    @DisplayName("购物小票场景解析 - 识别小票单品明细与总计")
    void testShopReceiptParse() {
        String receiptText = "沃尔玛购物小票\n"
                + "鲜牛奶 2 18.50\n"
                + "全麦面包 1 12.00\n"
                + "纸巾 3 10.00\n"
                + "支付宝实付 79.00元";

        UniversalFinanceAiParseReq req = UniversalFinanceAiParseReq.builder()
                .text(receiptText)
                .scene("SHOP_RECEIPT")
                .build();

        UniversalFinanceAiParseVo vo = universalFinanceAiService.parseLedger(req);

        assertNotNull(vo);
        assertEquals("SHOP_RECEIPT", vo.getTargetScene());
        assertEquals(new BigDecimal("79.00"), vo.getAmount());
        assertEquals("zfb", vo.getFromSource());
        assertEquals("daily", vo.getTypeCode());

        List<UniversalFinanceAiParseVo.ReceiptItemVo> items = vo.getReceiptItems();
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(3, items.size());
        assertEquals("鲜牛奶", items.get(0).getItemName());
        assertEquals(2, items.get(0).getQuantity());
        assertEquals(new BigDecimal("18.50"), items.get(0).getUnitPrice());
    }

    @Test
    @DisplayName("预付卡流水解析 - 自动模糊匹配当前用户正常状态的预付储值卡")
    void testPrepaidCardMatchParse() {
        TUserVo mockUser = new TUserVo();
        mockUser.setId(10088L);
        mockUser.setNickName("卡包达人");
        when(userUtils.getLoginUser()).thenReturn(mockUser);

        PrepaidCardInfoT starbucksCard = new PrepaidCardInfoT();
        starbucksCard.setCardId(9001L);
        starbucksCard.setCardName("星巴克星礼卡");
        starbucksCard.setCurrentBalance(new BigDecimal("350.00"));
        starbucksCard.setCardStatus("正常");
        starbucksCard.setUserId(10088L);

        when(prepaidCardInfoTMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(starbucksCard));

        UniversalFinanceAiParseReq req = UniversalFinanceAiParseReq.builder()
                .text("在咖啡厅刷星巴克星礼卡消费了38元")
                .build();

        UniversalFinanceAiParseVo vo = universalFinanceAiService.parseLedger(req);

        assertNotNull(vo);
        assertEquals("PREPAID_CARD", vo.getTargetScene());
        assertEquals(9001L, vo.getPrepaidCardId());
        assertEquals("星巴克星礼卡", vo.getPrepaidCardName());
        assertEquals(new BigDecimal("350.00"), vo.getPrepaidCardBalance());
        assertEquals("prepaid_card", vo.getFromSource());
        assertEquals(new BigDecimal("38"), vo.getAmount());
    }
}
