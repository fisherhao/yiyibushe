package com.dayu.yiyibushe.common.exception;

/**
 * 说明：参数级错误码枚举（2xxx 段）。
 * <p>
 * 所有对用户展示的错误提示文案只能定义在本枚举中，业务代码禁止手写中文提示。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.5
 */
public enum ParamErrorCode implements ErrorCode {

    /** 参数为空（兜底，优先使用更具体的错误码） */
    PARAM_NULL("2001", "参数不能为空"),
    /** 参数不合法（兜底，优先使用更具体的错误码） */
    PARAM_INVALID("2002", "参数不合法"),
    /** 请求对象为空 */
    REQUEST_NULL("2003", "请求对象不能为空"),
    /** 业务处理函数为空 */
    ACTION_NULL("2004", "业务处理函数不能为空"),
    /** 模型定义或编码为空 */
    MODEL_DEF_BLANK("2005", "模型定义或编码不能为空"),
    /** 用户 ID 为空 */
    USER_ID_BLANK("2006", "用户 ID 不能为空"),
    /** 用户信息为空 */
    USER_INFO_BLANK("2007", "用户信息不能为空"),
    /** 厂商标识为空 */
    PROVIDER_CODE_BLANK("2008", "厂商标识不能为空"),
    /** 模型编码为空 */
    MODEL_CODE_BLANK("2009", "模型编码不能为空"),
    /** 用户名和密码为空 */
    USERNAME_PASSWORD_BLANK("2010", "用户名和密码不能为空"),
    /** 单个文件超过大小上限 */
    FILE_TOO_LARGE("2011", "单张图片不能超过 10MB"),
    /** 任务类型不能为空 */
    TASK_TYPE_BLANK("2012", "任务类型不能为空"),
    /** 任务 ID 不能为空 */
    TASK_ID_BLANK("2013", "任务 ID 不能为空"),
    /** 节点类型不能为空 */
    NODE_TYPE_BLANK("2014", "节点类型不能为空"),
    /** 发号序列名（表名）不能为空 */
    SEQUENCE_NAME_BLANK("2015", "发号序列名（表名）不能为空");

    private final String code;
    private final String msg;

    /**
     * 构造器
     *
     * @param code
     *             错误码
     * @param msg
     *             错误提示文案
     */
    ParamErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * 获取错误提示文案
     *
     * @return 提示文案
     */
    @Override
    public String getMsg() {
        return msg;
    }
}
