package com.alex.finance.gift;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.common.exception.handler.GlobalExceptionHandler;
import com.alex.finance.gift.event.mapper.GiftEventInfoMapper;
import com.alex.finance.gift.person.mapper.GiftPersonInfoMapper;
import com.alex.finance.gift.record.controller.GiftRecordInfoController;
import com.alex.finance.gift.record.mapper.GiftRecordInfoMapper;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.record.service.impl.GiftRecordInfoServiceImp;
import com.alex.finance.gift.support.GiftDataScopeSupport;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GiftRecordExportTest {

    @Test
    void exportGiftRecordInfo_should_write_excel_attachment_with_records() throws Exception {
        GiftRecordInfoMapper mapper = mock(GiftRecordInfoMapper.class);
        GiftRecordInfoServiceImp service = new GiftRecordInfoServiceImp(
                new GiftDataScopeSupport(loginUserUtils()),
                mock(GiftPersonInfoMapper.class),
                mock(GiftEventInfoMapper.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        GiftRecordInfoVo record = new GiftRecordInfoVo()
                .setDirection("RECEIVE")
                .setEventName("婚宴")
                .setPersonName("张三")
                .setAmount(new BigDecimal("666.00"))
                .setPayTime(LocalDateTime.of(2026, 6, 10, 12, 30))
                .setReturnedFlag(0)
                .setRemark("朋友");
        when(mapper.getList(any(GiftRecordQuery.class))).thenReturn(List.of(record));

        MockHttpServletResponse response = new MockHttpServletResponse();

        service.exportGiftRecordInfo(new GiftRecordQuery().setDirection("RECEIVE"), response);

        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", response.getContentType());
        assertTrue(Objects.requireNonNull(response.getHeader("Content-Disposition")).contains("gift_record_info"));
        assertTrue(response.getContentAsByteArray().length > 0);

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(response.getContentAsByteArray()))) {
            Row header = workbook.getSheetAt(0).getRow(0);
            Row data = workbook.getSheetAt(0).getRow(1);
            assertEquals("日期", header.getCell(0).getStringCellValue());
            assertEquals("事由", header.getCell(1).getStringCellValue());
            assertEquals("往来对象", header.getCell(2).getStringCellValue());
            assertEquals("类型", header.getCell(3).getStringCellValue());
            assertEquals("金额", header.getCell(4).getStringCellValue());
            assertEquals("状态", header.getCell(5).getStringCellValue());
            assertEquals("婚宴", data.getCell(1).getStringCellValue());
            assertEquals("张三", data.getCell(2).getStringCellValue());
            assertEquals(666.00D, data.getCell(4).getNumericCellValue(), 0.001D);
        }
    }

    private static UserUtils loginUserUtils() {
        UserUtils userUtils = mock(UserUtils.class);
        when(userUtils.getLoginUser()).thenReturn(loginUser());
        return userUtils;
    }

    private static TUserVo loginUser() {
        OrgInfoVo orgInfoVo = new OrgInfoVo();
        orgInfoVo.setId(20L);
        TUserVo user = new TUserVo();
        user.setId(10L);
        user.setOrgId(20L);
        user.setOrgInfoVo(orgInfoVo);
        return user;
    }

    @ExtendWith(MockitoExtension.class)
    static class GiftRecordExportControllerTest {

        @Mock
        private GiftRecordInfoService giftRecordInfoService;

        private MockMvc mockMvc;

        @BeforeEach
        void setUp() {
            GiftRecordInfoController controller = new GiftRecordInfoController(giftRecordInfoService);
            mockMvc = MockMvcBuilders.standaloneSetup(controller)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();
        }

        @Test
        void postExport_should_delegate_to_service_and_return_attachment() throws Exception {
            mockMvc.perform(post("/api/v1/gift-record-info-t/export")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"direction\":\"RECEIVE\"}"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")));

            verify(giftRecordInfoService).exportGiftRecordInfo(any(GiftRecordQuery.class), any());
        }
    }
}
