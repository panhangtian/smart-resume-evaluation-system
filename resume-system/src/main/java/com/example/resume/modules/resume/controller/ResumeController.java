package com.example.resume.modules.resume.controller;

import com.example.resume.common.R;
import com.example.resume.modules.resume.dto.ResumeEditDTO;
import com.example.resume.modules.resume.entity.Resume;
import com.example.resume.modules.resume.service.ResumeService;
import com.example.resume.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
@Tag(name = "简历管理")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "上传简历（求职者），自动触发大模型解析")
    public R<Long> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(resumeService.upload(SecurityUtils.getCurrentUserId(), file));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "简历详情")
    public R<Resume> detail(@PathVariable Long id) {
        return R.ok(resumeService.detail(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "我的简历列表")
    public R<List<Resume>> my() {
        return R.ok(resumeService.myResumes(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/{id}/reparse")
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "重新解析")
    public R<Void> reparse(@PathVariable Long id) {
        resumeService.reparse(id);
        return R.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('JOBSEEKER')")
    @Operation(summary = "编辑简历数据")
    public R<Void> update(@PathVariable Long id, @RequestBody ResumeEditDTO dto) {
        resumeService.updateResume(id, SecurityUtils.getCurrentUserId(), dto);
        return R.ok();
    }
}
