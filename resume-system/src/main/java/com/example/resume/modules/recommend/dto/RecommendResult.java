package com.example.resume.modules.recommend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "推荐结果")
public class RecommendResult {

    @Schema(description = "岗位ID/简历ID(候选人)")
    private Long jobId;

    @Schema(description = "匹配度")
    private Integer matchScore;

    @Schema(description = "岗位名称/候选人昵称")
    private String jobTitle;

    @Schema(description = "公司/技能标签")
    private String company;

    @Schema(description = "地点/用户名")
    private String location;

    @Schema(description = "推荐理由类型")
    private String reasonType;

    @Schema(description = "薪资范围(K)")
    private String salary;

    @Schema(description = "候选人用户ID")
    private Long candidateUserId;

    @Schema(description = "工作年限")
    private Integer experienceYears;

    @Schema(description = "能力简介")
    private String abilitySummary;
}
