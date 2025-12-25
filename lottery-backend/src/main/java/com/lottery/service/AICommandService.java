package com.lottery.service;

import cn.hutool.core.util.RandomUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI指令解析服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AICommandService {

    private final PrizeService prizeService;
    private final ParticipantService participantService;

    /**
     * 解析语音指令
     */
    public CommandResult parseCommand(String transcript) {
        CommandResult result = new CommandResult();
        result.setRawText(transcript);

        // 移除标点符号、唤醒词和多余空格
        String text = transcript.replaceAll("[，。！？、]", "")
                                .replaceAll("小宝+", "")  // 移除唤醒词"小宝"
                                .trim();

        // 1. 如果只有唤醒词，没有实际指令
        if (text.isEmpty()) {
            result.setType("WAKEUP");
            result.setReply(getRandomReply(WAKEUP_REPLIES));
            return result;
        }

        // 2. 彩蛋：你听谁的话
        if (text.contains("你听谁") || text.contains("听谁的话") || text.contains("听谁话")) {
            result.setType("EASTER_EGG");
            result.setReply("我爸爸是家勇，我当然听我爸爸的话");
            return result;
        }

        // 3. 开始抽奖
        Pattern startPattern = Pattern.compile("(特|一|二|三|四|五|幸运)?等奖?(开始|抽奖|来)");
        Matcher startMatcher = startPattern.matcher(text);
        boolean startMatched = startMatcher.find();

        if (startMatched || text.contains("开始") || text.contains("抽奖")) {
            result.setType("START_DRAW");

            // 提取奖项等级
            String prizeLevel = null;
            if (startMatched) {
                prizeLevel = startMatcher.group(1);
            }
            Integer level = parsePrizeLevel(prizeLevel);

            Map<String, Object> params = new HashMap<>();
            params.put("prizeLevel", level);
            result.setParams(params);
            result.setReply(getRandomReply(START_REPLIES));
            return result;
        }

        // 4. 停止抽奖
        if (text.equals("停") || text.equals("暂停") || text.equals("停止") ||
            text.equals("stop") || text.contains("停下")) {
            result.setType("STOP_DRAW");
            result.setReply(getRandomReply(STOP_REPLIES));
            return result;
        }

        // 5. 重置系统
        if (text.contains("重置") || text.contains("重来") || text.contains("重新开始") ||
            text.contains("清空") || text.contains("reset")) {
            result.setType("RESET");
            result.setReply(getRandomReply(RESET_REPLIES));
            return result;
        }

        // 6. 作弊设置：给某某设置X等奖
        Pattern rigPattern = Pattern.compile("给(.+?)(设置|安排|弄个|来个)(特|一|二|三|四|五|幸运)?等奖");
        Matcher rigMatcher = rigPattern.matcher(text);
        if (rigMatcher.find()) {
            result.setType("RIG");

            String name = rigMatcher.group(1).trim();
            String prizeLevel = rigMatcher.group(3);
            Integer level = parsePrizeLevel(prizeLevel);

            Map<String, Object> params = new HashMap<>();
            params.put("participantName", name);
            params.put("prizeLevel", level);
            result.setParams(params);
            result.setReply(getRandomReply(RIG_REPLIES));
            return result;
        }

        // 7. 撤销中奖：撤销某某的奖项
        Pattern cancelPattern = Pattern.compile("撤销(.+?)的?(奖项|中奖|奖)");
        Matcher cancelMatcher = cancelPattern.matcher(text);
        if (cancelMatcher.find()) {
            result.setType("CANCEL");

            String name = cancelMatcher.group(1).trim();
            Map<String, Object> params = new HashMap<>();
            params.put("participantName", name);
            result.setParams(params);
            result.setReply(getRandomReply(CANCEL_REPLIES));
            return result;
        }

        // 8. 未匹配到指令，当作闲聊
        result.setType("CHAT");
        result.setReply(getChatReply(text));
        return result;
    }

    /**
     * 解析奖项等级
     */
    private Integer parsePrizeLevel(String prizeText) {
        if (prizeText == null) {
            // 返回下一个待抽取的奖项等级
            var prize = prizeService.getNextPendingPrize();
            return prize != null ? prize.getLevel() : 1;
        }

        Map<String, Integer> levelMap = new HashMap<>();
        levelMap.put("特", 1);
        levelMap.put("一", 1);
        levelMap.put("二", 2);
        levelMap.put("三", 3);
        levelMap.put("四", 4);
        levelMap.put("五", 5);
        levelMap.put("幸运", 6);

        return levelMap.getOrDefault(prizeText, 1);
    }

    /**
     * 获取随机回复
     */
    private String getRandomReply(String[] replies) {
        return replies[RandomUtil.randomInt(replies.length)];
    }

    /**
     * 获取闲聊回复
     */
    private String getChatReply(String text) {
        // 简单的关键词匹配
        if (text.contains("你好") || text.contains("hello") || text.contains("hi")) {
            return "您好呀！";
        }
        if (text.contains("你是谁") || text.contains("你叫什么")) {
            return "我是小宝，您的抽奖助手";
        }
        if (text.contains("天气")) {
            return "今天适合抽大奖！";
        }
        if (text.contains("加油") || text.contains("冲")) {
            return "冲冲冲！";
        }
        if (text.contains("谢谢") || text.contains("感谢")) {
            return "不客气~";
        }

        // 默认回复
        String[] defaultReplies = {
                "我在听呢",
                "嗯嗯",
                "收到",
                "明白",
                "好的"
        };
        return getRandomReply(defaultReplies);
    }

    // 预设回复库
    private static final String[] WAKEUP_REPLIES = {
            "小宝在，请您下达指令",
            "小宝在呢，有事您说话",
            "在的，请吩咐",
            "我在，您说"
    };

    private static final String[] START_REPLIES = {
            "收到，马上开始！",
            "好嘞，开始抽奖！",
            "来了来了！",
            "准备好了，开始！"
    };

    private static final String[] STOP_REPLIES = {
            "好嘞，已经停了",
            "停！",
            "收到，停止",
            "定格！"
    };

    private static final String[] RESET_REPLIES = {
            "明白，已重置",
            "好的，已经清空了",
            "收到，重新开始",
            "已重置完成"
    };

    private static final String[] RIG_REPLIES = {
            "没问题，已安排上了",
            "收到，已设置",
            "明白，包在我身上",
            "好的，稳了"
    };

    private static final String[] CANCEL_REPLIES = {
            "好嘞，已撤销",
            "明白，重新加入抽奖池了",
            "收到，已取消",
            "好的，已经撤销了"
    };

    /**
     * 指令解析结果
     */
    @Data
    public static class CommandResult {
        private String type;                    // 指令类型
        private Map<String, Object> params;     // 参数
        private String reply;                   // AI回复
        private String rawText;                 // 原始文本
    }
}
