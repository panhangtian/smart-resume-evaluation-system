package com.example.resume.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容客户端：支持混元 / 通义 / OpenAI 等（配置 baseUrl + apiKey + model 即可切换）
 */
@Slf4j
public class OpenAiCompatibleClient implements AiClient {

    private final AiProperties props;
    private final RestClient restClient;

    public OpenAiCompatibleClient(AiProperties props) {
        this.props = props;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.getTimeoutMs());
        factory.setReadTimeout(props.getTimeoutMs());
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + props.getApiKey())
                .build();
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        Map<String, Object> body = Map.of(
                "model", props.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)),
                "temperature", 0.2);
        String resp = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        JSONObject json = JSON.parseObject(resp);
        return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }

    @Override
    public float[] embed(String text) {
        Map<String, Object> body = Map.of(
                "model", props.getEmbedModel(),
                "input", text);
        String resp = restClient.post()
                .uri("/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
        JSONObject json = JSON.parseObject(resp);
        JSONArray vector = json.getJSONArray("data")
                .getJSONObject(0)
                .getJSONArray("embedding");
        float[] vec = new float[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            vec[i] = vector.getFloatValue(i);
        }
        return vec;
    }

    @Override
    public boolean isReal() {
        return true;
    }
}
