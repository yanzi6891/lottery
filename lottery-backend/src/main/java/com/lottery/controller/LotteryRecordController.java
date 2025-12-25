package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.entity.LotteryRecord;
import com.lottery.repository.LotteryRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 抽奖记录控制器
 */
@RestController
@RequestMapping("/api/lottery-records")
@RequiredArgsConstructor
@Slf4j
public class LotteryRecordController {

    private final LotteryRecordRepository lotteryRecordRepository;

    /**
     * 查询所有记录
     */
    @GetMapping
    public Result<List<LotteryRecord>> findAll() {
        return Result.success(lotteryRecordRepository.findAll());
    }

    /**
     * 根据奖项ID查询记录
     */
    @GetMapping("/prize/{prizeId}")
    public Result<List<LotteryRecord>> findByPrizeId(@PathVariable String prizeId) {
        return Result.success(lotteryRecordRepository.findByPrizeIdAndIsCancelled(prizeId, false));
    }

    /**
     * 根据参与人ID查询记录
     */
    @GetMapping("/participant/{participantId}")
    public Result<List<LotteryRecord>> findByParticipantId(@PathVariable String participantId) {
        return Result.success(lotteryRecordRepository.findByParticipantIdAndIsCancelled(participantId, false));
    }

    /**
     * 查询所有有效记录（未撤销）
     */
    @GetMapping("/valid")
    public Result<List<LotteryRecord>> findValidRecords() {
        return Result.success(lotteryRecordRepository.findByIsCancelledOrderByDrawTimeDesc(false));
    }
}
