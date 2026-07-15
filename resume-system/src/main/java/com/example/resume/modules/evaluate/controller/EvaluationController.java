package com.example.resume.modules.evaluate.controller;

import com.example.resume.common.R;
import com.example.resume.modules.evaluate.entity.EvaluationResult;
import com.example.resume.modules.evaluate.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluations")
@RequiredArgsConstructor
@Tag(name = "简历评估")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "评估简历与岗位匹配度（求职者）")
    public R<EvaluationResult> evaluate(@RequestParam Long resumeId, @RequestParam Long jobId) {
        return R.ok(evaluationService.evaluate(resumeId, jobId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "评估详情")
    public R<EvaluationResult> detail(@PathVariable Long id) {
        return R.ok(evaluationService.getById(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "我的评估记录（按简历）")
    public R<List<EvaluationResult>> byResume(@RequestParam Long resumeId) {
        return R.ok(evaluationService.byResume(resumeId));
    }

    @GetMapping("/my-all")
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "我所有的评估记录")
    public R<List<EvaluationResult>> myAll() {
        return R.ok(evaluationService.byUser(
                com.example.resume.security.SecurityUtils.getCurrentUserId()));
    }
}
