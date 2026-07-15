package com.example.resume.modules.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.resume.modules.job.dto.JobDTO;
import com.example.resume.modules.job.entity.Job;

import java.util.List;

public interface JobService extends IService<Job> {

    Long create(JobDTO dto, Long hrId);

    void update(Long id, JobDTO dto);

    void toggleStatus(Long id, Integer status);

    Job detail(Long id);

    List<Job> myJobs(Long hrId);

    List<Job> available(String keyword);

    List<Job> allAvailable();
}
