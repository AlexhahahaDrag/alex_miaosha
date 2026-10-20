package com.alex.finance.gift;

import com.alex.api.finance.gift.record.vo.GiftRecordSummaryVo;
import com.alex.api.finance.gift.summary.vo.GiftPersonBalanceVo;
import com.alex.finance.gift.analysis.service.impl.GiftAnalysisServiceImpl;
import com.alex.finance.gift.event.service.GiftEventInfoService;
import com.alex.finance.gift.person.service.GiftPersonInfoService;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class GiftAnalysisExportTest {

    @Mock
    private GiftRecordInfoService giftRecordInfoService;

    @Mock
    private GiftEventInfoService giftEventInfoService;

    @Mock
    private GiftPersonInfoService giftPersonInfoService;

    @Spy
    @InjectMocks
    private GiftAnalysisServiceImpl giftAnalysisService;

    private GiftPersonBalanceVo mockBalance;
    private GiftRecordSummaryVo mockSummary;

    @BeforeEach
    void setUp() {
        mockBalance = new GiftPersonBalanceVo()
                .setPersonId(1847192837192837192L)
                .setPersonName("张三")
                .setRelationType("friend")
                .setRelationTypeName("好友")
                .setTotalReceiveAmount(new BigDecimal("2000.00"))
                .setTotalGiveAmount(new BigDecimal("800.00"))
                .setNetBalance(new BigDecimal("1200.00"))
                .setBalanceStatus("SURPLUS")
                .setRecordCount(3)
                .setLatestRecordTime(java.time.LocalDateTime.of(2026, 6, 15, 12, 0, 0))
                .setIsImbalancedAlert(true)
                .setAlertReason("大额单向礼金超180天无互动");

        mockSummary = new GiftRecordSummaryVo()
                .setReceiveAmount(new BigDecimal("5000.00"))
                .setReceiveCount(10L)
                .setGiveAmount(new BigDecimal("3000.00"))
                .setGiveCount(6L)
                .setReturnAmount(new BigDecimal("800.00"))
                .setReturnCount(2L)
                .setNetAmount(new BigDecimal("1200.00"));
    }

    @Test
    @DisplayName("GIFT-AN-07: 导出亲友互赠平衡矩阵Excel应正确生成双Sheet且包含完整样式与预警")
    void testExportBalanceMatrix_shouldGenerateDoubleSheetsWithAccurateData() throws Exception {
        doReturn(List.of(mockBalance)).when(giftAnalysisService).balanceMatrix(any(), any());
        doReturn(mockSummary).when(giftAnalysisService).overview(null);

        MockHttpServletResponse response = new MockHttpServletResponse();
        giftAnalysisService.exportBalanceMatrix(null, null, response);

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertTrue(Objects.requireNonNull(response.getHeader("Content-Disposition")).contains("attachment"));
        byte[] bytes = response.getContentAsByteArray();
        assertTrue(bytes.length > 0);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertEquals(2, workbook.getNumberOfSheets(), "应包含2个工作表");

            // Sheet 1: 亲友互赠平衡矩阵
            Sheet matrixSheet = workbook.getSheetAt(0);
            assertEquals("亲友互赠平衡矩阵", matrixSheet.getSheetName());
            Row headerRow = matrixSheet.getRow(0);
            assertEquals("亲友姓名", headerRow.getCell(0).getStringCellValue());
            assertEquals("累计收TA礼金", headerRow.getCell(2).getStringCellValue());
            assertEquals("健康度预警", headerRow.getCell(8).getStringCellValue());

            Row dataRow = matrixSheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("张三", dataRow.getCell(0).getStringCellValue());
            assertEquals("好友", dataRow.getCell(1).getStringCellValue());
            assertEquals(2000.0, dataRow.getCell(2).getNumericCellValue());
            assertEquals(800.0, dataRow.getCell(3).getNumericCellValue());
            assertEquals(1200.0, dataRow.getCell(4).getNumericCellValue());
            assertEquals("礼金盈余(待回)", dataRow.getCell(5).getStringCellValue());
            assertEquals(3.0, dataRow.getCell(6).getNumericCellValue());
            assertTrue(dataRow.getCell(8).getStringCellValue().contains("大额单向礼金超180天无互动"));

            // Sheet 2: 人情收支汇总总览
            Sheet summarySheet = workbook.getSheetAt(1);
            assertEquals("人情收支汇总总览", summarySheet.getSheetName());
            Row sHeader = summarySheet.getRow(0);
            assertEquals("统计维度", sHeader.getCell(0).getStringCellValue());
            assertEquals("数值 / 金额", sHeader.getCell(1).getStringCellValue());

            Row sRow1 = summarySheet.getRow(1);
            assertEquals("收礼总额", sRow1.getCell(0).getStringCellValue());
            assertEquals(5000.0, sRow1.getCell(1).getNumericCellValue());
        }
    }
}
