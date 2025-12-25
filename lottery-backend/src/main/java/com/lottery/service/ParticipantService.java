package com.lottery.service;

import com.lottery.entity.Participant;
import com.lottery.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 参与人员服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    /**
     * 查询所有人员
     */
    public List<Participant> findAll() {
        return participantRepository.findAll();
    }

    /**
     * 根据状态查询人员
     */
    public List<Participant> findByStatus(String status) {
        return participantRepository.findByStatus(status);
    }

    /**
     * 根据ID查询人员
     */
    public Participant findById(String id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("人员不存在"));
    }

    /**
     * 根据姓名查询人员
     */
    public Participant findByName(String name) {
        return participantRepository.findByName(name)
                .orElse(null);
    }

    /**
     * 添加人员
     */
    @Transactional(rollbackFor = Exception.class)
    public Participant add(Participant participant) {
        // 检查姓名是否重复
        if (participantRepository.existsByName(participant.getName())) {
            throw new RuntimeException("姓名已存在：" + participant.getName());
        }

        participant.setStatus("AVAILABLE");
        return participantRepository.save(participant);
    }

    /**
     * 批量添加人员
     */
    @Transactional(rollbackFor = Exception.class)
    public void addBatch(List<Participant> participants) {
        for (Participant participant : participants) {
            if (!participantRepository.existsByName(participant.getName())) {
                participant.setStatus("AVAILABLE");
                participantRepository.save(participant);
            } else {
                log.warn("姓名重复，跳过：{}", participant.getName());
            }
        }
    }

    /**
     * 更新人员
     */
    @Transactional(rollbackFor = Exception.class)
    public Participant update(String id, Participant participant) {
        Participant existing = findById(id);

        // 如果修改了姓名，检查是否重复
        if (!existing.getName().equals(participant.getName())) {
            if (participantRepository.existsByName(participant.getName())) {
                throw new RuntimeException("姓名已存在：" + participant.getName());
            }
        }

        existing.setName(participant.getName());
        existing.setEmployeeId(participant.getEmployeeId());
        existing.setDepartment(participant.getDepartment());

        return participantRepository.save(existing);
    }

    /**
     * 删除人员
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        Participant participant = findById(id);

        // 如果已经中奖，不允许删除
        if ("WON".equals(participant.getStatus())) {
            throw new RuntimeException("该人员已中奖，不能删除");
        }

        participantRepository.deleteById(id);
    }

    /**
     * 批量删除人员
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<String> ids) {
        for (String id : ids) {
            try {
                delete(id);
            } catch (Exception e) {
                log.error("删除人员失败：{}", id, e);
            }
        }
    }

    /**
     * 统计人数
     */
    public long countByStatus(String status) {
        return participantRepository.countByStatus(status);
    }

    /**
     * 获取统计信息
     */
    public ParticipantStatistics getStatistics() {
        long total = participantRepository.count();
        long available = countByStatus("AVAILABLE");
        long won = countByStatus("WON");

        ParticipantStatistics stats = new ParticipantStatistics();
        stats.setTotal(total);
        stats.setAvailable(available);
        stats.setWon(won);
        stats.setWinRate(total > 0 ? (double) won / total * 100 : 0.0);

        return stats;
    }

    /**
     * 人员统计信息
     */
    public static class ParticipantStatistics {
        private Long total;
        private Long available;
        private Long won;
        private Double winRate;

        public Long getTotal() { return total; }
        public void setTotal(Long total) { this.total = total; }
        public Long getAvailable() { return available; }
        public void setAvailable(Long available) { this.available = available; }
        public Long getWon() { return won; }
        public void setWon(Long won) { this.won = won; }
        public Double getWinRate() { return winRate; }
        public void setWinRate(Double winRate) { this.winRate = winRate; }
    }
}
