package com.lottery.repository;

import com.lottery.entity.RiggedSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 作弊设置Repository
 */
@Repository
public interface RiggedSettingRepository extends JpaRepository<RiggedSetting, String> {

    /**
     * 根据奖项ID和状态查询
     */
    List<RiggedSetting> findByPrizeIdAndStatus(String prizeId, String status);

    /**
     * 根据参与人ID和状态查询
     */
    List<RiggedSetting> findByParticipantIdAndStatus(String participantId, String status);

    /**
     * 检查是否存在待生效的设置
     */
    boolean existsByParticipantIdAndPrizeIdAndStatus(
            String participantId, String prizeId, String status);

    /**
     * 查询所有待生效的设置
     */
    List<RiggedSetting> findByStatus(String status);
}
