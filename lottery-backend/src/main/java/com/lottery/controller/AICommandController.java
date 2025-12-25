package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.service.AICommandService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI指令控制器
 */
@RestController
@RequestMapping("/api/ai-command")
@RequiredArgsConstructor
@Slf4j
public class AICommandController {

    private final AICommandService aiCommandService;

    /**
     * 解析语音指令
     */
    @PostMapping("/parse")
    public Result<AICommandService.CommandResult> parseCommand(@RequestBody ParseRequest request) {
        log.info("收到语音指令：{}", request.getTranscript());
        AICommandService.CommandResult result = aiCommandService.parseCommand(request.getTranscript());
        log.info("指令解析结果：type={}, reply={}", result.getType(), result.getReply());
        return Result.success(result);
    }

    /**
     * 解析请求
     */
    @Data
    public static class ParseRequest {
        private String transcript;  // 语音识别文本
    }
}
