package com.example.resume.modules.application.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "投递记录视图")
public class ApplicationVO {

    @Schema(description = "投递ID")
    private Long id;

    @Schema(description = "简历ID")
    private Long resumeId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "岗位ID")
    private Long jobId;

    @Schema(description = "岗位名称")
    private String jobTitle;

    @Schema(description = "公司名称")
    private String jobCompany;

    @Schema(description = "投递状态(0待处理1已查看2面试3通过4不通过)")
    private Integer status;

    @Schema(description = "投递时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
