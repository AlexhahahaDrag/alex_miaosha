package com.alex.ai.controller;

import com.alex.ai.service.AiAnalyzeService;
import com.alex.api.ai.vo.AiAnalyzeReq;
import com.alex.api.ai.vo.AiAnalyzeResp;
import com.alex.base.common.Result;
import com.alex.common.annotations.LogRestRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Agent：
 * AI 分析接口
 */
@Api(value = "AI 分析相关接口", tags = {"AI 分析相关接口"})
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.version:/api/v1}/ai")
public class AiAnalyzeController {

    private final AiAnalyzeService aiAnalyzeService;

    @LogRestRequest(apiName = "AI 分析")
    @ApiOperation(value = "AI 分析", notes = "对输入内容进行 AI 分析（当前为规则引擎示例，可后续扩展为大模型）", response = Result.class)
    @PostMapping("/analyze")
    public Result<AiAnalyzeResp> analyze(@RequestBody AiAnalyzeReq req) {
        return Result.success(aiAnalyzeService.analyze(req));
    }
}


