package com.example.resume.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.resume.modules.user.dto.RegisterDTO;
import com.example.resume.modules.user.entity.User;
import com.example.resume.modules.user.vo.LoginVO;

public interface UserService extends IService<User> {

    LoginVO register(RegisterDTO dto);

    LoginVO login(String username, String password);

    User currentUser(Long userId);

    /** 更新用户个人资料 */
    void updateProfile(Long userId, String nickname, String email, String phone);
}
