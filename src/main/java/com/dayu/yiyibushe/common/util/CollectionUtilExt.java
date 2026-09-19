package com.dayu.yiyibushe.common.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 集合工具类（内部使用 Apache Commons Collections4 实现，对外屏蔽底层依赖）。
 * <p>
 * 类名统一以 {@code UtilExt} 结尾，与底层 {@code org.apache.commons.collections4.*} 明确区分。
 * <p>
 * 业务代码统一使用本类：
 * <ul>
 * <li>不直接写 {@code list.size()}，统一 {@link #getSize(Collection)}；</li>
 * <li>不直接写 {@code list.stream()}，统一 {@link #findFirst}/{@link #filterToList}
 * 等方法；</li>
 * <li>所有方法均 null 安全，不在业务代码里反复判空。</li>
 * </ul>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public final class CollectionUtilExt {

    /**
     * 私有构造器：工具类不允许实例化
     */
    private CollectionUtilExt() {
    }

    // ==================== 判空 ====================

    /**
     * 判断集合是否为 null 或空
     *
     * @param collection
     *                   待判断集合
     * @return true 表示为 null 或没有元素
     */
    public static boolean isEmpty(Collection<?> collection) {
        return CollectionUtils.isEmpty(collection);
    }

    /**
     * 判断集合是否非空
     *
     * @param collection
     *                   待判断集合
     * @return true 表示至少有一个元素
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return CollectionUtils.isNotEmpty(collection);
    }

    /**
     * 判断 Map 是否为 null 或空
     *
     * @param map
     *            待判断 Map
     * @return true 表示为 null 或没有键值对
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return MapUtils.isEmpty(map);
    }

    /**
     * 判断 Map 是否非空
     *
     * @param map
     *            待判断 Map
     * @return true 表示至少有一个键值对
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return MapUtils.isNotEmpty(map);
    }

    // ==================== 大小 ====================

    /**
     * 获取集合大小（null 安全）
     *
     * @param collection
     *                   待统计集合
     * @return 元素个数，null 返回 0
     */
    public static int getSize(Collection<?> collection) {
        return Objects.isNull(collection) ? 0 : collection.size();
    }

    /**
     * 获取 Map 大小（null 安全）
     *
     * @param map
     *            待统计 Map
     * @return 键值对个数，null 返回 0
     */
    public static int getSize(Map<?, ?> map) {
        return Objects.isNull(map) ? 0 : map.size();
    }

    // ==================== 包含判断 ====================

    /**
     * 集合是否包含指定元素（null 安全）
     *
     * @param collection
     *                   待检查集合
     * @param value
     *                   目标元素
     * @return true 表示包含
     */
    public static boolean contains(Collection<?> collection, Object value) {
        return Objects.nonNull(collection) && collection.contains(value);
    }

    /**
     * Map 是否包含指定 key（null 安全）
     *
     * @param map
     *            待检查 Map
     * @param key
     *            目标 key
     * @return true 表示包含
     */
    public static boolean containsKey(Map<?, ?> map, Object key) {
        return Objects.nonNull(map) && map.containsKey(key);
    }

    /**
     * Map 是否包含指定 value（null 安全）
     *
     * @param map
     *              待检查 Map
     * @param value
     *              目标 value
     * @return true 表示包含
     */
    public static boolean containsValue(Map<?, ?> map, Object value) {
        return Objects.nonNull(map) && map.containsValue(value);
    }

    // ==================== 函数式操作（替代 stream） ====================

    /**
     * 查找第一个满足条件的元素
     *
     * @param collection
     *                   待查找集合
     * @param predicate
     *                   匹配条件
     * @param <T>
     *                   元素类型
     * @return 第一个匹配元素，没有匹配返回 null
     */
    public static <T> T findFirst(Collection<T> collection, Predicate<? super T> predicate) {
        if (isEmpty(collection) || Objects.isNull(predicate)) {
            return null;
        }
        for (T item : collection) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 过滤出所有满足条件的元素
     *
     * @param collection
     *                   待过滤集合
     * @param predicate
     *                   匹配条件
     * @param <T>
     *                   元素类型
     * @return 匹配元素组成的新列表，入参为 null 时返回空列表
     */
    public static <T> List<T> filterToList(Collection<T> collection, Predicate<? super T> predicate) {
        List<T> matchedList = new ArrayList<>();
        if (isEmpty(collection) || Objects.isNull(predicate)) {
            return matchedList;
        }
        for (T item : collection) {
            if (predicate.test(item)) {
                matchedList.add(item);
            }
        }
        return matchedList;
    }

    /**
     * 将集合元素逐个转换后收集为新列表
     *
     * @param collection
     *                   源集合
     * @param mapper
     *                   元素转换函数
     * @param <S>
     *                   源元素类型
     * @param <T>
     *                   目标元素类型
     * @return 转换结果列表，入参为 null 时返回空列表
     */
    public static <S, T> List<T> mapToList(Collection<S> collection, Function<? super S, ? extends T> mapper) {
        List<T> resultList = new ArrayList<>();
        if (isEmpty(collection) || Objects.isNull(mapper)) {
            return resultList;
        }
        for (S item : collection) {
            resultList.add(mapper.apply(item));
        }
        return resultList;
    }

    /**
     * 按比较器取最大元素
     *
     * @param collection
     *                   待统计集合
     * @param comparator
     *                   比较器
     * @param <T>
     *                   元素类型
     * @return 最大元素，集合为 null 或空时返回 null
     */
    public static <T> T getMax(Collection<T> collection, Comparator<? super T> comparator) {
        if (isEmpty(collection) || Objects.isNull(comparator)) {
            return null;
        }
        T maxItem = null;
        for (T item : collection) {
            if (Objects.isNull(maxItem) || comparator.compare(item, maxItem) > 0) {
                maxItem = item;
            }
        }
        return maxItem;
    }

    /**
     * 从可变集合中删除所有满足条件的元素
     *
     * @param collection
     *                   待处理集合
     * @param predicate
     *                   删除条件
     * @param <T>
     *                   元素类型
     * @return 实际删除的元素个数
     */
    public static <T> int removeMatched(Collection<T> collection, Predicate<? super T> predicate) {
        if (isEmpty(collection) || Objects.isNull(predicate)) {
            return 0;
        }
        int removedCount = 0;
        var iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (predicate.test(iterator.next())) {
                iterator.remove();
                removedCount++;
            }
        }
        return removedCount;
    }

    // ==================== 集合运算 ====================

    /**
     * 并集
     *
     * @param first
     *               集合 A
     * @param second
     *               集合 B
     * @param <T>
     *               元素类型
     * @return 并集集合
     */
    public static <T> Collection<T> union(Collection<T> first, Collection<T> second) {
        return CollectionUtils.union(first, second);
    }

    /**
     * 交集
     *
     * @param first
     *               集合 A
     * @param second
     *               集合 B
     * @param <T>
     *               元素类型
     * @return 交集集合
     */
    public static <T> Collection<T> intersection(Collection<T> first, Collection<T> second) {
        return CollectionUtils.intersection(first, second);
    }

    /**
     * 差集（A - B）
     *
     * @param first
     *               集合 A
     * @param second
     *               集合 B
     * @param <T>
     *               元素类型
     * @return 差集集合
     */
    public static <T> Collection<T> subtract(Collection<T> first, Collection<T> second) {
        return CollectionUtils.subtract(first, second);
    }

    // ==================== Map 取值 ====================

    /**
     * 从 Map 取 Object 值
     *
     * @param map
     *            数据源 Map
     * @param key
     *            键
     * @return 值，Map 为 null 或键不存在时返回 null
     */
    public static Object getObject(Map<?, ?> map, Object key) {
        return Objects.isNull(map) ? null : map.get(key);
    }

    /**
     * 从 Map 取 String 值
     *
     * @param map
     *            数据源 Map
     * @param key
     *            键
     * @return 字符串值，不存在返回 null
     */
    public static String getString(Map<?, ?> map, Object key) {
        Object value = getObject(map, key);
        return Objects.isNull(value) ? null : value.toString();
    }

    /**
     * 从 Map 取 Integer 值
     *
     * @param map
     *            数据源 Map
     * @param key
     *            键
     * @return Integer 值，不存在返回 null
     */
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

    /**
     * 从 Map 取 Long 值
     *
     * @param map
     *            数据源 Map
     * @param key
     *            键
     * @return Long 值，不存在返回 null
     */
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

    /**
     * 从 Map 取 Boolean 值
     *
     * @param map
     *            数据源 Map
     * @param key
     *            键
     * @return Boolean 值，不存在返回 null
     */
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
