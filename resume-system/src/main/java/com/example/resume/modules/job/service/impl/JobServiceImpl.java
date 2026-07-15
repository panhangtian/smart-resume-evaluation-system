package com.example.resume.modules.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resume.ai.EmbeddingService;
import com.example.resume.common.BizException;
import com.example.resume.common.utils.VectorUtils;
import com.example.resume.modules.job.dto.JobDTO;
import com.example.resume.modules.job.entity.Job;
import com.example.resume.modules.job.mapper.JobMapper;
import com.example.resume.modules.job.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

    private final EmbeddingService embeddingService;

    @Override
    public Long create(JobDTO dto, Long hrId) {
        Job job = new Job();
        applyDto(job, dto);
        job.setHrId(hrId);
        job.setStatus(1);
        save(job);
        // 异步生成向量？这里同步处理，因为 HR 即时需要
        calcEmbedding(job);
        updateById(job);
        return job.getId();
    }

    @Override
    public void update(Long id, JobDTO dto) {
        Job job = getById(id);
        if (job == null) throw new BizException("岗位不存在");
        applyDto(job, dto);
        calcEmbedding(job);
        updateById(job);
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        Job job = getById(id);
        if (job == null) throw new BizException("岗位不存在");
        job.setStatus(status);
        updateById(job);
    }

    @Override
    public Job detail(Long id) {
        Job job = getById(id);
        if (job == null) throw new BizException("岗位不存在");
        return job;
    }

    @Override
    public List<Job> myJobs(Long hrId) {
        return lambdaQuery().eq(Job::getHrId, hrId).orderByDesc(Job::getCreateTime).list();
    }

    @Override
    public List<Job> available(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return allAvailable();
        }
        return lambdaQuery().eq(Job::getStatus, 1)
                .and(w -> w.like(Job::getTitle, keyword)
                        .or().like(Job::getDescription, keyword)
                        .or().like(Job::getRequirementTags, keyword))
                .orderByDesc(Job::getCreateTime).list();
    }

    @Override
    public List<Job> allAvailable() {
        return lambdaQuery().eq(Job::getStatus, 1).orderByDesc(Job::getCreateTime).list();
    }

    private void applyDto(Job job, JobDTO dto) {
        job.setTitle(dto.getTitle());
        job.setCompany(dto.getCompany());
        job.setLocation(dto.getLocation());
        job.setDescription(dto.getDescription());
        job.setRequirementTags(dto.getRequirementTags());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
    }

    private void calcEmbedding(Job job) {
        String text = (job.getTitle() == null ? "" : job.getTitle()) + " "
                + (job.getDescription() == null ? "" : job.getDescription()) + " "
                + (job.getRequirementTags() == null ? "" : job.getRequirementTags());
        float[] vec = embeddingService.embed(text);
        job.setEmbedding(VectorUtils.toJson(vec));
    }
}
