package com.example.resume.modules.recommend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.resume.modules.recommend.dto.RecommendResult;
import com.example.resume.modules.recommend.entity.Recommendation;

import java.util.List;

public interface RecommendService extends IService<Recommendation> {

    /** 求职者：基于简历向量匹配推荐岗位 */
    List<RecommendResult> recommendJobsForUser(Long userId, int topN);

    /** HR：查看匹配某岗位的人才候选 */
    List<RecommendResult> recommendCandidatesForJob(Long jobId, int topN);

    /** 查看某用户的推荐历史 */
    List<Recommendation> historyByUser(Long userId);
}
