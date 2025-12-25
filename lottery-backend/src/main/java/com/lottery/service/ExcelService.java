package com.lottery.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.lottery.entity.Participant;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel导入导出服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {

    private final ParticipantService participantService;

    /**
     * 导入人员Excel
     */
    public ImportResult importParticipants(MultipartFile file) {
        ImportResult result = new ImportResult();

        try {
            List<ParticipantExcel> dataList = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            EasyExcel.read(file.getInputStream(), ParticipantExcel.class,
                    new AnalysisEventListener<ParticipantExcel>() {
                        @Override
                        public void invoke(ParticipantExcel data, AnalysisContext context) {
                            dataList.add(data);
                        }

                        @Override
                        public void doAfterAllAnalysed(AnalysisContext context) {
                            log.info("Excel读取完成，共{}行数据", dataList.size());
                        }
                    }).sheet().doRead();

            // 验证和转换数据
            int rowNum = 2; // 从第2行开始（第1行是表头）
            for (ParticipantExcel excel : dataList) {
                try {
                    // 验证必填字段
                    if (excel.getName() == null || excel.getName().trim().isEmpty()) {
                        errors.add("第" + rowNum + "行：姓名不能为空");
                        rowNum++;
                        continue;
                    }

                    // 转换为实体
                    Participant participant = new Participant();
                    participant.setName(excel.getName().trim());
                    participant.setEmployeeId(excel.getEmployeeId());
                    participant.setDepartment(excel.getDepartment());

                    // 尝试添加
                    try {
                        participantService.add(participant);
                        result.setSuccess(result.getSuccess() + 1);
                    } catch (Exception e) {
                        errors.add("第" + rowNum + "行：" + e.getMessage());
                        result.setFailed(result.getFailed() + 1);
                    }

                } catch (Exception e) {
                    errors.add("第" + rowNum + "行：数据格式错误");
                    result.setFailed(result.getFailed() + 1);
                }
                rowNum++;
            }

            result.setTotal(dataList.size());
            result.setErrors(errors);

        } catch (IOException e) {
            log.error("读取Excel文件失败", e);
            throw new RuntimeException("读取Excel文件失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * Excel数据模型
     */
    @Data
    public static class ParticipantExcel {
        private String name;        // 姓名
        private String employeeId;  // 工号
        private String department;  // 部门
    }

    /**
     * 导入结果
     */
    @Data
    public static class ImportResult {
        private Integer total = 0;      // 总数
        private Integer success = 0;    // 成功数
        private Integer failed = 0;     // 失败数
        private List<String> errors = new ArrayList<>();  // 错误信息
    }
}
