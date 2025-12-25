package com.lottery.repository;

import com.lottery.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 参与人员Repository
 */
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, String> {

    /**
     * 根据状态查询人员
     */
    List<Participant> findByStatus(String status);

    /**
     * 根据姓名查询
     */
    Optional<Participant> findByName(String name);

    /**
     * 检查姓名是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据奖项ID查询中奖人员
     */
    List<Participant> findByWonPrizeId(String wonPrizeId);

    /**
     * 统计各状态人数
     */
    long countByStatus(String status);
}
