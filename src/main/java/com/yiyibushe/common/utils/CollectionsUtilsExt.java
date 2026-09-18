package com.yiyibushe.common.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 集合工具类（内部使用 Apache Commons Collections4 实现，对外屏蔽底层依赖）。
 * <p>
 * 类名以 {@code UtilsExt} 结尾，与底层 {@code org.apache.commons.collections4.*} 明确区分。
 * <p>
 * 业务代码统一使用本类，不直接 import org.apache.commons.collections4.*。
 *
 * @author Witty·Kid Fisher
 */
public final class CollectionsUtilsExt {

    private CollectionsUtilsExt() {
    }

    // ==================== 判空 ====================

    /** 判断集合是否为 null 或空。 */
    public static boolean isEmpty(Collection<?> collection) {
        return org.apache.commons.collections4.CollectionUtils.isEmpty(collection);
    }

    /** 判断集合是否非空。 */
    public static boolean isNotEmpty(Collection<?> collection) {
        return org.apache.commons.collections4.CollectionUtils.isNotEmpty(collection);
    }

    /** 判断 Map 是否为 null 或空。 */
    public static boolean isEmpty(Map<?, ?> map) {
        return org.apache.commons.collections4.MapUtils.isEmpty(map);
    }

    /** 判断 Map 是否非空。 */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return org.apache.commons.collections4.MapUtils.isNotEmpty(map);
    }

    // ==================== 转 Stream ====================

    /**
     * 将集合转为 Stream，内部已判空。
     * 业务代码直接调用本方法，无需先判空，避免 list.stream() 在 list 为 null 时抛 NPE。
     */
    public static <T> Stream<T> toStream(Collection<T> collection) {
        if (Objects.isNull(collection) || collection.isEmpty()) {
            return Stream.empty();
        }
        return collection.stream();
    }

    // ==================== 常用操作 ====================

    /** 集合大小（null 返回 0）。 */
    public static int size(Collection<?> collection) {
        return Objects.isNull(collection) ? 0 : collection.size();
    }

    /** Map 大小（null 返回 0）。 */
    public static int size(Map<?, ?> map) {
        return Objects.isNull(map) ? 0 : map.size();
    }

    /** 集合是否包含指定元素（null 安全）。 */
    public static boolean contains(Collection<?> collection, Object value) {
        return Objects.nonNull(collection) && collection.contains(value);
    }

    /** Map 是否包含指定 key（null 安全）。 */
    public static boolean containsKey(Map<?, ?> map, Object key) {
        return Objects.nonNull(map) && map.containsKey(key);
    }

    /** Map 是否包含指定 value（null 安全）。 */
    public static boolean containsValue(Map<?, ?> map, Object value) {
        return Objects.nonNull(map) && map.containsValue(value);
    }

    // ==================== 集合运算 ====================

    /** 并集。 */
    public static <T> java.util.Collection<T> union(Collection<T> a, Collection<T> b) {
        return org.apache.commons.collections4.CollectionUtils.union(a, b);
    }

    /** 交集。 */
    public static <T> java.util.Collection<T> intersection(Collection<T> a, Collection<T> b) {
        return org.apache.commons.collections4.CollectionUtils.intersection(a, b);
    }

    /** 差集（a - b）。 */
    public static <T> java.util.Collection<T> subtract(Collection<T> a, Collection<T> b) {
        return org.apache.commons.collections4.CollectionUtils.subtract(a, b);
    }

    // ==================== Map 取值 ====================

    /** 从 Map 取 Object 值。 */
    public static Object getObject(Map<?, ?> map, Object key) {
        return Objects.isNull(map) ? null : map.get(key);
    }

    /** 从 Map 取 String 值。 */
    public static String getString(Map<?, ?> map, Object key) {
        Object value = getObject(map, key);
        return Objects.isNull(value) ? null : value.toString();
    }

    /** 从 Map 取 Integer 值。 */
    public static Integer getInteger(Map<?, ?> map, Object key) {
        Object value = getObject(map, key);
        if (Objects.isNull(value)) {
            return null;
        }
        if (value instanceof Integer integer) {
            return integer;
        }
        return Integer.valueOf(value.toString());
    }

    /** 从 Map 取 Long 值。 */
    public static Long getLong(Map<?, ?> map, Object key) {
        Object value = getObject(map, key);
        if (Objects.isNull(value)) {
            return null;
        }
        if (value instanceof Long longVal) {
            return longVal;
        }
        return Long.valueOf(value.toString());
    }

    /** 从 Map 取 Boolean 值。 */
    public static Boolean getBoolean(Map<?, ?> map, Object key) {
        Object value = getObject(map, key);
        if (Objects.isNull(value)) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.valueOf(value.toString());
    }
}
