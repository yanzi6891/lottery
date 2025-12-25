package com.lottery.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 参与人员实体
 */
@Entity
@Table(name = "participants")
@EntityListeners(AuditingEntityListener.class)
@Data
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * 姓名（唯一）
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 工号
     */
    @Column(length = 20)
    private String employeeId;

    /**
     * 部门
     */
    @Column(length = 50)
    private String department;

    /**
     * 状态：AVAILABLE-可参与, WON-已中奖
     */
    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE";

    /**
     * 中奖奖项ID
     */
    private String wonPrizeId;

    /**
     * 中奖奖项名称（冗余字段）
     */
    private String wonPrizeName;

    /**
     * 中奖时间
     */
    private LocalDateTime wonTime;

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
