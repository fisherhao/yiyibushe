package com.yiyibushe.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;
import java.util.Objects;

/**
 * JSON 工具类（内部使用 FastJSON2 实现，对外屏蔽底层依赖）。
 * <p>
 * 类名以 {@code UtilsExt} 结尾，与底层 {@code com.alibaba.fastjson2.JSON} 明确区分。
 * <p>
 * 业务代码统一使用本类，不直接 import com.alibaba.fastjson2.*，
 * 以便后续底层 JSON 库升级或替换（如换 Jackson）时只改这一处。
 *
 * @author Witty·Kid Fisher
 */
public final class JsonUtilsExt {

    private JsonUtilsExt() {
    }

    // ==================== 序列化 ====================

    /** 将对象序列化为 JSON 字符串。 */
    public static String toJsonString(Object object) {
        return JSON.toJSONString(object);
    }

    /** 将对象序列化为美化格式的 JSON 字符串。 */
    public static String toJsonStringPretty(Object object) {
        return JSON.toJSONString(object, com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat);
    }

    // ==================== 反序列化 ====================

    /** 将 JSON 字符串解析为 JSONObject。 */
    public static JSONObject parseObject(String text) {
        return JSON.parseObject(text);
    }

    /** 将 JSON 字符串解析为指定类型的 Java 对象。 */
    public static <T> T parseObject(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    /** 将 JSON 字符串解析为指定类型的 List。 */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz);
    }

    /** 将 JSON 字符串解析为 JSONArray。 */
    public static JSONArray parseArray(String text) {
        return JSON.parseArray(text);
    }

    // ==================== 创建 ====================

    /** 创建一个空的 JSONObject。 */
    public static JSONObject newObject() {
        return new JSONObject();
    }

    /** 创建一个空的 JSONArray。 */
    public static JSONArray newArray() {
        return new JSONArray();
    }

    // ==================== 写入 ====================

    /** 向 JSONObject 写入一个键值对，返回自身以便链式调用。 */
    public static JSONObject put(JSONObject target, String key, Object value) {
        target.put(key, value);
        return target;
    }

    /** 批量写入 Map 中的所有键值对。 */
    public static JSONObject putAll(JSONObject target, java.util.Map<String, Object> map) {
        if (Objects.nonNull(map)) {
            target.putAll(map);
        }
        return target;
    }

    // ==================== 判空 ====================

    /** JSONObject 是否为 null 或空。 */
    public static boolean isEmpty(JSONObject object) {
        return Objects.isNull(object) || object.isEmpty();
    }

    /** JSONArray 是否为 null 或空。 */
    public static boolean isEmpty(JSONArray array) {
        return Objects.isNull(array) || array.isEmpty();
    }

    // ==================== 取值 ====================

    /** 从 JSONObject 取 String 值。 */
    public static String getString(JSONObject object, String key) {
        return Objects.isNull(object) ? null : object.getString(key);
    }

    /** 从 JSONObject 取 Integer 值。 */
    public static Integer getInteger(JSONObject object, String key) {
        return Objects.isNull(object) ? null : object.getInteger(key);
    }

    /** 从 JSONObject 取 Long 值。 */
    public static Long getLong(JSONObject object, String key) {
        return Objects.isNull(object) ? null : object.getLong(key);
    }

    /** 从 JSONObject 取 Boolean 值。 */
    public static Boolean getBoolean(JSONObject object, String key) {
        return Objects.isNull(object) ? null : object.getBoolean(key);
    }

    /** 从 JSONObject 取 Double 值。 */
    public static Double getDouble(JSONObject object, String key) {
        return Objects.isNull(object) ? null : object.getDouble(key);
    }

    /** 从 JSONObject 取嵌套 JSONObject。 */
    public static JSONObject getJSONObject(JSONObject object, String key) {
        return Objects.isNull(object) ? null : object.getJSONObject(key);
    }

    /** 从 JSONObject 取 JSONArray。 */
    public static JSONArray getJSONArray(JSONObject object, String key) {
        return Objects.isNull(object) ? null : object.getJSONArray(key);
    }
}
