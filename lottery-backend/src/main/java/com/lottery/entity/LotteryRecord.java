package com.lottery.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 抽奖记录实体
 */
@Entity
@Table(name = "lottery_records")
@Data
public class LotteryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * 奖项ID
     */
    @Column(nullable = false)
    private String prizeId;

    /**
     * 奖项名称（冗余）
     */
    @Column(nullable = false, length = 50)
    private String prizeName;

    /**
     * 奖项等级（冗余）
     */
    @Column(nullable = false)
    private Integer prizeLevel;

    /**
     * 中奖人ID
     */
    @Column(nullable = false)
    private String participantId;

    /**
     * 中奖人姓名（冗余）
     */
    @Column(nullable = false, length = 50)
    private String participantName;

    /**
     * 操作类型：DRAW-正常抽取, RIGGED-作弊抽取, CANCELLED-已撤销
     */
    @Column(nullable = false, length = 20)
    private String action = "DRAW";

    /**
     * 是否已撤销
     */
    @Column(nullable = false)
    private Boolean isCancelled = false;

    /**
     * 撤销时间
     */
    private LocalDateTime cancelledTime;

    /**
     * 操作人
     */
    @Column(length = 50)
    private String operator;

    /**
     * 备注
     */
    @Column(length = 200)
    private String remark;

    /**
     * 抽奖时间
     */
    @Column(nullable = false)
    private LocalDateTime drawTime = LocalDateTime.now();

    /**
     * 创建时间
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
