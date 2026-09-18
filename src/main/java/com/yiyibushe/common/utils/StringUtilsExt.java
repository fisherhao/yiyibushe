package com.yiyibushe.common.utils;

/**
 * 字符串工具类（内部使用 Apache Commons Lang3 实现，对外屏蔽底层依赖）。
 * <p>
 * 类名以 {@code UtilsExt} 结尾，与底层 {@code org.apache.commons.lang3.StringUtils} 明确区分，
 * 避免业务代码 import 时混淆。
 * <p>
 * 业务代码统一使用本类，不直接 import org.apache.commons.lang3.StringUtils，
 * 以便后续底层库升级或替换时只改这一处。
 *
 * @author Witty·Kid Fisher
 */
public final class StringUtilsExt {

    private StringUtilsExt() {
    }

    // ==================== 判空 ====================

    /** 判断字符串是否为空或全空白字符。 */
    public static boolean isBlank(String value) {
        return org.apache.commons.lang3.StringUtils.isBlank(value);
    }

    /** 判断字符串是否非空且不全为空白字符。 */
    public static boolean isNotBlank(String value) {
        return org.apache.commons.lang3.StringUtils.isNotBlank(value);
    }

    /** 判断字符串是否为空（null 或长度为 0）。 */
    public static boolean isEmpty(String value) {
        return org.apache.commons.lang3.StringUtils.isEmpty(value);
    }

    /** 判断字符串是否非空。 */
    public static boolean isNotEmpty(String value) {
        return org.apache.commons.lang3.StringUtils.isNotEmpty(value);
    }

    /** 是否任意一个为空白。 */
    public static boolean isAnyBlank(CharSequence... values) {
        return org.apache.commons.lang3.StringUtils.isAnyBlank(values);
    }

    /** 是否全部都不为空白。 */
    public static boolean isNoneBlank(CharSequence... values) {
        return org.apache.commons.lang3.StringUtils.isNoneBlank(values);
    }

    // ==================== 取默认值 ====================

    /** 若字符串为空白则返回默认值。 */
    public static String defaultIfBlank(String value, String defaultValue) {
        return org.apache.commons.lang3.StringUtils.defaultIfBlank(value, defaultValue);
    }

    /** 若字符串为空则返回默认值。 */
    public static String defaultIfEmpty(String value, String defaultValue) {
        return org.apache.commons.lang3.StringUtils.defaultIfEmpty(value, defaultValue);
    }

    /** null 转空串。 */
    public static String defaultString(String value) {
        return org.apache.commons.lang3.StringUtils.defaultString(value);
    }

    // ==================== 截取 ====================

    /** 按起止下标截取。 */
    public static String substring(String value, int start) {
        return org.apache.commons.lang3.StringUtils.substring(value, start);
    }

    /** 按起止下标截取。 */
    public static String substring(String value, int start, int end) {
        return org.apache.commons.lang3.StringUtils.substring(value, start, end);
    }

    /** 截取分隔符第一次出现之前的部分。 */
    public static String substringBefore(String value, String separator) {
        return org.apache.commons.lang3.StringUtils.substringBefore(value, separator);
    }

    /** 截取分隔符第一次出现之后的部分。 */
    public static String substringAfter(String value, String separator) {
        return org.apache.commons.lang3.StringUtils.substringAfter(value, separator);
    }

    /** 截取分隔符最后一次出现之前的部分。 */
    public static String substringBeforeLast(String value, String separator) {
        return org.apache.commons.lang3.StringUtils.substringBeforeLast(value, separator);
    }

    /** 截取分隔符最后一次出现之后的部分，常用于取文件扩展名。 */
    public static String substringAfterLast(String value, String separator) {
        return org.apache.commons.lang3.StringUtils.substringAfterLast(value, separator);
    }

    /** 截取两个分隔符之间的内容。 */
    public static String substringBetween(String value, String open, String close) {
        return org.apache.commons.lang3.StringUtils.substringBetween(value, open, close);
    }

    // ==================== 大小写 ====================

    /** 首字母大写。 */
    public static String capitalize(String value) {
        return org.apache.commons.lang3.StringUtils.capitalize(value);
    }

    /** 首字母小写。 */
    public static String uncapitalize(String value) {
        return org.apache.commons.lang3.StringUtils.uncapitalize(value);
    }

    /** 全部转大写。 */
    public static String upperCase(String value) {
        return org.apache.commons.lang3.StringUtils.upperCase(value);
    }

    /** 全部转小写。 */
    public static String lowerCase(String value) {
        return org.apache.commons.lang3.StringUtils.lowerCase(value);
    }

    // ==================== 比较 ====================

    /** 比较两个字符串是否相等（null 安全）。 */
    public static boolean equals(CharSequence a, CharSequence b) {
        return org.apache.commons.lang3.StringUtils.equals(a, b);
    }

    /** 忽略大小写比较。 */
    public static boolean equalsIgnoreCase(CharSequence a, CharSequence b) {
        return org.apache.commons.lang3.StringUtils.equalsIgnoreCase(a, b);
    }

    /** 是否以指定前缀开头。 */
    public static boolean startsWith(String value, String prefix) {
        return org.apache.commons.lang3.StringUtils.startsWith(value, prefix);
    }

    /** 是否以指定后缀结尾。 */
    public static boolean endsWith(String value, String suffix) {
        return org.apache.commons.lang3.StringUtils.endsWith(value, suffix);
    }

    /** 是否包含指定字符序列。 */
    public static boolean contains(String value, CharSequence search) {
        return org.apache.commons.lang3.StringUtils.contains(value, search);
    }

    /** 忽略大小写包含。 */
    public static boolean containsIgnoreCase(String value, CharSequence search) {
        return org.apache.commons.lang3.StringUtils.containsIgnoreCase(value, search);
    }

    // ==================== 替换/删除 ====================

    /** 替换第一个匹配。 */
    public static String replace(String value, String search, String replacement) {
        return org.apache.commons.lang3.StringUtils.replace(value, search, replacement);
    }

    /** 替换所有匹配。 */
    public static String replaceAll(String value, String regex, String replacement) {
        return org.apache.commons.lang3.StringUtils.replaceAll(value, regex, replacement);
    }

    /** 删除所有匹配。 */
    public static String remove(String value, String remove) {
        return org.apache.commons.lang3.StringUtils.remove(value, remove);
    }

    /** 删除开头匹配。 */
    public static String removeStart(String value, String remove) {
        return org.apache.commons.lang3.StringUtils.removeStart(value, remove);
    }

    /** 删除结尾匹配。 */
    public static String removeEnd(String value, String remove) {
        return org.apache.commons.lang3.StringUtils.removeEnd(value, remove);
    }

    /** 删除所有空白字符。 */
    public static String deleteWhitespace(String value) {
        return org.apache.commons.lang3.StringUtils.deleteWhitespace(value);
    }

    // ==================== 分割/拼接 ====================

    /** 按分隔符拆分为数组。 */
    public static String[] split(String value, String separatorChars) {
        return org.apache.commons.lang3.StringUtils.split(value, separatorChars);
    }

    /** 拼接多个字符串，跳过 null。 */
    public static String join(Iterable<?> values, String separator) {
        return org.apache.commons.lang3.StringUtils.join(values, separator);
    }

    /** 拼接多个字符串。 */
    public static String join(Object[] values, String separator) {
        return org.apache.commons.lang3.StringUtils.join(values, separator);
    }

    // ==================== 其他 ====================

    /** 字符串长度（null 返回 0）。 */
    public static int length(CharSequence value) {
        return org.apache.commons.lang3.StringUtils.length(value);
    }

    /** 重复字符串。 */
    public static String repeat(String value, int repeat) {
        return org.apache.commons.lang3.StringUtils.repeat(value, repeat);
    }

    /** 左侧补空格到指定长度。 */
    public static String leftPad(String value, int size) {
        return org.apache.commons.lang3.StringUtils.leftPad(value, size);
    }

    /** 右侧补空格到指定长度。 */
    public static String rightPad(String value, int size) {
        return org.apache.commons.lang3.StringUtils.rightPad(value, size);
    }

    /** 居中（两侧补空格）。 */
    public static String center(String value, int size) {
        return org.apache.commons.lang3.StringUtils.center(value, size);
    }

    /** 去掉首尾空白。 */
    public static String trim(String value) {
        return org.apache.commons.lang3.StringUtils.trim(value);
    }
}
