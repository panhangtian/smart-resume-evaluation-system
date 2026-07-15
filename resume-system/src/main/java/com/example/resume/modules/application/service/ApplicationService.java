package com.example.resume.modules.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.resume.modules.application.entity.Application;
import com.example.resume.modules.application.vo.ApplicationVO;

import java.util.List;

public interface ApplicationService extends IService<Application> {

    Long apply(Long resumeId, Long jobId, Long userId);

    void updateStatus(Long id, Integer status);

    List<ApplicationVO> myApplications(Long userId);

    List<Application> jobApplications(Long jobId);
}
