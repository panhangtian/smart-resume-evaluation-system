package com.example.resume.modules.job.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job")
public class Job {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long hrId;

    private String title;

    private String company;

    private String location;

    private String description;

    /** 技能/要求标签（逗号分隔） */
    private String requirementTags;

    /** 岗位向量（JSON 数组） */
    private String embedding;

    private Integer salaryMin;

    private Integer salaryMax;

    /** 1已发布 0下架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
