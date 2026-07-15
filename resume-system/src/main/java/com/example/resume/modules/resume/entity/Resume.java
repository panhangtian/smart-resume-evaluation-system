package com.example.resume.modules.resume.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历主表
 */
@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 原始文件名 */
    private String fileName;

    /** 存储 key（本地/MinIO） */
    private String storageKey;

    /** 解析状态：0 待解析 1 解析中 2 已解析 3 解析失败 */
    private Integer status;

    /** 提取的纯文本 */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String rawText;

    /** AI 解析的结构化 JSON */
    private String parsedJson;

    /** 能力画像（一句话/段落） */
    private String abilityProfile;

    /** 技能标签（逗号分隔） */
    private String skills;

    /** 工作年限 */
    private Integer yearsOfExperience;

    /** 简历向量（JSON 数组） */
    private String embedding;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
