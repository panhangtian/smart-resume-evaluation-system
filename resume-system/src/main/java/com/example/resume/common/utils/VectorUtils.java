package com.example.resume.common.utils;

import com.alibaba.fastjson2.JSONArray;

import java.util.List;

/**
 * 向量工具：余弦相似度（推荐引擎的语义匹配核心）
 */
public final class VectorUtils {

    private VectorUtils() {
    }

    public static float[] parse(String embeddingJson) {
        if (embeddingJson == null || embeddingJson.isBlank()) {
            return new float[0];
        }
        JSONArray arr = JSONArray.parseArray(embeddingJson);
        float[] vec = new float[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            vec[i] = arr.getFloatValue(i);
        }
        return vec;
    }

    public static String toJson(float[] vec) {
        JSONArray arr = new JSONArray();
        for (float v : vec) {
            arr.add(v);
        }
        return arr.toJSONString();
    }

    /**
     * 余弦相似度，结果范围 [-1, 1]
     */
    public static double cosine(float[] a, float[] b) {
        if (a.length == 0 || b.length == 0 || a.length != b.length) {
            return 0.0;
        }
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 列表求和归一化到 [0,100]
     */
    public static int toScore(double similarity) {
        return (int) Math.round(Math.max(0, Math.min(1, similarity)) * 100);
    }

    public static float[] average(List<float[]> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return new float[0];
        }
        int dim = vectors.get(0).length;
        float[] sum = new float[dim];
        for (float[] v : vectors) {
            for (int i = 0; i < dim; i++) {
                sum[i] += v[i];
            }
        }
        for (int i = 0; i < dim; i++) {
            sum[i] /= vectors.size();
        }
        return sum;
    }
}
