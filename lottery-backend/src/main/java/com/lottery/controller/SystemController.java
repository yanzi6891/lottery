package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.service.ParticipantService;
import com.lottery.service.PrizeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 系统控制器
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
@Slf4j
public class SystemController {

    private final ParticipantService participantService;
    private final PrizeService prizeService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }

    /**
     * 系统信息
     */
    @GetMapping("/info")
    public Result<SystemInfo> getSystemInfo() {
        SystemInfo info = new SystemInfo();
        info.setServerTime(LocalDateTime.now());
        info.setVersion("1.0.0");
        info.setParticipantStats(participantService.getStatistics());
        info.setPrizeStats(prizeService.getStatistics());
        return Result.success(info);
    }

    /**
     * 系统信息
     */
    @Data
    public static class SystemInfo {
        private LocalDateTime serverTime;
        private String version;
        private ParticipantService.ParticipantStatistics participantStats;
        private PrizeService.PrizeStatistics prizeStats;
    }
}
