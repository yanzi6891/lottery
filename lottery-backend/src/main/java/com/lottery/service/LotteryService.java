package com.lottery.service;

import cn.hutool.core.util.RandomUtil;
import com.lottery.dto.LotteryResultDTO;
import com.lottery.entity.LotteryRecord;
import com.lottery.entity.Participant;
import com.lottery.entity.Prize;
import com.lottery.entity.RiggedSetting;
import com.lottery.repository.LotteryRecordRepository;
import com.lottery.repository.ParticipantRepository;
import com.lottery.repository.PrizeRepository;
import com.lottery.repository.RiggedSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽奖核心服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LotteryService {

    private final ParticipantRepository participantRepository;
    private final PrizeRepository prizeRepository;
    private final LotteryRecordRepository lotteryRecordRepository;
    private final RiggedSettingRepository riggedSettingRepository;

    /**
     * 执行抽奖
     *
     * @param prizeId 奖项ID
     * @param operator 操作人
     * @return 抽奖结果
     */
    @Transactional(rollbackFor = Exception.class)
    public LotteryResultDTO draw(String prizeId, String operator) {
        // 1. 获取奖项信息
        Prize prize = prizeRepository.findById(prizeId)
                .orElseThrow(() -> new RuntimeException("奖项不存在"));

        // 2. 检查奖项状态
        if ("COMPLETED".equals(prize.getStatus())) {
            throw new RuntimeException("该奖项已抽取完毕");
        }

        // 3. 检查是否还有名额
        int remainingCount = prize.getCount() - prize.getDrawnCount();
        if (remainingCount <= 0) {
            throw new RuntimeException("该奖项已无剩余名额");
        }

        // 4. 获取可用人员池（状态为AVAILABLE）
        List<Participant> availableParticipants = participantRepository.findByStatus("AVAILABLE");
        if (availableParticipants.isEmpty()) {
            throw new RuntimeException("没有可参与抽奖的人员");
        }

        if (availableParticipants.size() < remainingCount) {
            throw new RuntimeException("可用人员不足，请减少中奖人数");
        }

        // 5. 获取作弊设置（该奖项的待生效设置）
        List<RiggedSetting> riggedSettings = riggedSettingRepository
                .findByPrizeIdAndStatus(prizeId, "PENDING");

        // 6. 执行抽奖算法
        List<Participant> winners = performDraw(availableParticipants, remainingCount, riggedSettings);

        // 7. 更新人员状态
        LocalDateTime drawTime = LocalDateTime.now();
        for (Participant winner : winners) {
            winner.setStatus("WON");
            winner.setWonPrizeId(prizeId);
            winner.setWonPrizeName(prize.getName());
            winner.setWonTime(drawTime);
            participantRepository.save(winner);
        }

        // 8. 更新奖项状态
        prize.setDrawnCount(prize.getDrawnCount() + winners.size());
        if (prize.getDrawnCount() >= prize.getCount()) {
            prize.setStatus("COMPLETED");
        }
        prize.setDrawTime(drawTime);
        prizeRepository.save(prize);

        // 9. 记录抽奖日志
        for (Participant winner : winners) {
            LotteryRecord record = new LotteryRecord();
            record.setPrizeId(prizeId);
            record.setPrizeName(prize.getName());
            record.setPrizeLevel(prize.getLevel());
            record.setParticipantId(winner.getId());
            record.setParticipantName(winner.getName());

            // 检查是否是作弊中奖
            boolean isRigged = riggedSettings.stream()
                    .anyMatch(r -> r.getParticipantId().equals(winner.getId()));
            record.setAction(isRigged ? "RIGGED" : "DRAW");
            record.setOperator(operator);
            record.setDrawTime(drawTime);

            lotteryRecordRepository.save(record);
        }

        // 10. 更新作弊设置状态
        for (RiggedSetting setting : riggedSettings) {
            if (winners.stream().anyMatch(w -> w.getId().equals(setting.getParticipantId()))) {
                setting.setStatus("USED");
                setting.setUsedTime(drawTime);
                riggedSettingRepository.save(setting);
            }
        }

        // 11. 构造返回结果
        return buildLotteryResult(prize, winners, riggedSettings, drawTime);
    }

    /**
     * 执行抽奖算法（Fisher-Yates洗牌算法）
     *
     * @param candidates 候选人
     * @param count 需要抽取的人数
     * @param riggedSettings 作弊设置
     * @return 中奖人员
     */
    private List<Participant> performDraw(List<Participant> candidates, int count,
                                          List<RiggedSetting> riggedSettings) {
        List<Participant> winners = new ArrayList<>();

        // 1. 先处理作弊设置（必中人员）
        for (RiggedSetting setting : riggedSettings) {
            Participant riggedParticipant = candidates.stream()
                    .filter(p -> p.getId().equals(setting.getParticipantId()))
                    .findFirst()
                    .orElse(null);

            if (riggedParticipant != null) {
                winners.add(riggedParticipant);
                candidates.remove(riggedParticipant);
                log.info("作弊设置生效：{} 必中 {}",
                        riggedParticipant.getName(), setting.getPrizeName());
            }
        }

        // 2. 计算还需要随机抽取的人数
        int remainCount = count - winners.size();
        if (remainCount <= 0) {
            return winners;
        }

        // 3. 使用Fisher-Yates算法随机抽取剩余名额
        List<Participant> randomWinners = RandomUtil.randomEleList(candidates, remainCount);
        winners.addAll(randomWinners);

        return winners;
    }

    /**
     * 构造抽奖结果DTO
     */
    private LotteryResultDTO buildLotteryResult(Prize prize, List<Participant> winners,
                                                List<RiggedSetting> riggedSettings,
                                                LocalDateTime drawTime) {
        LotteryResultDTO result = new LotteryResultDTO();
        result.setPrizeId(prize.getId());
        result.setPrizeName(prize.getName());
        result.setPrizeLevel(prize.getLevel());
        result.setDrawTime(drawTime);

        List<LotteryResultDTO.WinnerDTO> winnerDTOs = winners.stream().map(winner -> {
            LotteryResultDTO.WinnerDTO dto = new LotteryResultDTO.WinnerDTO();
            dto.setId(winner.getId());
            dto.setName(winner.getName());
            dto.setEmployeeId(winner.getEmployeeId());
            dto.setDepartment(winner.getDepartment());

            // 标记是否是作弊中奖
            boolean isRigged = riggedSettings.stream()
                    .anyMatch(r -> r.getParticipantId().equals(winner.getId()));
            dto.setIsRigged(isRigged);

            return dto;
        }).collect(Collectors.toList());

        result.setWinners(winnerDTOs);
        return result;
    }

    /**
     * 撤销中奖
     *
     * @param participantId 参与人ID
     * @param operator 操作人
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelWin(String participantId, String operator) {
        // 1. 查询参与人
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new RuntimeException("人员不存在"));

        if (!"WON".equals(participant.getStatus())) {
            throw new RuntimeException("该人员未中奖，无需撤销");
        }

        // 2. 查询中奖记录
        LotteryRecord record = lotteryRecordRepository
                .findFirstByParticipantIdAndIsCancelledOrderByDrawTimeDesc(participantId, false)
                .orElseThrow(() -> new RuntimeException("未找到中奖记录"));

        // 3. 更新记录状态
        record.setIsCancelled(true);
        record.setCancelledTime(LocalDateTime.now());
        lotteryRecordRepository.save(record);

        // 4. 恢复参与人状态
        participant.setStatus("AVAILABLE");
        participant.setWonPrizeId(null);
        participant.setWonPrizeName(null);
        participant.setWonTime(null);
        participantRepository.save(participant);

        // 5. 更新奖项状态
        Prize prize = prizeRepository.findById(record.getPrizeId())
                .orElse(null);
        if (prize != null) {
            prize.setDrawnCount(Math.max(0, prize.getDrawnCount() - 1));
            if (prize.getDrawnCount() < prize.getCount()) {
                prize.setStatus("PENDING");
            }
            prizeRepository.save(prize);
        }

        log.info("撤销中奖：{} 的 {} 已撤销", participant.getName(), record.getPrizeName());
    }

    /**
     * 重置系统
     */
    @Transactional(rollbackFor = Exception.class)
    public void reset() {
        // 1. 重置所有人员状态
        List<Participant> allParticipants = participantRepository.findAll();
        for (Participant participant : allParticipants) {
            participant.setStatus("AVAILABLE");
            participant.setWonPrizeId(null);
            participant.setWonPrizeName(null);
            participant.setWonTime(null);
            participantRepository.save(participant);
        }

        // 2. 重置所有奖项状态
        List<Prize> allPrizes = prizeRepository.findAll();
        for (Prize prize : allPrizes) {
            prize.setStatus("PENDING");
            prize.setDrawnCount(0);
            prize.setDrawTime(null);
            prizeRepository.save(prize);
        }

        // 3. 清空抽奖记录（可选：也可以保留记录）
        lotteryRecordRepository.deleteAll();

        // 4. 清空作弊设置
        riggedSettingRepository.deleteAll();

        log.info("系统已重置");
    }
}
