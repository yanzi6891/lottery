package com.lottery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 年会抽奖系统主启动类
 *
 * @date 2025-12-23
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class LotteryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LotteryApplication.class, args);
        System.out.println("\n" +
                "========================================\n" +
                "  年会抽奖系统启动成功！\n" +
                "  访问地址：http://localhost:8080\n" +
                "  H2控制台：http://localhost:8080/h2-console\n" +
                "========================================\n");
    }
}
