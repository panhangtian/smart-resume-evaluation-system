package com.example.resume.modules.job.controller;

import com.example.resume.common.R;
import com.example.resume.modules.job.dto.JobDTO;
import com.example.resume.modules.job.entity.Job;
import com.example.resume.modules.job.service.JobService;
import com.example.resume.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "岗位管理")
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "发布岗位（HR）")
    public R<Long> create(@Valid @RequestBody JobDTO dto) {
        return R.ok(jobService.create(dto, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "编辑岗位")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody JobDTO dto) {
        jobService.update(id, dto);
        return R.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "岗位详情")
    public R<Job> detail(@PathVariable Long id) {
        return R.ok(jobService.detail(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "我发布的岗位")
    public R<List<Job>> my() {
        return R.ok(jobService.myJobs(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/available")
    @Operation(summary = "浏览可投递的岗位（求职者）")
    public R<List<Job>> available(@RequestParam(required = false) String keyword) {
        return R.ok(jobService.available(keyword));
    }

    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasRole('HR')")
    @Operation(summary = "上下架岗位")
    public R<Void> toggle(@PathVariable Long id, @RequestParam Integer status) {
        jobService.toggleStatus(id, status);
        return R.ok();
    }
}
