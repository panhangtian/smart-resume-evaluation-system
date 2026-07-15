package com.example.resume.modules.evaluate.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation_result")
public class EvaluationResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resumeId;

    private Long jobId;

    private Long userId;

    private Integer skillMatch;

    private Integer experienceMatch;

    private Integer educationMatch;

    private Integer overall;

    private String strengths;

    private String gaps;

    private String comment;

    private String rawJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
