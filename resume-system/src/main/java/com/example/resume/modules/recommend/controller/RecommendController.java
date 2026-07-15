package com.example.resume.modules.recommend.controller;

import com.example.resume.common.R;
import com.example.resume.modules.recommend.dto.RecommendResult;
import com.example.resume.modules.recommend.service.RecommendService;
import com.example.resume.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@Tag(name = "岗位推荐")
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "为我推荐岗位（求职者）")
    public R<List<RecommendResult>> recommendJobs(@RequestParam(defaultValue = "20") int topN) {
        return R.ok(recommendService.recommendJobsForUser(SecurityUtils.getCurrentUserId(), topN));
    }

    @GetMapping("/candidates/{jobId}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "为岗位推荐候选人（HR）")
    public R<List<RecommendResult>> recommendCandidates(@PathVariable Long jobId,
                                                        @RequestParam(defaultValue = "10") int topN) {
        return R.ok(recommendService.recommendCandidatesForJob(jobId, topN));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "我的推荐历史")
    public R<?> history() {
        return R.ok(recommendService.historyByUser(SecurityUtils.getCurrentUserId()));
    }
}
