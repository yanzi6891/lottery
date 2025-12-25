package com.lottery.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 作弊设置实体
 */
@Entity
@Table(name = "rigged_settings")
@EntityListeners(AuditingEntityListener.class)
@Data
public class RiggedSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * 参与人ID
     */
    @Column(nullable = false)
    private String participantId;

    /**
     * 参与人姓名（冗余）
     */
    @Column(nullable = false, length = 50)
    private String participantName;

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
     * 状态：PENDING-待生效, USED-已生效, CANCELLED-已取消
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /**
     * 生效时间
     */
    private LocalDateTime usedTime;

    /**
     * 设置人
     */
    @Column(length = 50)
    private String operator;

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
