package com.example.resume.ai;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.resume.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mock AI 客户端（默认）：无需任何 API Key 即可让整条链路跑通。
 * - 文本生成：基于关键词启发式产出结构化 JSON（简历解析 / 岗位评估）
 * - 向量化：基于词袋哈希的确定性向量，保证余弦相似度可用
 */
@Slf4j
public class MockAiClient implements AiClient {

    private final int dim;

    private static final Set<String> SKILL_DICT = new HashSet<>(Arrays.asList(
            "java", "spring", "springboot", "spring cloud", "mybatis", "hibernate",
            "python", "django", "flask", "fastapi", "pandas", "numpy",
            "javascript", "typescript", "vue", "react", "angular", "node", "webpack",
            "html", "css", "sass", "less",
            "go", "golang", "rust", "c++", "c#", "c", "kotlin", "swift",
            "mysql", "postgresql", "oracle", "mongodb", "redis", "elasticsearch", "clickhouse",
            "docker", "kubernetes", "k8s", "jenkins", "git", "linux", "nginx", "prometheus",
            "tensorflow", "pytorch", "sklearn", "spark", "flink", "hadoop", "kafka",
            "aws", "aliyun", "tencentcloud", "azure", "微服务", "分布式", "高并发", "数据分析", "机器学习", "深度学习"
    ));

    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d+)\\s*年");

    public MockAiClient(int dim) {
        this.dim = dim;
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        if (systemPrompt != null && systemPrompt.contains("解析简历")) {
            return parseResume(userPrompt);
        }
        if (systemPrompt != null && (systemPrompt.contains("评估") || systemPrompt.contains("匹配"))) {
            return evaluateResume(userPrompt);
        }
        // 兜底：直接回复
        return "{\"reply\":\"" + safe(userPrompt).substring(0, Math.min(50, safe(userPrompt).length())) + "\"}";
    }

    @Override
    public float[] embed(String text) {
        float[] vec = new float[dim];
        if (!StringUtils.hasText(text)) {
            return normalize(vec);
        }
        String lower = text.toLowerCase(Locale.ROOT);
        // 拉丁词
        for (String token : lower.split("[^a-z0-9+#.]+")) {
            if (token.isBlank()) continue;
            int idx = Math.floorMod(hash(token), dim);
            vec[idx] += 1.0f;
        }
        // 中文字符（逐字，保证中文内容有重叠信号）
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) {
                int idx = Math.floorMod(hash(String.valueOf(c)), dim);
                vec[idx] += 1.0f;
            }
        }
        return normalize(vec);
    }

    @Override
    public boolean isReal() {
        return false;
    }

    // ===== 简历解析 =====
    private String parseResume(String text) {
        JSONObject root = new JSONObject();
        String safe = safe(text);
        root.put("name", extractName(safe));
        root.put("yearsOfExperience", extractYears(safe));
        root.put("education", extractEducation(safe));
        root.put("work", extractWork(safe));
        root.put("projects", new JSONArray());
        root.put("skills", extractSkills(safe));
        root.put("summary", "由 Mock 引擎基于关键词启发式解析生成，配置真实大模型后可获得更精准的结构化结果。");
        return root.toJSONString();
    }

    private String extractName(String text) {
        // 简单的“姓名：XXX”或“姓名 XXX”提取
        Matcher m = Pattern.compile("姓名[：:\\s]*([\\u4e00-\\u9fa5]{2,4})").matcher(text);
        if (m.find()) return m.group(1);
        return "未知";
    }

    private int extractYears(String text) {
        Matcher m = YEAR_PATTERN.matcher(text);
        int max = 0;
        while (m.find()) {
            max = Math.max(max, Integer.parseInt(m.group(1)));
        }
        return max;
    }

    private JSONArray extractEducation(String text) {
        JSONArray arr = new JSONArray();
        JSONObject e = new JSONObject();
        String degree = "本科";
        if (text.contains("博士")) degree = "博士";
        else if (text.contains("硕士") || text.contains("研究生")) degree = "硕士";
        else if (text.contains("大专") || text.contains("专科")) degree = "大专";
        e.put("degree", degree);
        e.put("school", "未知院校");
        e.put("major", "未知专业");
        arr.add(e);
        return arr;
    }

    private JSONArray extractWork(String text) {
        JSONArray arr = new JSONArray();
        JSONObject w = new JSONObject();
        w.put("company", "未知公司");
        w.put("title", "未知职位");
        w.put("duration", extractYears(text) + "年");
        w.put("description", "由 Mock 引擎提取，真实模型将给出更完整的工作经历。");
        arr.add(w);
        return arr;
    }

    private JSONArray extractSkills(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        JSONArray skills = new JSONArray();
        for (String skill : SKILL_DICT) {
            if (lower.contains(skill)) {
                skills.add(skill);
            }
        }
        return skills;
    }

    // ===== 岗位评估 =====
    private String evaluateResume(String text) {
        // userPrompt 约定格式：简历内容 >>> 岗位要求
        String[] parts = text.split(">>>", 2);
        String resumeText = parts.length > 0 ? parts[0] : text;
        String jobText = parts.length > 1 ? parts[1] : "";

        List<String> resumeSkills = matchSkills(resumeText);
        List<String> jobSkills = matchSkills(jobText);
        int skillMatch = overlapScore(resumeSkills, jobSkills);

        int resumeYears = extractYears(resumeText);
        int jobYears = extractYears(jobText);
        int experienceMatch = jobYears == 0 ? 80 : Math.max(0, 100 - Math.abs(resumeYears - jobYears) * 15);

        int educationMatch = 75; // mock 默认

        int overall = (int) Math.round(skillMatch * 0.5 + experienceMatch * 0.3 + educationMatch * 0.2);

        JSONObject root = new JSONObject();
        root.put("skillMatch", skillMatch);
        root.put("experienceMatch", experienceMatch);
        root.put("educationMatch", educationMatch);
        root.put("overall", overall);
        root.put("strengths", new JSONArray(resumeSkills.stream().limit(5).toList()));
        List<String> gaps = new ArrayList<>(jobSkills);
        gaps.removeAll(resumeSkills);
        root.put("gaps", new JSONArray(gaps.stream().limit(5).toList()));
        root.put("comment", "技能匹配度 " + skillMatch + "%，经验匹配度 " + experienceMatch + "%。" +
                (gaps.isEmpty() ? "岗位核心技能基本具备。" : "建议补充：" + String.join("、", gaps.stream().limit(3).toList()) + "。"));
        return root.toJSONString();
    }

    private List<String> matchSkills(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String skill : SKILL_DICT) {
            if (lower.contains(skill)) out.add(skill);
        }
        return out;
    }

    private int overlapScore(List<String> a, List<String> b) {
        if (b.isEmpty()) return a.isEmpty() ? 0 : 50;
        Set<String> setA = new HashSet<>(a);
        int hit = 0;
        for (String s : b) if (setA.contains(s)) hit++;
        return (int) Math.round((double) hit / b.size() * 100);
    }

    private static int hash(String s) {
        return Objects.hash(s);
    }

    private static float[] normalize(float[] vec) {
        double norm = 0.0;
        for (float v : vec) norm += v * v;
        if (norm == 0.0) return vec;
        double len = Math.sqrt(norm);
        for (int i = 0; i < vec.length; i++) vec[i] = (float) (vec[i] / len);
        return vec;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
