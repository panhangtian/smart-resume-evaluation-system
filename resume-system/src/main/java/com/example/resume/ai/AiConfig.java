package com.example.resume.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 客户端装配：根据配置选择 Mock 或真实大模型
 */
@Configuration
public class AiConfig {

    @Bean
    public AiClient aiClient(AiProperties props) {
        boolean useReal = props.isEnabled() && !"mock".equalsIgnoreCase(props.getProvider());
        if (useReal) {
            return new OpenAiCompatibleClient(props);
        }
        return new MockAiClient(props.getEmbeddingDim());
    }
}
