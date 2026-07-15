package com.example.resume.modules.evaluate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.resume.modules.evaluate.entity.EvaluationResult;

import java.util.List;

public interface EvaluationService extends IService<EvaluationResult> {

    EvaluationResult evaluate(Long resumeId, Long jobId);

    List<EvaluationResult> byResume(Long resumeId);

    List<EvaluationResult> byUser(Long userId);

    EvaluationResult latest(Long resumeId, Long jobId);
}
