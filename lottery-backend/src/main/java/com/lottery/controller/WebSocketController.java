package com.lottery.controller;

import com.lottery.dto.LotteryResultDTO;
import com.lottery.entity.Participant;
import com.lottery.entity.Prize;
import com.lottery.service.AICommandService;
import com.lottery.service.LotteryService;
import com.lottery.service.ParticipantService;
import com.lottery.service.PrizeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket消息控制器
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final AICommandService aiCommandService;
    private final LotteryService lotteryService;
    private final PrizeService prizeService;
    private final ParticipantService participantService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 处理语音指令
     */
    @MessageMapping("/voice-command")
    @SendTo("/topic/command-result")
    public CommandResponse handleVoiceCommand(VoiceCommandMessage message) {
        log.info("收到语音指令：{}", message.getTranscript());

        CommandResponse response = new CommandResponse();

        try {
            // 解析指令
            AICommandService.CommandResult commandResult =
                    aiCommandService.parseCommand(message.getTranscript());

            response.setType(commandResult.getType());
            response.setReply(commandResult.getReply());
            response.setRawText(commandResult.getRawText());
            response.setParams(commandResult.getParams());
            response.setSuccess(true);

            // 根据指令类型执行对应操作
            switch (commandResult.getType()) {
                case "START_DRAW":
                    handleStartDraw(commandResult, response);
                    break;
                case "STOP_DRAW":
                    handleStopDraw(response);
                    break;
                case "RESET":
                    handleReset(response);
                    break;
                case "RIG":
                    handleRig(commandResult, response);
                    break;
                case "CANCEL":
                    handleCancel(commandResult, response);
                    break;
                default:
                    // WAKEUP, EASTER_EGG, CHAT 只需要返回回复即可
                    break;
            }

        } catch (Exception e) {
            log.error("处理语音指令失败", e);
            response.setSuccess(false);
            response.setReply("抱歉，指令执行失败：" + e.getMessage());
        }

        return response;
    }

    /**
     * 处理开始抽奖
     */
    private void handleStartDraw(AICommandService.CommandResult commandResult, CommandResponse response) {
        Integer prizeLevel = (Integer) commandResult.getParams().get("prizeLevel");
        Prize prize = prizeService.findByLevel(prizeLevel);

        if (prize == null) {
            response.setReply("抱歉，没有找到对应的奖项");
            return;
        }

        response.setData(Map.of(
            "action", "START_DRAW",
            "prize", prize
        ));
    }

    /**
     * 处理停止抽奖
     */
    private void handleStopDraw(CommandResponse response) {
        response.setData(Map.of("action", "STOP_DRAW"));
    }

    /**
     * 处理重置
     */
    private void handleReset(CommandResponse response) {
        lotteryService.reset();
        response.setData(Map.of("action", "RESET"));
    }

    /**
     * 处理作弊设置
     */
    private void handleRig(AICommandService.CommandResult commandResult, CommandResponse response) {
        String name = (String) commandResult.getParams().get("participantName");
        Integer level = (Integer) commandResult.getParams().get("prizeLevel");

        Participant participant = participantService.findByName(name);
        if (participant == null) {
            response.setReply("抱歉，没有找到名为 " + name + " 的人员");
            return;
        }

        Prize prize = prizeService.findByLevel(level);
        if (prize == null) {
            response.setReply("抱歉，没有找到对应的奖项");
            return;
        }

        response.setData(Map.of(
            "action", "RIG",
            "participant", participant,
            "prize", prize
        ));
    }

    /**
     * 处理撤销中奖
     */
    private void handleCancel(AICommandService.CommandResult commandResult, CommandResponse response) {
        String name = (String) commandResult.getParams().get("participantName");

        Participant participant = participantService.findByName(name);
        if (participant == null) {
            response.setReply("抱歉，没有找到名为 " + name + " 的人员");
            return;
        }

        if (!"WON".equals(participant.getStatus())) {
            response.setReply(name + " 还没有中奖呢");
            return;
        }

        lotteryService.cancelWin(participant.getId(), "AI指令");
        response.setData(Map.of(
            "action", "CANCEL",
            "participant", participant
        ));
    }

    /**
     * 广播抽奖结果
     */
    public void broadcastLotteryResult(LotteryResultDTO result) {
        messagingTemplate.convertAndSend("/topic/lottery-result", result);
    }

    /**
     * 语音指令消息
     */
    @Data
    public static class VoiceCommandMessage {
        private String transcript;
        private String sessionId;
    }

    /**
     * 指令响应
     */
    @Data
    public static class CommandResponse {
        private Boolean success;
        private String type;
        private String reply;
        private String rawText;
        private Map<String, Object> params;
        private Map<String, Object> data;
    }
}
