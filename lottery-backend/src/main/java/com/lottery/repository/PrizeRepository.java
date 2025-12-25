package com.lottery.repository;

import com.lottery.entity.Prize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 奖项Repository
 */
@Repository
public interface PrizeRepository extends JpaRepository<Prize, String> {

    /**
     * 根据状态查询奖项
     */
    List<Prize> findByStatus(String status);

    /**
     * 根据等级查询奖项
     */
    Optional<Prize> findByLevel(Integer level);

    /**
     * 检查等级是否存在
     */
    boolean existsByLevel(Integer level);

    /**
     * 按等级排序查询所有奖项
     */
    List<Prize> findAllByOrderByLevelAsc();

    /**
     * 统计各状态奖项数量
     */
    long countByStatus(String status);
}
