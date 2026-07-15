package com.example.resume.task;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.resume.ai.AiClient;
import com.example.resume.ai.EmbeddingService;
import com.example.resume.ai.PromptTemplate;
import com.example.resume.common.utils.VectorUtils;
import com.example.resume.modules.resume.entity.Resume;
import com.example.resume.modules.resume.mapper.ResumeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 简历异步解析任务：调用大模型解析 → 结构化存储 → 向量化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeParseTask {

    private static final int STATUS_PARSING = 1;
    private static final int STATUS_DONE = 2;
    private static final int STATUS_FAILED = 3;

    private final ResumeMapper resumeMapper;
    private final AiClient aiClient;
    private final PromptTemplate promptTemplate;
    private final EmbeddingService embeddingService;

    @Async("resumeExecutor")
    public void parse(Long resumeId) {
        Resume r = resumeMapper.selectById(resumeId);
        if (r == null) return;
        r.setStatus(STATUS_PARSING);
        resumeMapper.updateById(r);
        try {
            String raw = r.getRawText() == null ? "" : r.getRawText();
            String json = aiClient.chat(promptTemplate.resumeParseSystem(), raw);
            JSONObject obj = JSON.parseObject(extractJson(json));

            r.setParsedJson(obj.toJSONString());
            r.setAbilityProfile(obj.getString("summary"));
            r.setYearsOfExperience(obj.getInteger("yearsOfExperience"));
            JSONArray skills = obj.getJSONArray("skills");
            r.setSkills(skills == null ? "" : String.join(",", skills.toList(String.class)));

            String embedText = (r.getAbilityProfile() == null ? "" : r.getAbilityProfile())
                    + " " + (r.getSkills() == null ? "" : r.getSkills());
            float[] vec = embeddingService.embed(embedText);
            r.setEmbedding(VectorUtils.toJson(vec));
            r.setStatus(STATUS_DONE);
        } catch (Exception e) {
            log.error("简历解析失败 resumeId={}", resumeId, e);
            r.setStatus(STATUS_FAILED);
        }
        resumeMapper.updateById(r);
    }

    /**
     * 从模型输出中抽取 JSON（兼容 ```json 代码块包裹）
     */
    private String extractJson(String text) {
        if (text == null) return "{}";
        String t = text.trim();
        int start = t.indexOf('{');
        int end = t.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return t.substring(start, end + 1);
        }
        return t;
    }
}
