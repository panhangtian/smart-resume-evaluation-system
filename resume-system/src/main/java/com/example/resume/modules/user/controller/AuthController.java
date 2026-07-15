package com.example.resume.modules.user.controller;

import com.example.resume.common.R;
import com.example.resume.modules.user.dto.ProfileDTO;
import com.example.resume.modules.user.dto.LoginDTO;
import com.example.resume.modules.user.dto.RegisterDTO;
import com.example.resume.modules.user.entity.User;
import com.example.resume.modules.user.service.UserService;
import com.example.resume.modules.user.vo.LoginVO;
import com.example.resume.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "注册（求职者/HR/管理员）")
    public R<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return R.ok(userService.register(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(userService.login(dto.getUsername(), dto.getPassword()));
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public R<User> info() {
        return R.ok(userService.currentUser(SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人资料")
    public R<Void> updateProfile(@RequestBody ProfileDTO dto) {
        userService.updateProfile(SecurityUtils.getCurrentUserId(),
                dto.getNickname(), dto.getEmail(), dto.getPhone());
        return R.ok();
    }
}
