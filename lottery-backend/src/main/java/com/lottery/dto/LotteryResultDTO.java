package com.lottery.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 抽奖结果DTO
 */
@Data
public class LotteryResultDTO {

    /**
     * 奖项ID
     */
    private String prizeId;

    /**
     * 奖项名称
     */
    private String prizeName;

    /**
     * 奖项等级
     */
    private Integer prizeLevel;

    /**
     * 中奖人员列表
     */
    private List<WinnerDTO> winners;

    /**
     * 抽奖时间
     */
    private LocalDateTime drawTime;

    @Data
    public static class WinnerDTO {
        private String id;
        private String name;
        private String employeeId;
        private String department;
        private Boolean isRigged; // 是否通过作弊设置中奖
    }
}
