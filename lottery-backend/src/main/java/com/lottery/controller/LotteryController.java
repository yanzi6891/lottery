package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.dto.LotteryResultDTO;
import com.lottery.service.LotteryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 抽奖控制器
 */
@RestController
@RequestMapping("/api/lottery")
@RequiredArgsConstructor
@Slf4j
public class LotteryController {

    private final LotteryService lotteryService;

    /**
     * 执行抽奖
     */
    @PostMapping("/draw")
    public Result<LotteryResultDTO> draw(@RequestBody DrawRequest request) {
        log.info("开始抽奖，奖项ID：{}，操作人：{}", request.getPrizeId(), request.getOperator());
        LotteryResultDTO result = lotteryService.draw(request.getPrizeId(), request.getOperator());
        return Result.success(result);
    }

    /**
     * 撤销中奖
     */
    @PostMapping("/cancel-win")
    public Result<Void> cancelWin(@RequestBody CancelWinRequest request) {
        log.info("撤销中奖，人员ID：{}，操作人：{}", request.getParticipantId(), request.getOperator());
        lotteryService.cancelWin(request.getParticipantId(), request.getOperator());
        return Result.success(null);
    }

    /**
     * 重置系统
     */
    @PostMapping("/reset")
    public Result<Void> reset() {
        log.info("重置抽奖系统");
        lotteryService.reset();
        return Result.success(null);
    }

    /**
     * 抽奖请求
     */
    @Data
    public static class DrawRequest {
        private String prizeId;
        private String operator;
    }

    /**
     * 撤销中奖请求
     */
    @Data
    public static class CancelWinRequest {
        private String participantId;
        private String operator;
    }
}
