package com.aiwps.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("study_record")
public class StudyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long questionId;
    private Long knowledgePointId;
    private Integer result; // 0: 错误, 1: 正确
    private Integer timeSpent; // 答题耗时（秒）
    private LocalDateTime studiedAt;
    private LocalDateTime nextReviewAt; // 艾宾浩斯下次复习时间
    private Integer reviewCount; // 复习次数
    private String ebbinghausStep; // 艾宾浩斯记忆阶段
    private LocalDateTime createdAt;
}
