package com.example.resume.ai;

/**
 * AI 客户端抽象：文本生成 + 向量化
 * 实现：MockAiClient（默认，无需 API Key）/ OpenAiCompatibleClient（混元/通义/OpenAI）
 */
public interface AiClient {

    /**
     * 对话生成（返回模型原始文本）
     */
    String chat(String systemPrompt, String userPrompt);

    /**
     * 文本向量化
     */
    float[] embed(String text);

    /**
     * 当前实现是否真实大模型
     */
    boolean isReal();
}
