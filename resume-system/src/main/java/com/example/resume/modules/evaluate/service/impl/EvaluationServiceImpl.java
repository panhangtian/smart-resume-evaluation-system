package com.example.resume.modules.evaluate.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resume.ai.AiClient;
import com.example.resume.ai.PromptTemplate;
import com.example.resume.common.BizException;
import com.example.resume.modules.evaluate.entity.EvaluationResult;
import com.example.resume.modules.evaluate.mapper.EvaluationResultMapper;
import com.example.resume.modules.evaluate.service.EvaluationService;
import com.example.resume.modules.job.entity.Job;
import com.example.resume.modules.job.service.JobService;
import com.example.resume.modules.resume.entity.Resume;
import com.example.resume.modules.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl extends ServiceImpl<EvaluationResultMapper, EvaluationResult> implements EvaluationService {

    private final ResumeService resumeService;
    private final JobService jobService;
    private final AiClient aiClient;
    private final PromptTemplate promptTemplate;

    @Override
    public EvaluationResult evaluate(Long resumeId, Long jobId) {
        Resume resume = resumeService.getById(resumeId);
        if (resume == null || resume.getStatus() != 2) {
            throw new BizException("简历不存在或未完成解析");
        }
        Job job = jobService.getById(jobId);
        if (job == null || job.getStatus() == null || job.getStatus() == 0) {
            throw new BizException("岗位不存在或已下线");
        }

        String resumeText = resume.getRawText() != null ? resume.getRawText()
                : (resume.getSkills() != null ? resume.getSkills() : "");
        String jobText = (job.getTitle() != null ? job.getTitle() + "\n" : "")
                + (job.getDescription() != null ? job.getDescription() + "\n" : "")
                + (job.getRequirementTags() != null ? job.getRequirementTags() : "");

        String userPrompt = promptTemplate.evaluateUser(resumeText, jobText);
        String systemPrompt = promptTemplate.evaluateSystem();
        String raw = aiClient.chat(systemPrompt, userPrompt);

        JSONObject obj = JSON.parseObject(extractJson(raw));
        EvaluationResult result = new EvaluationResult();
        result.setResumeId(resumeId);
        result.setJobId(jobId);
        result.setUserId(resume.getUserId());
        result.setSkillMatch(obj.getInteger("skillMatch"));
        result.setExperienceMatch(obj.getInteger("experienceMatch"));
        result.setEducationMatch(obj.getInteger("educationMatch"));
        result.setOverall(obj.getInteger("overall"));
        result.setStrengths(toJsonArrayString(obj.getJSONArray("strengths")));
        result.setGaps(toJsonArrayString(obj.getJSONArray("gaps")));
        result.setComment(obj.getString("comment"));
        result.setRawJson(raw);
        save(result);
        return result;
    }

    @Override
    public List<EvaluationResult> byResume(Long resumeId) {
        return lambdaQuery().eq(EvaluationResult::getResumeId, resumeId)
                .orderByDesc(EvaluationResult::getCreateTime).list();
    }

    @Override
    public List<EvaluationResult> byUser(Long userId) {
        return lambdaQuery().eq(EvaluationResult::getUserId, userId)
                .orderByDesc(EvaluationResult::getCreateTime).list();
    }

    @Override
    public EvaluationResult latest(Long resumeId, Long jobId) {
        return lambdaQuery().eq(EvaluationResult::getResumeId, resumeId)
                .eq(EvaluationResult::getJobId, jobId)
                .orderByDesc(EvaluationResult::getCreateTime)
                .last("limit 1").one();
    }

    private String extractJson(String text) {
        if (text == null) return "{}";
        String t = text.trim();
        int s = t.indexOf('{');
        int e = t.lastIndexOf('}');
        return s >= 0 && e > s ? t.substring(s, e + 1) : t;
    }

    private String toJsonArrayString(JSONArray arr) {
        return arr == null ? "[]" : arr.toJSONString();
    }
}
