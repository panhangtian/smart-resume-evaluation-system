package com.example.resume.modules.resume.dto;

import lombok.Data;

/**
 * 简历编辑 DTO
 */
@Data
public class ResumeEditDTO {
    private String parsedJson;
    private String abilityProfile;
    private String skills;
    private Integer yearsOfExperience;
}
