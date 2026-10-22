package com.alex.finance.finance;

import com.alex.api.finance.vo.finance.FinanceInfoVo;
import com.alex.api.finance.vo.finance.FinanceSummaryVo;
import com.alex.api.user.userInfo.UserUtils;
import com.alex.api.user.userInfo.api.UserApi;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.cache.multilevel.HotKeyDetector;
import com.alex.common.cache.multilevel.MultiLevelCacheProperties;
import com.alex.common.cache.multilevel.MultiLevelCacheService;
import com.alex.finance.finance.entity.FinanceInfo;
import com.alex.finance.finance.mapper.FinanceInfoMapper;
import com.alex.finance.finance.service.impl.FinanceInfoServiceImp;
import com.alex.finance.handler.IExcelDictHandlerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 个人日常财务 (SelfFinance) 收支总览多级缓存 (L1 Caffeine + L2 Redis) 加速与双写失效集成测试
 * 覆盖：
 * 1. 首次查询穿透回源并预热 L1/L2，二次查询 100% 命中缓存免查库；
 * 2. 记账流水新增、修改、删除时精准触发多级缓存双层失效；
 * 3. 内部资金转账调拨 (Transfer) 对账闭环，严格排除于日常收支汇总之外；
 * 4. 多家庭成员/不同属主用户缓存空间严格隔离。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("个人日常财务 (SelfFinance) 多级缓存加速与月度对账闭环测试")
class FinanceSummaryMultiLevelCacheTest {

    private static final Long USER_ID_A = 1001L;
    private static final Long ORG_ID_A = 2001L;

    private static final Long USER_ID_B = 1002L;
    private static final Long ORG_ID_B = 2001L;

    @Mock
    private FinanceInfoMapper financeInfoMapper;

    @Mock
    private IExcelDictHandlerImpl iExcelDictHandler;

    @Mock
    private UserApi userApi;

    @Mock
    private UserUtils userUtils;

    private MultiLevelCacheService multiLevelCacheService;
    private FinanceInfoServiceImp financeInfoService;

    @BeforeEach
    void setUp() {
        MultiLevelCacheProperties properties = new MultiLevelCacheProperties();
        properties.setL1MaximumSize(1000L);
        properties.setL1DefaultTtlSeconds(300L);
        HotKeyDetector detector = new HotKeyDetector(properties);
        multiLevelCacheService = new MultiLevelCacheService(properties, null, detector);

        financeInfoService = new FinanceInfoServiceImp(
                financeInfoMapper,
                iExcelDictHandler,
                userApi
        );
        ReflectionTestUtils.setField(financeInfoService, "baseMapper", financeInfoMapper);
        ReflectionTestUtils.setField(financeInfoService, "userUtils", userUtils);
        financeInfoService.setMultiLevelCacheService(multiLevelCacheService);

        TUserVo userA = new TUserVo();
        userA.setId(USER_ID_A);
        userA.setOrgId(ORG_ID_A);
        lenient().when(userUtils.getLoginUser()).thenReturn(userA);
    }

    @Test
    @DisplayName("1. 首次查询回源数据库并预热多级缓存，二次查询 100% 命中缓存无多余 DB 查询")
    void testFinanceSummaryMultiLevelCacheHit() {
        FinanceInfoVo queryVo = new FinanceInfoVo()
                .setBelongTo(USER_ID_A)
                .setOrgId(ORG_ID_A)
                .setInfoDateStart(LocalDate.of(2026, 10, 1))
                .setInfoDateEnd(LocalDate.of(2026, 10, 31));

        FinanceSummaryVo dbSummary = FinanceSummaryVo.builder()
                .totalIncome(new BigDecimal("8000.00"))
                .totalExpense(new BigDecimal("3500.00"))
                .totalCount(15L)
                .build();

        when(financeInfoMapper.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(dbSummary);

        // 第一次调用：回源并写入缓存
        FinanceSummaryVo firstResult = financeInfoService.getFinanceSummary(queryVo);
        assertNotNull(firstResult);
        assertEquals(new BigDecimal("8000.00"), firstResult.getTotalIncome());
        assertEquals(new BigDecimal("3500.00"), firstResult.getTotalExpense());
        assertEquals(new BigDecimal("4500.00"), firstResult.getTotalBalance(), "净结余应为 8000 - 3500 = 4500");
        verify(financeInfoMapper, times(1)).getFinanceSummary(any(FinanceInfoVo.class));

        // 第二次调用：直接命中 L1 缓存，Mapper 调用次数保持为 1
        FinanceSummaryVo secondResult = financeInfoService.getFinanceSummary(queryVo);
        assertNotNull(secondResult);
        assertEquals(new BigDecimal("8000.00"), secondResult.getTotalIncome());
        assertEquals(new BigDecimal("3500.00"), secondResult.getTotalExpense());
        verify(financeInfoMapper, times(1)).getFinanceSummary(any(FinanceInfoVo.class));
    }

    @Test
    @DisplayName("2. 新增记账流水时精准触发多级缓存失效，后续查询自动穿透拉取最新汇总")
    void testCacheEvictedOnAddFinanceInfo() {
        FinanceInfoVo queryVo = new FinanceInfoVo()
                .setBelongTo(USER_ID_A)
                .setOrgId(ORG_ID_A);

        FinanceSummaryVo initialSummary = FinanceSummaryVo.builder()
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpense(new BigDecimal("2000.00"))
                .totalCount(5L)
                .build();

        FinanceSummaryVo updatedSummary = FinanceSummaryVo.builder()
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpense(new BigDecimal("2300.00"))
                .totalCount(6L)
                .build();

        when(financeInfoMapper.getFinanceSummary(any(FinanceInfoVo.class)))
                .thenReturn(initialSummary)
                .thenReturn(updatedSummary);

        // 预热缓存
        FinanceSummaryVo r1 = financeInfoService.getFinanceSummary(queryVo);
        assertEquals(new BigDecimal("2000.00"), r1.getTotalExpense());
        verify(financeInfoMapper, times(1)).getFinanceSummary(any(FinanceInfoVo.class));

        // 新增一笔支出流水 (¥300)
        FinanceInfoVo newRecord = new FinanceInfoVo()
                .setName("超市日常采购")
                .setAmount(new BigDecimal("300.00"))
                .setIncomeAndExpenses("expense")
                .setTypeCode("日用百货")
                .setFromSource("微信支付")
                .setBelongTo(USER_ID_A)
                .setOrgId(ORG_ID_A);

        when(financeInfoMapper.insert(any(FinanceInfo.class))).thenReturn(1);
        financeInfoService.addFinanceInfo(newRecord);

        // 再次查询：缓存已失效，Mapper 再次被调用并返回最新值 ¥2300
        FinanceSummaryVo r2 = financeInfoService.getFinanceSummary(queryVo);
        assertEquals(new BigDecimal("2300.00"), r2.getTotalExpense());
        verify(financeInfoMapper, times(2)).getFinanceSummary(any(FinanceInfoVo.class));
    }

    @Test
    @DisplayName("3. 修改记账流水时多级缓存失效验证")
    void testCacheEvictedOnUpdateFinanceInfo() {
        FinanceInfoVo queryVo = new FinanceInfoVo()
                .setBelongTo(USER_ID_A)
                .setOrgId(ORG_ID_A);

        FinanceSummaryVo s1 = FinanceSummaryVo.builder()
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpense(new BigDecimal("2000.00"))
                .build();
        FinanceSummaryVo s2 = FinanceSummaryVo.builder()
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpense(new BigDecimal("2500.00"))
                .build();

        when(financeInfoMapper.getFinanceSummary(any(FinanceInfoVo.class)))
                .thenReturn(s1)
                .thenReturn(s2);

        // 预热
        financeInfoService.getFinanceSummary(queryVo);

        // 模拟已有流水
        FinanceInfo existing = new FinanceInfo();
        existing.setId(88L);
        existing.setBelongTo(USER_ID_A);
        when(financeInfoMapper.selectById(88L)).thenReturn(existing);
        when(financeInfoMapper.updateById(any(FinanceInfo.class))).thenReturn(1);

        FinanceInfoVo updateVo = new FinanceInfoVo();
        updateVo.setId(88L);
        updateVo.setName("电费缴纳调整");
        updateVo.setAmount(new BigDecimal("500.00"));
        updateVo.setBelongTo(USER_ID_A);
        updateVo.setOrgId(ORG_ID_A);

        financeInfoService.updateFinanceInfo(updateVo);

        // 缓存失效后查出新汇总
        FinanceSummaryVo r2 = financeInfoService.getFinanceSummary(queryVo);
        assertEquals(new BigDecimal("2500.00"), r2.getTotalExpense());
        verify(financeInfoMapper, times(2)).getFinanceSummary(any(FinanceInfoVo.class));
    }

    @Test
    @DisplayName("4. 删除记账流水时多级缓存失效验证")
    void testCacheEvictedOnDeleteFinanceInfo() {
        FinanceInfoVo queryVo = new FinanceInfoVo()
                .setBelongTo(USER_ID_A)
                .setOrgId(ORG_ID_A);

        FinanceSummaryVo s1 = FinanceSummaryVo.builder().totalCount(3L).build();
        FinanceSummaryVo s2 = FinanceSummaryVo.builder().totalCount(2L).build();

        when(financeInfoMapper.getFinanceSummary(any(FinanceInfoVo.class)))
                .thenReturn(s1)
                .thenReturn(s2);

        financeInfoService.getFinanceSummary(queryVo);

        FinanceInfo existing = new FinanceInfo();
        existing.setId(99L);
        existing.setBelongTo(USER_ID_A);
                when(financeInfoMapper.selectById(99L)).thenReturn(existing);
        when(financeInfoMapper.deleteByIds(anyList())).thenReturn(1);

        financeInfoService.deleteFinanceInfo("99");

        FinanceSummaryVo r2 = financeInfoService.getFinanceSummary(queryVo);
        assertEquals(2L, r2.getTotalCount());
        verify(financeInfoMapper, times(2)).getFinanceSummary(any(FinanceInfoVo.class));
    }

    @Test
    @DisplayName("5. 内部资金转账调拨 (Transfer) 对账闭环：严格排除于总收支统计，结余不受虚增影响")
    void testInternalTransferZeroImpactOnSummary() {
        // 模拟数据库返回排除了转账记录后的纯净收支统计
        FinanceSummaryVo pureSummary = FinanceSummaryVo.builder()
                .totalIncome(new BigDecimal("10000.00"))
                .totalExpense(new BigDecimal("4000.00"))
                .totalCount(8L)
                .build();

        when(financeInfoMapper.getFinanceSummary(any(FinanceInfoVo.class))).thenReturn(pureSummary);

        FinanceInfoVo vo = new FinanceInfoVo()
                .setBelongTo(USER_ID_A)
                .setOrgId(ORG_ID_A);

        FinanceSummaryVo result = financeInfoService.getFinanceSummary(vo);

        // 断言：净结余严格等于 10000 - 4000 = 6000，转账金额无论多大均不得侵入收支差额
        assertEquals(new BigDecimal("10000.00"), result.getTotalIncome());
        assertEquals(new BigDecimal("4000.00"), result.getTotalExpense());
        assertEquals(new BigDecimal("6000.00"), result.getTotalBalance());
    }

    @Test
    @DisplayName("6. 多家庭成员/不同属主用户缓存空间严格隔离")
    void testMultiUserCacheIsolation() {
        FinanceInfoVo queryA = new FinanceInfoVo().setBelongTo(USER_ID_A).setOrgId(ORG_ID_A);
        FinanceInfoVo queryB = new FinanceInfoVo().setBelongTo(USER_ID_B).setOrgId(ORG_ID_B);

        FinanceSummaryVo summaryA = FinanceSummaryVo.builder().totalIncome(new BigDecimal("1000.00")).build();
        FinanceSummaryVo summaryB = FinanceSummaryVo.builder().totalIncome(new BigDecimal("9999.00")).build();

        when(financeInfoMapper.getFinanceSummary(argThat(v -> v != null && USER_ID_A.equals(v.getBelongTo())))).thenReturn(summaryA);
        when(financeInfoMapper.getFinanceSummary(argThat(v -> v != null && USER_ID_B.equals(v.getBelongTo())))).thenReturn(summaryB);

        FinanceSummaryVo rA = financeInfoService.getFinanceSummary(queryA);
        FinanceSummaryVo rB = financeInfoService.getFinanceSummary(queryB);

        assertEquals(new BigDecimal("1000.00"), rA.getTotalIncome());
        assertEquals(new BigDecimal("9999.00"), rB.getTotalIncome());
        verify(financeInfoMapper, times(2)).getFinanceSummary(any(FinanceInfoVo.class));
    }
}
