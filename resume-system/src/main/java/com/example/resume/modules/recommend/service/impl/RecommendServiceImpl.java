package com.example.resume.modules.recommend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resume.common.utils.VectorUtils;
import com.example.resume.modules.job.entity.Job;
import com.example.resume.modules.job.service.JobService;
import com.example.resume.modules.recommend.dto.RecommendResult;
import com.example.resume.modules.recommend.entity.Recommendation;
import com.example.resume.modules.recommend.mapper.RecommendationMapper;
import com.example.resume.modules.recommend.service.RecommendService;
import com.example.resume.modules.resume.entity.Resume;
import com.example.resume.modules.resume.service.ResumeService;
import com.example.resume.modules.user.entity.User;
import com.example.resume.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl extends ServiceImpl<RecommendationMapper, Recommendation> implements RecommendService {

    private final ResumeService resumeService;
    private final JobService jobService;
    private final UserMapper userMapper;

    @Override
    public List<RecommendResult> recommendJobsForUser(Long userId, int topN) {
        Resume resume = resumeService.latestParsed(userId);
        if (resume == null || resume.getEmbedding() == null) return List.of();

        float[] resumeVec = VectorUtils.parse(resume.getEmbedding());
        List<Job> jobs = jobService.allAvailable().stream()
                .filter(j -> j.getEmbedding() != null && !j.getEmbedding().isBlank())
                .toList();

        List<JobScore> scored = new ArrayList<>();
        for (Job job : jobs) {
            double sim = VectorUtils.cosine(resumeVec, VectorUtils.parse(job.getEmbedding()));
            scored.add(new JobScore(job.getId(), sim, VectorUtils.toScore(sim)));
        }
        scored.sort((a, b) -> Double.compare(b.score, a.score));
        if (scored.size() > topN) scored = scored.subList(0, topN);

        // 保存推荐记录
        for (JobScore js : scored) {
            Recommendation rec = new Recommendation();
            rec.setResumeId(resume.getId());
            rec.setUserId(userId);
            rec.setJobId(js.jobId);
            rec.setMatchScore(js.score100);
            rec.setReasonType("hybrid");
            save(rec);
        }

        // 组装结果
        Map<Long, Job> jobMap = jobs.stream().collect(Collectors.toMap(Job::getId, j -> j));
        return scored.stream().map(js -> {
            Job j = jobMap.get(js.jobId);
            if (j == null) return null;
            RecommendResult r = new RecommendResult();
            r.setJobId(j.getId());
            r.setMatchScore(js.score100);
            r.setJobTitle(j.getTitle());
            r.setCompany(j.getCompany());
            r.setLocation(j.getLocation());
            r.setReasonType("hybrid");
            r.setSalary(j.getSalaryMin() + "-" + j.getSalaryMax() + "K");
            return r;
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public List<RecommendResult> recommendCandidatesForJob(Long jobId, int topN) {
        Job job = jobService.getById(jobId);
        if (job == null) return List.of();

        // 所有已解析简历
        List<Resume> allResumes = resumeService.lambdaQuery()
                .eq(Resume::getStatus, 2)
                .isNotNull(Resume::getSkills)
                .ne(Resume::getSkills, "")
                .list();
        if (allResumes.isEmpty()) return List.of();

        Map<Long, Resume> resumeMap = allResumes.stream()
                .collect(Collectors.toMap(Resume::getId, r -> r));

        Set<Long> matchedIds = new HashSet<>();
        List<JobScore> scored = new ArrayList<>();
        boolean hasJobVec = job.getEmbedding() != null && !job.getEmbedding().isBlank();

        // 1. 向量匹配
        if (hasJobVec) {
            float[] jobVec = VectorUtils.parse(job.getEmbedding());
            for (Resume r : allResumes) {
                if (r.getEmbedding() == null || r.getEmbedding().isBlank()) continue;
                double sim = VectorUtils.cosine(jobVec, VectorUtils.parse(r.getEmbedding()));
                int score100 = VectorUtils.toScore(sim);
                scored.add(new JobScore(r.getId(), sim, score100));
                matchedIds.add(r.getId());
            }
        }

        // 2. 技能文本匹配兜底
        if (scored.size() < topN && job.getRequirementTags() != null) {
            String[] keywords = job.getRequirementTags().toLowerCase().split(",");
            for (Resume r : allResumes) {
                if (matchedIds.contains(r.getId()) || r.getSkills() == null) continue;
                String skills = r.getSkills().toLowerCase();
                int matched = 0;
                for (String kw : keywords) {
                    if (skills.contains(kw.trim())) matched++;
                }
                if (matched > 0) {
                    int score = Math.min(40 + matched * 5, 65);
                    scored.add(new JobScore(r.getId(), score / 100.0, score));
                    matchedIds.add(r.getId());
                }
            }
        }

        // 3. 最新简历保底
        int need = topN - scored.size();
        if (need > 0) {
            allResumes.stream()
                    .filter(r -> !matchedIds.contains(r.getId()))
                    .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                    .limit(need)
                    .forEach(r -> {
                        scored.add(new JobScore(r.getId(), 0.3, 30));
                        matchedIds.add(r.getId());
                    });
        }

        scored.sort((a, b) -> Double.compare(b.score, a.score));
        if (scored.size() > topN) scored = scored.subList(0, topN);

        // 4. 查询用户昵称
        List<Long> userIds = scored.stream()
                .map(js -> resumeMap.get(js.jobId))
                .filter(Objects::nonNull)
                .map(Resume::getUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        return scored.stream().map(js -> {
            Resume r = resumeMap.get(js.jobId);
            if (r == null) return null;
            User u = userMap.get(r.getUserId());
            RecommendResult res = new RecommendResult();
            res.setJobId(r.getId());
            res.setMatchScore(js.score100);
            res.setJobTitle(u != null ? u.getNickname() : "候选人");
            res.setCompany(r.getSkills());
            res.setLocation(u != null ? u.getUsername() : "");
            res.setReasonType(hasJobVec ? "vector" : "text");
            res.setCandidateUserId(r.getUserId());
            res.setExperienceYears(r.getYearsOfExperience());
            res.setAbilitySummary(r.getAbilityProfile());
            return res;
        }).filter(Objects::nonNull).toList();
    }

    @Override
    public List<Recommendation> historyByUser(Long userId) {
        return lambdaQuery().eq(Recommendation::getUserId, userId)
                .orderByDesc(Recommendation::getCreateTime)
                .last("limit 50").list();
    }

    private record JobScore(Long jobId, double score, int score100) {}
}
