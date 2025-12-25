package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.entity.Prize;
import com.lottery.service.PrizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 奖项控制器
 */
@RestController
@RequestMapping("/api/prizes")
@RequiredArgsConstructor
@Slf4j
public class PrizeController {

    private final PrizeService prizeService;

    /**
     * 查询所有奖项
     */
    @GetMapping
    public Result<List<Prize>> findAll() {
        return Result.success(prizeService.findAll());
    }

    /**
     * 根据状态查询奖项
     */
    @GetMapping("/status/{status}")
    public Result<List<Prize>> findByStatus(@PathVariable String status) {
        return Result.success(prizeService.findByStatus(status));
    }

    /**
     * 根据ID查询奖项
     */
    @GetMapping("/{id}")
    public Result<Prize> findById(@PathVariable String id) {
        return Result.success(prizeService.findById(id));
    }

    /**
     * 创建奖项
     */
    @PostMapping
    public Result<Prize> create(@Validated @RequestBody Prize prize) {
        return Result.success(prizeService.create(prize));
    }

    /**
     * 更新奖项
     */
    @PutMapping("/{id}")
    public Result<Prize> update(@PathVariable String id,
                                @Validated @RequestBody Prize prize) {
        return Result.success(prizeService.update(id, prize));
    }

    /**
     * 删除奖项
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        prizeService.delete(id);
        return Result.success(null);
    }

    /**
     * 获取下一个待抽取的奖项
     */
    @GetMapping("/next-pending")
    public Result<Prize> getNextPendingPrize() {
        return Result.success(prizeService.getNextPendingPrize());
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Result<PrizeService.PrizeStatistics> getStatistics() {
        return Result.success(prizeService.getStatistics());
    }
}
