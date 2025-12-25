package com.lottery.controller;

import com.lottery.common.Result;
import com.lottery.entity.Participant;
import com.lottery.entity.Prize;
import com.lottery.entity.RiggedSetting;
import com.lottery.repository.RiggedSettingRepository;
import com.lottery.service.ParticipantService;
import com.lottery.service.PrizeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 作弊设置控制器
 */
@RestController
@RequestMapping("/api/rigged-settings")
@RequiredArgsConstructor
@Slf4j
public class RiggedSettingController {

    private final RiggedSettingRepository riggedSettingRepository;
    private final ParticipantService participantService;
    private final PrizeService prizeService;

    /**
     * 查询所有作弊设置
     */
    @GetMapping
    public Result<List<RiggedSetting>> findAll() {
        return Result.success(riggedSettingRepository.findAll());
    }

    /**
     * 根据状态查询
     */
    @GetMapping("/status/{status}")
    public Result<List<RiggedSetting>> findByStatus(@PathVariable String status) {
        return Result.success(riggedSettingRepository.findByStatus(status));
    }

    /**
     * 创建作弊设置
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<RiggedSetting> create(@RequestBody CreateRiggedSettingRequest request) {
        // 查询参与人
        Participant participant = participantService.findById(request.getParticipantId());
        if (!"AVAILABLE".equals(participant.getStatus())) {
            return Result.error("该人员不可用");
        }

        // 查询奖项
        Prize prize = prizeService.findById(request.getPrizeId());
        if (!"PENDING".equals(prize.getStatus())) {
            return Result.error("该奖项不可用");
        }

        // 检查是否已存在
        if (riggedSettingRepository.existsByParticipantIdAndPrizeIdAndStatus(
                request.getParticipantId(), request.getPrizeId(), "PENDING")) {
            return Result.error("该设置已存在");
        }

        // 创建设置
        RiggedSetting setting = new RiggedSetting();
        setting.setParticipantId(participant.getId());
        setting.setParticipantName(participant.getName());
        setting.setPrizeId(prize.getId());
        setting.setPrizeName(prize.getName());
        setting.setPrizeLevel(prize.getLevel());
        setting.setStatus("PENDING");
        setting.setOperator(request.getOperator());

        return Result.success(riggedSettingRepository.save(setting));
    }

    /**
     * 删除作弊设置
     */
    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable String id) {
        RiggedSetting setting = riggedSettingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设置不存在"));

        if ("USED".equals(setting.getStatus())) {
            return Result.error("该设置已生效，不能删除");
        }

        riggedSettingRepository.deleteById(id);
        return Result.success(null);
    }

    /**
     * 创建作弊设置请求
     */
    @Data
    public static class CreateRiggedSettingRequest {
        private String participantId;
        private String prizeId;
        private String operator;
    }
}
