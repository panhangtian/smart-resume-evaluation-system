package com.example.resume.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

/**
 * JSON 工具（基于 fastjson2）
 */
public final class JsonUtils {

    private JsonUtils() {
    }

    public static String toJson(Object obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.WriteNulls);
    }

    public static String toJsonPretty(Object obj) {
        return JSON.toJSONString(obj, JSONWriter.Feature.PrettyFormat);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    public static JSONObject toObject(String json) {
        return JSON.parseObject(json);
    }
}
