package com.example.resume.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 引擎配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** mock / openai-compatible */
    private String provider = "mock";

    /** 是否启用真实大模型（false 时强制走 mock） */
    private boolean enabled = false;

    /** OpenAI 兼容接口地址，如 https://api.openai.com/v1 */
    private String baseUrl = "https://api.openai.com/v1";

    private String apiKey = "";

    /** 对话模型 */
    private String chatModel = "gpt-3.5-turbo";

    /** 向量模型 */
    private String embedModel = "text-embedding-ada-002";

    /** 向量维度（mock 默认 64，真实模型按服务商调整） */
    private int embeddingDim = 64;

    /** 单轮最大 token 近似 */
    private int maxTokens = 2000;

    /** 调用超时（毫秒） */
    private int timeoutMs = 30000;
}
