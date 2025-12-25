package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.entity.Participant;
import com.lottery.service.ExcelService;
import com.lottery.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 参与人员控制器
 */
@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
@Slf4j
public class ParticipantController {

    private final ParticipantService participantService;
    private final ExcelService excelService;

    /**
     * 查询所有人员
     */
    @GetMapping
    public Result<List<Participant>> findAll() {
        return Result.success(participantService.findAll());
    }

    /**
     * 根据状态查询人员
     */
    @GetMapping("/status/{status}")
    public Result<List<Participant>> findByStatus(@PathVariable String status) {
        return Result.success(participantService.findByStatus(status));
    }

    /**
     * 根据ID查询人员
     */
    @GetMapping("/{id}")
    public Result<Participant> findById(@PathVariable String id) {
        return Result.success(participantService.findById(id));
    }

    /**
     * 添加人员
     */
    @PostMapping
    public Result<Participant> add(@Validated @RequestBody Participant participant) {
        return Result.success(participantService.add(participant));
    }

    /**
     * 更新人员
     */
    @PutMapping("/{id}")
    public Result<Participant> update(@PathVariable String id,
                                      @Validated @RequestBody Participant participant) {
        return Result.success(participantService.update(id, participant));
    }

    /**
     * 删除人员
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        participantService.delete(id);
        return Result.success(null);
    }

    /**
     * 批量删除人员
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@RequestBody List<String> ids) {
        participantService.deleteBatch(ids);
        return Result.success(null);
    }

    /**
     * Excel导入人员
     */
    @PostMapping("/import")
    public Result<ExcelService.ImportResult> importExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        return Result.success(excelService.importParticipants(file));
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Result<ParticipantService.ParticipantStatistics> getStatistics() {
        return Result.success(participantService.getStatistics());
    }
}
