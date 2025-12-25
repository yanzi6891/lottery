package com.lottery.service;

import com.lottery.entity.Prize;
import com.lottery.repository.ParticipantRepository;
import com.lottery.repository.PrizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 奖项服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PrizeService {

    private final PrizeRepository prizeRepository;
    private final ParticipantRepository participantRepository;

    /**
     * 查询所有奖项（按等级排序）
     */
    public List<Prize> findAll() {
        return prizeRepository.findAllByOrderByLevelAsc();
    }

    /**
     * 根据状态查询奖项
     */
    public List<Prize> findByStatus(String status) {
        return prizeRepository.findByStatus(status);
    }

    /**
     * 根据ID查询奖项
     */
    public Prize findById(String id) {
        return prizeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("奖项不存在"));
    }

    /**
     * 根据等级查询奖项
     */
    public Prize findByLevel(Integer level) {
        return prizeRepository.findByLevel(level)
                .orElse(null);
    }

    /**
     * 创建奖项
     */
    @Transactional(rollbackFor = Exception.class)
    public Prize create(Prize prize) {
        // 检查等级是否重复
        if (prizeRepository.existsByLevel(prize.getLevel())) {
            throw new RuntimeException("等级已存在：" + prize.getLevel());
        }

        // 检查中奖人数是否合理
        long availableCount = participantRepository.countByStatus("AVAILABLE");
        if (prize.getCount() > availableCount) {
            throw new RuntimeException("中奖人数超过可用人数");
        }

        prize.setStatus("PENDING");
        prize.setDrawnCount(0);
        return prizeRepository.save(prize);
    }

    /**
     * 更新奖项
     */
    @Transactional(rollbackFor = Exception.class)
    public Prize update(String id, Prize prize) {
        Prize existing = findById(id);

        // 如果已经开始抽奖，不允许修改关键字段
        if (existing.getDrawnCount() > 0) {
            throw new RuntimeException("该奖项已开始抽奖，不能修改");
        }

        // 如果修改了等级，检查是否重复
        if (!existing.getLevel().equals(prize.getLevel())) {
            if (prizeRepository.existsByLevel(prize.getLevel())) {
                throw new RuntimeException("等级已存在：" + prize.getLevel());
            }
        }

        existing.setName(prize.getName());
        existing.setLevel(prize.getLevel());
        existing.setCount(prize.getCount());
        existing.setDescription(prize.getDescription());

        return prizeRepository.save(existing);
    }

    /**
     * 删除奖项
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        Prize prize = findById(id);

        // 如果已经开始抽奖，不允许删除
        if (prize.getDrawnCount() > 0) {
            throw new RuntimeException("该奖项已开始抽奖，不能删除");
        }

        prizeRepository.deleteById(id);
    }

    /**
     * 获取下一个待抽取的奖项（按等级从高到低）
     */
    public Prize getNextPendingPrize() {
        List<Prize> pendingPrizes = prizeRepository.findByStatus("PENDING");
        if (pendingPrizes.isEmpty()) {
            return null;
        }

        // 返回等级最高的（level最大的）
        return pendingPrizes.stream()
                .max((p1, p2) -> Integer.compare(p2.getLevel(), p1.getLevel()))
                .orElse(null);
    }

    /**
     * 统计信息
     */
    public PrizeStatistics getStatistics() {
        long total = prizeRepository.count();
        long pending = prizeRepository.countByStatus("PENDING");
        long completed = prizeRepository.countByStatus("COMPLETED");

        // 计算总奖品数
        List<Prize> allPrizes = prizeRepository.findAll();
        int totalWinnerSlots = allPrizes.stream()
                .mapToInt(Prize::getCount)
                .sum();

        int totalDrawn = allPrizes.stream()
                .mapToInt(Prize::getDrawnCount)
                .sum();

        PrizeStatistics stats = new PrizeStatistics();
        stats.setTotalPrizes(total);
        stats.setPendingPrizes(pending);
        stats.setCompletedPrizes(completed);
        stats.setTotalWinnerSlots(totalWinnerSlots);
        stats.setTotalDrawn(totalDrawn);

        return stats;
    }

    /**
     * 奖项统计信息
     */
    public static class PrizeStatistics {
        private Long totalPrizes;
        private Long pendingPrizes;
        private Long completedPrizes;
        private Integer totalWinnerSlots;
        private Integer totalDrawn;

        public Long getTotalPrizes() { return totalPrizes; }
        public void setTotalPrizes(Long totalPrizes) { this.totalPrizes = totalPrizes; }
        public Long getPendingPrizes() { return pendingPrizes; }
        public void setPendingPrizes(Long pendingPrizes) { this.pendingPrizes = pendingPrizes; }
        public Long getCompletedPrizes() { return completedPrizes; }
        public void setCompletedPrizes(Long completedPrizes) { this.completedPrizes = completedPrizes; }
        public Integer getTotalWinnerSlots() { return totalWinnerSlots; }
        public void setTotalWinnerSlots(Integer totalWinnerSlots) { this.totalWinnerSlots = totalWinnerSlots; }
        public Integer getTotalDrawn() { return totalDrawn; }
        public void setTotalDrawn(Integer totalDrawn) { this.totalDrawn = totalDrawn; }
    }
}
