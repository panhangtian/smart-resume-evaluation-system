package com.example.resume.modules.application.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resume.common.BizException;
import com.example.resume.modules.application.entity.Application;
import com.example.resume.modules.application.mapper.ApplicationMapper;
import com.example.resume.modules.application.service.ApplicationService;
import com.example.resume.modules.application.vo.ApplicationVO;
import com.example.resume.modules.job.entity.Job;
import com.example.resume.modules.job.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    private final JobMapper jobMapper;

    @Override
    public Long apply(Long resumeId, Long jobId, Long userId) {
        if (lambdaQuery().eq(Application::getUserId, userId)
                .eq(Application::getJobId, jobId).count() > 0) {
            throw new BizException("你已经投递过该岗位");
        }
        Application app = new Application();
        app.setResumeId(resumeId);
        app.setJobId(jobId);
        app.setUserId(userId);
        app.setStatus(0);
        save(app);
        return app.getId();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Application app = getById(id);
        if (app == null) throw new BizException("投递记录不存在");
        app.setStatus(status);
        updateById(app);
    }

    @Override
    public List<ApplicationVO> myApplications(Long userId) {
        List<Application> apps = lambdaQuery().eq(Application::getUserId, userId)
                .orderByDesc(Application::getCreateTime).list();
        if (apps.isEmpty()) return Collections.emptyList();

        List<Long> jobIds = apps.stream().map(Application::getJobId).collect(Collectors.toList());
        List<Job> jobs = jobMapper.selectBatchIds(jobIds);
        Map<Long, Job> jobMap = jobs.stream().collect(Collectors.toMap(Job::getId, j -> j));

        return apps.stream().map(app -> {
            ApplicationVO vo = new ApplicationVO();
            vo.setId(app.getId());
            vo.setResumeId(app.getResumeId());
            vo.setUserId(app.getUserId());
            vo.setJobId(app.getJobId());
            vo.setStatus(app.getStatus());
            vo.setCreateTime(app.getCreateTime());
            vo.setUpdateTime(app.getUpdateTime());
            Job job = jobMap.get(app.getJobId());
            if (job != null) {
                vo.setJobTitle(job.getTitle());
                vo.setJobCompany(job.getCompany());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Application> jobApplications(Long jobId) {
        return lambdaQuery().eq(Application::getJobId, jobId)
                .orderByDesc(Application::getCreateTime).list();
    }
}
