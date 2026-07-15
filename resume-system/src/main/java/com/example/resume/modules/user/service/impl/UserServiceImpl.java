package com.example.resume.modules.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resume.common.BizException;
import com.example.resume.common.constants.RoleConstants;
import com.example.resume.modules.user.dto.RegisterDTO;
import com.example.resume.modules.user.entity.User;
import com.example.resume.modules.user.mapper.UserMapper;
import com.example.resume.modules.user.service.UserService;
import com.example.resume.modules.user.vo.LoginVO;
import com.example.resume.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO register(RegisterDTO dto) {
        if (lambdaQuery().eq(User::getUsername, dto.getUsername()).count() > 0) {
            throw new BizException("账号已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setNickname(dto.getNickname() == null ? dto.getUsername() : dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        save(user);

        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generate(user.getId(), user.getRole()));
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public LoginVO login(String username, String password) {
        User user = lambdaQuery().eq(User::getUsername, username).one();
        if (user == null) {
            throw new BizException("账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BizException("账号已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BizException("账号或密码错误");
        }
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generate(user.getId(), user.getRole()));
        vo.setUserId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public User currentUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    @Override
    public void updateProfile(Long userId, String nickname, String email, String phone) {
        User user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setNickname(nickname);
        user.setEmail(email);
        user.setPhone(phone);
        updateById(user);
    }
}
