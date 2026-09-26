package com.dayu.yiyibushe.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 字符串工具类（内部使用 Apache Commons Lang3 实现，对外屏蔽底层依赖）。
 * <p>
 * 类名统一以 {@code UtilExt} 结尾，与底层 {@code org.apache.commons.lang3.StringUtils} 明确区分，
 * 避免业务代码 import 时混淆。
 * <p>
 * 业务代码统一使用本类，不直接 import org.apache.commons.lang3.StringUtils，
 * 以便后续底层库升级或替换时只改这一处。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public final class StringUtilExt {

    /**
     * 私有构造器：工具类不允许实例化
     */
    private StringUtilExt() {
    }

    // ==================== 判空 ====================

    /** 判断字符串是否为空或全空白字符。 */
    public static boolean isBlank(String value) {
        return StringUtils.isBlank(value);
    }

    /** 判断字符串是否非空且不全为空白字符。 */
    public static boolean isNotBlank(String value) {
        return StringUtils.isNotBlank(value);
    }

    /** 判断字符串是否为空（null 或长度为 0）。 */
    public static boolean isEmpty(String value) {
        return StringUtils.isEmpty(value);
    }

    /** 判断字符串是否非空。 */
    public static boolean isNotEmpty(String value) {
        return StringUtils.isNotEmpty(value);
    }

    /** 是否任意一个为空白。 */
    public static boolean isAnyBlank(CharSequence... values) {
        return StringUtils.isAnyBlank(values);
    }

    /** 是否全部都不为空白。 */
    public static boolean isNoneBlank(CharSequence... values) {
        return StringUtils.isNoneBlank(values);
    }

    // ==================== 取默认值 ====================

    /** 若字符串为空白则返回默认值。 */
    public static String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.defaultIfBlank(value, defaultValue);
    }

    /** 若字符串为空则返回默认值。 */
    public static String defaultIfEmpty(String value, String defaultValue) {
        return StringUtils.defaultIfEmpty(value, defaultValue);
    }

    /** null 转空串。 */
    public static String defaultString(String value) {
        return StringUtils.defaultString(value);
    }

    // ==================== 查找/截取 ====================

    /**
     * 从指定下标开始查找子串第一次出现的位置。
     *
     * @param value
     *                被查找字符串
     * @param search
     *                待查找子串
     * @param startPos
     *                起始下标
     *
     * @return 子串下标；value 为 null 或未找到返回 -1
     */
    public static int indexOf(String value, String search, int startPos) {
        return StringUtils.indexOf(value, search, startPos);
    }

    /** 按起止下标截取。 */
    public static String substring(String value, int start) {
        return StringUtils.substring(value, start);
    }

    /** 按起止下标截取。 */
    public static String substring(String value, int start, int end) {
        return StringUtils.substring(value, start, end);
    }

    /** 截取分隔符第一次出现之前的部分。 */
    public static String substringBefore(String value, String separator) {
        return StringUtils.substringBefore(value, separator);
    }

    /** 截取分隔符第一次出现之后的部分。 */
    public static String substringAfter(String value, String separator) {
        return StringUtils.substringAfter(value, separator);
    }

    /** 截取分隔符最后一次出现之前的部分。 */
    public static String substringBeforeLast(String value, String separator) {
        return StringUtils.substringBeforeLast(value, separator);
    }

    /** 截取分隔符最后一次出现之后的部分，常用于取文件扩展名。 */
    public static String substringAfterLast(String value, String separator) {
        return StringUtils.substringAfterLast(value, separator);
    }

    /** 截取两个分隔符之间的内容。 */
    public static String substringBetween(String value, String open, String close) {
        return StringUtils.substringBetween(value, open, close);
    }

    // ==================== 大小写 ====================

    /** 首字母大写。 */
    public static String capitalize(String value) {
        return StringUtils.capitalize(value);
    }

    /** 首字母小写。 */
    public static String uncapitalize(String value) {
        return StringUtils.uncapitalize(value);
    }

    /** 全部转大写。 */
    public static String upperCase(String value) {
        return StringUtils.upperCase(value);
    }

    /** 全部转小写。 */
    public static String lowerCase(String value) {
        return StringUtils.lowerCase(value);
    }

    // ==================== 比较 ====================

    /** 比较两个字符串是否相等（null 安全）。 */
    public static boolean equals(CharSequence a, CharSequence b) {
        return StringUtils.equals(a, b);
    }

    /** 忽略大小写比较。 */
    public static boolean equalsIgnoreCase(CharSequence a, CharSequence b) {
        return StringUtils.equalsIgnoreCase(a, b);
    }

    /** 是否以指定前缀开头。 */
    public static boolean startsWith(String value, String prefix) {
        return StringUtils.startsWith(value, prefix);
    }

    /** 是否以指定后缀结尾。 */
    public static boolean endsWith(String value, String suffix) {
        return StringUtils.endsWith(value, suffix);
    }

    /** 是否包含指定字符序列。 */
    public static boolean contains(String value, CharSequence search) {
        return StringUtils.contains(value, search);
    }

    /** 忽略大小写包含。 */
    public static boolean containsIgnoreCase(String value, CharSequence search) {
        return StringUtils.containsIgnoreCase(value, search);
    }

    // ==================== 替换/删除 ====================

    /** 替换第一个匹配。 */
    public static String replace(String value, String search, String replacement) {
        return StringUtils.replace(value, search, replacement);
    }

    /** 替换所有匹配。 */
    public static String replaceAll(String value, String regex, String replacement) {
        return StringUtils.replaceAll(value, regex, replacement);
    }

    /** 删除所有匹配。 */
    public static String remove(String value, String remove) {
        return StringUtils.remove(value, remove);
    }

    /** 删除开头匹配。 */
    public static String removeStart(String value, String remove) {
        return StringUtils.removeStart(value, remove);
    }

    /** 删除结尾匹配。 */
    public static String removeEnd(String value, String remove) {
        return StringUtils.removeEnd(value, remove);
    }

    /** 删除所有空白字符。 */
    public static String deleteWhitespace(String value) {
        return StringUtils.deleteWhitespace(value);
    }

    // ==================== 分割/拼接 ====================

    /** 按分隔符拆分为数组。 */
    public static String[] split(String value, String separatorChars) {
        return StringUtils.split(value, separatorChars);
    }

    /** 拼接多个字符串，跳过 null。 */
    public static String join(Iterable<?> values, String separator) {
        return StringUtils.join(values, separator);
    }

    /** 拼接多个字符串。 */
    public static String join(Object[] values, String separator) {
        return StringUtils.join(values, separator);
    }

    // ==================== 其他 ====================

    /** 字符串长度（null 返回 0）。 */
    public static int length(CharSequence value) {
        return StringUtils.length(value);
    }

    /** 重复字符串。 */
    public static String repeat(String value, int repeat) {
        return StringUtils.repeat(value, repeat);
    }

    /** 左侧补空格到指定长度。 */
    public static String leftPad(String value, int size) {
        return StringUtils.leftPad(value, size);
    }

    /** 右侧补空格到指定长度。 */
    public static String rightPad(String value, int size) {
        return StringUtils.rightPad(value, size);
    }

    /** 居中（两侧补空格）。 */
    public static String center(String value, int size) {
        return StringUtils.center(value, size);
    }

    /** 去掉首尾空白。 */
    public static String trim(String value) {
        return StringUtils.trim(value);
    }
}
