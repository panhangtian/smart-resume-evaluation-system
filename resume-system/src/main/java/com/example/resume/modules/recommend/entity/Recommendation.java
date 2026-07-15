package com.example.resume.modules.recommend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("recommendation")
public class Recommendation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resumeId;

    private Long userId;

    private Long jobId;

    private Integer matchScore;

    private String reasonType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
