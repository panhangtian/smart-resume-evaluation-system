package com.example.resume.modules.user.dto;

import lombok.Data;

/**
 * 个人资料更新 DTO
 */
@Data
public class ProfileDTO {
    private String nickname;
    private String email;
    private String phone;
}
