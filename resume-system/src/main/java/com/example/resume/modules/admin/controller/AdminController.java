package com.example.resume.modules.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.resume.common.R;
import com.example.resume.modules.evaluate.mapper.EvaluationResultMapper;
import com.example.resume.modules.job.entity.Job;
import com.example.resume.modules.job.mapper.JobMapper;
import com.example.resume.modules.recommend.mapper.RecommendationMapper;
import com.example.resume.modules.resume.entity.Resume;
import com.example.resume.modules.resume.mapper.ResumeMapper;
import com.example.resume.modules.user.entity.User;
import com.example.resume.modules.user.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理看板")
public class AdminController {

    private final UserMapper userMapper;
    private final ResumeMapper resumeMapper;
    private final JobMapper jobMapper;
    private final EvaluationResultMapper evaluationResultMapper;
    private final RecommendationMapper recommendationMapper;

    @GetMapping("/dashboard")
    @Operation(summary = "看板统计")
    public R<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", userMapper.selectCount(null));
        data.put("totalHr", userMapper.selectCount(new QueryWrapper<User>().eq("role", "HR")));
        data.put("totalJobseekers", userMapper.selectCount(new QueryWrapper<User>().eq("role", "JOBSEEKER")));
        data.put("totalResumes", resumeMapper.selectCount(null));
        data.put("parsedResumes", resumeMapper.selectCount(new QueryWrapper<Resume>().eq("status", 2)));
        data.put("totalJobs", jobMapper.selectCount(null));
        data.put("activeJobs", jobMapper.selectCount(new QueryWrapper<Job>().eq("status", 1)));
        data.put("totalEvaluations", evaluationResultMapper.selectCount(null));
        data.put("totalRecommendations", recommendationMapper.selectCount(null));
        return R.ok(data);
    }
}
