package com.example.resume.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 提示词模板管理：优先读取 resources/prompt/*.md，便于热维护；
 * 缺省时使用内置模板，保证开箱即用。
 */
@Slf4j
@Component
public class PromptTemplate {

    private static final String RESUME_PARSE_SYSTEM =
            "你是一名资深 HR 与简历解析专家【任务：解析简历】。\n" +
                    "请将用户提供的简历原文解析为严格 JSON，字段：\n" +
                    "name(姓名), yearsOfExperience(整数年), summary(一句话画像),\n" +
                    "education[{degree,school,major,year}], work[{company,title,duration,description}],\n" +
                    "projects[{name,role,description}], skills[技能关键词数组]。\n" +
                    "只输出 JSON，不要任何解释。";

    private static final String EVALUATE_SYSTEM =
            "你是一名严谨的招聘评估专家【任务：评估匹配度】。\n" +
                    "给定简历内容与岗位要求（以 >>> 分隔），输出严格 JSON：\n" +
                    "skillMatch(0-100), experienceMatch(0-100), educationMatch(0-100),\n" +
                    "overall(0-100，建议加权：技能0.5/经验0.3/学历0.2),\n" +
                    "strengths[优势], gaps[短板], comment(一句话总评)。\n" +
                    "只输出 JSON，不要任何解释。";

    public String resumeParseSystem() {
        return loadOrDefault("resume-parse.md", RESUME_PARSE_SYSTEM);
    }

    public String evaluateSystem() {
        return loadOrDefault("evaluate.md", EVALUATE_SYSTEM);
    }

    public String evaluateUser(String resumeText, String jobText) {
        return "简历内容：\n" + (resumeText == null ? "" : resumeText) +
                "\n>>>\n岗位要求：\n" + (jobText == null ? "" : jobText);
    }

    private String loadOrDefault(String fileName, String fallback) {
        try {
            ClassPathResource resource = new ClassPathResource("prompt/" + fileName);
            if (resource.exists()) {
                Path path = resource.getFile().toPath();
                return Files.readString(path, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.debug("读取提示词模板失败，使用内置默认：{}", fileName);
        }
        return fallback;
    }
}
