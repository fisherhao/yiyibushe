package com.dayu.yiyibushe.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 说明：统一日志门面工具。
 * <p>
 * logger 由各业务类自己持有（经 {@link #getLogger(Class)} 创建，日志归属地仍在各业务类），
 * 但所有打印动作统一走本工具，保证日志行为只有一个出口：
 * 未来要统一增强（traceId、统一格式、采样、上报等）只改本类一处即可。
 * <p>
 * 消息模板使用编号占位：{@code {0}、{1}、{2}…}，依次对应第 1、2、3 个参数，例如：
 * <pre>
 *     LogUtilExt.info(log, "上传成功: bucket={0}, key={1}, etag={2}", bucketName, key, etag);
 * </pre>
 * 变长参数最后一位传 Throwable 时，自动作为异常打印完整堆栈，不参与占位编号。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public final class LogUtilExt {

    /** 工具类禁止实例化 */
    private LogUtilExt() {
    }

    /**
     * 创建业务类专属 logger
     *
     * @param clazz
     *     业务类
     * @return 该类的 logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * debug 级别日志（排查细节用，生产默认关闭）
     *
     * @param logger
     *     业务类持有的 logger
     * @param template
     *     消息模板，{0}、{1}… 编号占位
     * @param args
     *     占位参数，最后一位可为 Throwable
     */
    public static void debug(Logger logger, String template, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(format(template, args));
        }
    }

    /**
     * info 级别日志（正常流程）
     *
     * @param logger
     *     业务类持有的 logger
     * @param template
     *     消息模板，{0}、{1}… 编号占位
     * @param args
     *     占位参数，最后一位可为 Throwable
     */
    public static void info(Logger logger, String template, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(format(template, args));
        }
    }

    /**
     * warn 级别日志（可自动恢复的异常/忽略类事件）
     *
     * @param logger
     *     业务类持有的 logger
     * @param template
     *     消息模板，{0}、{1}… 编号占位
     * @param args
     *     占位参数，最后一位可为 Throwable
     */
    public static void warn(Logger logger, String template, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(format(template, args));
        }
    }

    /**
     * error 级别日志（异常事件）
     *
     * @param logger
     *     业务类持有的 logger
     * @param template
     *     消息模板，{0}、{1}… 编号占位
     * @param args
     *     占位参数，最后一位传 Throwable 时打印完整堆栈且不参与占位编号
     */
    public static void error(Logger logger, String template, Object... args) {
        if (!logger.isErrorEnabled()) {
            return;
        }
        if (args.length > 0 && args[args.length - 1] instanceof Throwable throwable) {
            Object[] plainArgs = Arrays.copyOf(args, args.length - 1);
            logger.error(format(template, plainArgs), throwable);
            return;
        }
        logger.error(format(template, args));
    }

    /**
     * 把模板中的 {0}、{1}… 编号占位替换成实际参数（按参数下标替换，
     * 模板里没出现的占位参数忽略，模板里缺参数的占位保持原样）
     *
     * @param template
     *     消息模板
     * @param args
     *     占位参数（不含 Throwable）
     * @return 渲染后的消息
     */
    private static String format(String template, Object... args) {
        if (args.length == 0) {
            return template;
        }
        String result = template;
        for (int i = 0; i < args.length; i++) {
            String token = "{" + i + "}";
            if (!result.contains(token)) {
                break;
            }
            result = result.replace(token, String.valueOf(args[i]));
        }
        return result;
    }
}
