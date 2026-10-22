package com.alex.finance.gift;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.common.exception.handler.GlobalExceptionHandler;
import com.alex.finance.gift.record.controller.GiftRecordInfoController;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.common.exception.FinanceException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller ?????standalone MockMvc?????? Controller ????????
 */
@ExtendWith(MockitoExtension.class)
class GiftRecordControllerIT {

    @Mock
    private GiftRecordInfoService giftRecordInfoService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        GiftRecordInfoController controller = new GiftRecordInfoController(giftRecordInfoService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void add_should_return_success_when_service_succeeds() throws Exception {
        GiftRecordInfoVo saved = new GiftRecordInfoVo();
        saved.setId(1L);
        saved.setDirection("RECEIVE");
        saved.setAmount(new BigDecimal("100"));
        when(giftRecordInfoService.addGiftRecordInfo(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/gift-record-info-t")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiftRecordInfoVo()
                                .setDirection("RECEIVE")
                                .setAmount(new BigDecimal("100")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void add_should_return_error_when_service_rejects_invalid_amount() throws Exception {
        when(giftRecordInfoService.addGiftRecordInfo(any()))
                .thenThrow(FinanceException.param("礼金金额必须大于0"));

        mockMvc.perform(post("/api/v1/gift-record-info-t")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiftRecordInfoVo()
                                .setDirection("RECEIVE")
                                .setAmount(BigDecimal.ZERO))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("礼金金额必须大于0"));
    }

    @Test
    void postPage_should_delegate_to_service_getPage() throws Exception {
        when(giftRecordInfoService.getPage(eq(1L), eq(10L), any(GiftRecordQuery.class)))
                .thenReturn(new Page<>(1, 10, 0));

        mockMvc.perform(post("/api/v1/gift-record-info-t/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiftRecordQuery().setDirection("GIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(giftRecordInfoService).getPage(
                eq(1L),
                eq(10L),
                argThat(query -> "GIVE".equals(query.getDirection())));
    }

    @Test
    void postPage_should_accept_null_query_body() throws Exception {
        when(giftRecordInfoService.getPage(1L, 10L, null)).thenReturn(new Page<>(1, 10, 0));

        mockMvc.perform(post("/api/v1/gift-record-info-t/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(giftRecordInfoService).getPage(eq(1L), eq(10L), isNull());
    }

    @Test
    void markReturned_should_return_error_when_service_rejects_cross_user() throws Exception {
        when(giftRecordInfoService.markReturned(9L))
                .thenThrow(FinanceException.forbidden("无权访问其他用户的礼金记录"));

        mockMvc.perform(put("/api/v1/gift-record-info-t/mark-returned")
                        .param("receiveRecordId", "9"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.message").value("无权访问其他用户的礼金记录"));
    }

    @Test
    @DisplayName("GET query: should return record when authorized")
    void get_should_return_record_when_authorized() throws Exception {
        GiftRecordInfoVo record = new GiftRecordInfoVo();
        record.setId(10L);
        record.setDirection("GIVE");
        record.setAmount(new BigDecimal("500"));
        when(giftRecordInfoService.queryGiftRecordInfo(10L)).thenReturn(record);

        mockMvc.perform(get("/api/v1/gift-record-info-t")
                        .param("id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.direction").value("GIVE"));
    }

    @Test
    @DisplayName("GET query: cross_user should return 403 error response")
    void get_should_return_403_when_cross_user() throws Exception {
        when(giftRecordInfoService.queryGiftRecordInfo(99L))
                .thenThrow(FinanceException.forbidden("无权访问其他机构的礼金记录"));

        mockMvc.perform(get("/api/v1/gift-record-info-t")
                        .param("id", "99"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.message").value("无权访问其他机构的礼金记录"));
    }

    @Test
    @DisplayName("PUT update: cross_user should return 403 error response (H3)")
    void update_should_return_403_when_cross_user() throws Exception {
        when(giftRecordInfoService.updateGiftRecordInfo(any()))
                .thenThrow(FinanceException.forbidden("无权访问其他机构的礼金记录"));

        mockMvc.perform(put("/api/v1/gift-record-info-t")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiftRecordInfoVo()
                                .setDirection("RECEIVE")
                                .setAmount(new BigDecimal("100")))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.message").value("无权访问其他机构的礼金记录"));
    }

    @Test
    @DisplayName("PUT update: should return 200 when update succeeds")
    void update_should_return_success_when_authorized() throws Exception {
        when(giftRecordInfoService.updateGiftRecordInfo(any())).thenReturn(true);

        mockMvc.perform(put("/api/v1/gift-record-info-t")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiftRecordInfoVo()
                                .setDirection("RECEIVE")
                                .setAmount(new BigDecimal("100")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("DELETE delete: cross_user should return 403 error response (H4)")
    void delete_should_return_403_when_cross_user() throws Exception {
        when(giftRecordInfoService.deleteGiftRecordInfo("99"))
                .thenThrow(FinanceException.forbidden("无权访问其他机构的礼金记录"));

        mockMvc.perform(delete("/api/v1/gift-record-info-t")
                        .param("ids", "99"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.message").value("无权访问其他机构的礼金记录"));
    }

    @Test
    @DisplayName("DELETE delete: should return 200 when delete succeeds")
    void delete_should_return_success_when_authorized() throws Exception {
        when(giftRecordInfoService.deleteGiftRecordInfo("1,2")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/gift-record-info-t")
                        .param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("GET pending-return-amount: should return amount when authorized")
    void pendingReturnAmount_should_return_success() throws Exception {
        when(giftRecordInfoService.calculatePendingReturnAmount(5L)).thenReturn(new BigDecimal("150.00"));

        mockMvc.perform(get("/api/v1/gift-record-info-t/pending-return-amount")
                        .param("receiveRecordId", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(150.00));
    }
}
