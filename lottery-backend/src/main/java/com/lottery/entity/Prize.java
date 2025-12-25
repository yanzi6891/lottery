package com.lottery.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 奖项实体
 */
@Entity
@Table(name = "prizes")
@EntityListeners(AuditingEntityListener.class)
@Data
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * 奖项名称（一等奖、二等奖等）
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 等级（1-一等奖，2-二等奖，数字越小等级越高）
     */
    @Column(nullable = false, unique = true)
    private Integer level;

    /**
     * 中奖人数
     */
    @Column(nullable = false)
    private Integer count = 1;

    /**
     * 奖品描述
     */
    @Column(length = 200)
    private String description;

    /**
     * 状态：PENDING-未抽取, DRAWING-抽取中, COMPLETED-已完成
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /**
     * 已抽取人数
     */
    @Column(nullable = false)
    private Integer drawnCount = 0;

    /**
     * 抽取时间
     */
    private LocalDateTime drawTime;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
