package com.example.resume.ai;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 向量化服务（带缓存，避免重复调用大模型）
 */
@Service
public class EmbeddingService {

    private final AiClient aiClient;

    public EmbeddingService(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @Cacheable(value = "embedding", key = "#text")
    public float[] embed(String text) {
        return aiClient.embed(text);
    }

    public boolean isRealModel() {
        return aiClient.isReal();
    }
}
