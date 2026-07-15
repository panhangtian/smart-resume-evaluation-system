package com.example.resume.modules.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "岗位创建/更新请求")
public class JobDTO {

    @NotBlank(message = "岗位名称不能为空")
    @Schema(description = "岗位名称")
    private String title;

    @Schema(description = "公司名称")
    private String company;

    @Schema(description = "工作地点")
    private String location;

    @Schema(description = "岗位描述")
    private String description;

    @Schema(description = "技能/要求标签（逗号分隔）")
    private String requirementTags;

    @Schema(description = "薪资下限(K)")
    private Integer salaryMin;

    @Schema(description = "薪资上限(K)")
    private Integer salaryMax;
}
