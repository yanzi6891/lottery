package com.lottery.repository;

import com.lottery.entity.LotteryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 抽奖记录Repository
 */
@Repository
public interface LotteryRecordRepository extends JpaRepository<LotteryRecord, String> {

    /**
     * 根据奖项ID查询记录
     */
    List<LotteryRecord> findByPrizeId(String prizeId);

    /**
     * 根据参与人ID查询记录
     */
    List<LotteryRecord> findByParticipantId(String participantId);

    /**
     * 根据奖项ID和撤销状态查询
     */
    List<LotteryRecord> findByPrizeIdAndIsCancelled(String prizeId, Boolean isCancelled);

    /**
     * 根据参与人ID和撤销状态查询
     */
    List<LotteryRecord> findByParticipantIdAndIsCancelled(String participantId, Boolean isCancelled);

    /**
     * 查询参与人最新的中奖记录
     */
    Optional<LotteryRecord> findFirstByParticipantIdAndIsCancelledOrderByDrawTimeDesc(
            String participantId, Boolean isCancelled);

    /**
     * 查询所有有效记录（未撤销）
     */
    List<LotteryRecord> findByIsCancelled(Boolean isCancelled);

    /**
     * 根据撤销状态查询并按抽奖时间倒序排列
     */
    List<LotteryRecord> findByIsCancelledOrderByDrawTimeDesc(Boolean isCancelled);

    /**
     * 按抽奖时间倒序查询
     */
    List<LotteryRecord> findAllByOrderByDrawTimeDesc();
}
