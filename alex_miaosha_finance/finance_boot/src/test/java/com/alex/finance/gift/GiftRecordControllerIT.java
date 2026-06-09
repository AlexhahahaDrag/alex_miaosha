package com.alex.finance.gift;

import com.alex.api.finance.gift.record.query.GiftRecordQuery;
import com.alex.api.finance.gift.record.vo.GiftRecordInfoTVo;
import com.alex.common.exception.handler.GlobalExceptionHandler;
import com.alex.finance.gift.record.controller.GiftRecordInfoTController;
import com.alex.finance.gift.record.service.GiftRecordInfoTService;
import com.alex.finance.gift.support.GiftExceptions;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller 切片集成测试：验证 HTTP 层与 Service 契约、全局异常转换。
 */
@WebMvcTest(controllers = GiftRecordInfoTController.class)
@Import(GlobalExceptionHandler.class)
class GiftRecordControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GiftRecordInfoTService giftRecordInfoTService;

    @Test
    void add_should_return_success_when_service_succeeds() throws Exception {
        GiftRecordInfoTVo saved = new GiftRecordInfoTVo();
        saved.setId(1L);
        saved.setDirection("RECEIVE");
        saved.setAmount(new BigDecimal("100"));
        when(giftRecordInfoTService.addGiftRecordInfoT(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/gift-record-info-t")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiftRecordInfoTVo()
                                .setDirection("RECEIVE")
                                .setAmount(new BigDecimal("100")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void add_should_return_error_when_service_rejects_invalid_amount() throws Exception {
        when(giftRecordInfoTService.addGiftRecordInfoT(any()))
                .thenThrow(GiftExceptions.param("礼金金额必须大于0"));

        mockMvc.perform(post("/api/v1/gift-record-info-t")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GiftRecordInfoTVo()
                                .setDirection("RECEIVE")
                                .setAmount(BigDecimal.ZERO))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("礼金金额必须大于0"));
    }

    @Test
    void postPage_should_delegate_to_service_getPage() throws Exception {
        GiftRecordQuery query = new GiftRecordQuery().setDirection("GIVE");
        when(giftRecordInfoTService.getPage(1L, 10L, query)).thenReturn(new Page<>(1, 10, 0));

        mockMvc.perform(post("/api/v1/gift-record-info-t/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(giftRecordInfoTService).getPage(1L, 10L, query);
    }

    @Test
    void postPage_should_accept_null_query_body() throws Exception {
        when(giftRecordInfoTService.getPage(1L, 10L, null)).thenReturn(new Page<>(1, 10, 0));

        mockMvc.perform(post("/api/v1/gift-record-info-t/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(giftRecordInfoTService).getPage(eq(1L), eq(10L), isNull());
    }

    @Test
    void markReturned_should_return_error_when_service_rejects_cross_user() throws Exception {
        when(giftRecordInfoTService.markReturned(9L))
                .thenThrow(GiftExceptions.forbidden("无权访问其他用户的礼金记录"));

        mockMvc.perform(put("/api/v1/gift-record-info-t/mark-returned")
                        .param("receiveRecordId", "9"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("403"))
                .andExpect(jsonPath("$.message").value("无权访问其他用户的礼金记录"));
    }
}
