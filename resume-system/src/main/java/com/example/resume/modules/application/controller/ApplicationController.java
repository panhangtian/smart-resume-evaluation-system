package com.example.resume.modules.application.controller;

import com.example.resume.common.R;
import com.example.resume.modules.application.service.ApplicationService;
import com.example.resume.modules.application.vo.ApplicationVO;
import com.example.resume.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "投递管理")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "投递岗位（求职者）")
    public R<Long> apply(@RequestParam Long resumeId, @RequestParam Long jobId) {
        return R.ok(applicationService.apply(resumeId, jobId, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "我的投递记录")
    public R<List<ApplicationVO>> my() {
        return R.ok(applicationService.myApplications(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "岗位投递列表（HR）")
    public R<List<Application>> byJob(@PathVariable Long jobId) {
        return R.ok(applicationService.jobApplications(jobId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "更新投递状态（HR）")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        applicationService.updateStatus(id, status);
        return R.ok();
    }
}
