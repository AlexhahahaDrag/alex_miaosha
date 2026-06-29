package com.alex.finance.gift;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoVo;
import com.alex.common.exception.handler.GlobalExceptionHandler;
import com.alex.finance.gift.record.controller.GiftRecordInfoController;
import com.alex.finance.gift.record.service.GiftRecordInfoService;
import com.alex.finance.gift.support.GiftExceptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
                .thenThrow(GiftExceptions.param("礼金金额必须大于0"));

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
                .thenThrow(GiftExceptions.forbidden("无权访问其他用户的礼金记录"));

        mockMvc.perform(put("/api/v1/gift-record-info-t/mark-returned")
                        .param("receiveRecordId", "9"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.message").value("无权访问其他用户的礼金记录"));
    }
}
