package com.dayu.yiyibushe.common.exception;

/**
 * 说明：业务级错误码枚举（3xxx 段）。
 * <p>
 * 所有对用户展示的错误提示文案只能定义在本枚举中，业务代码禁止手写中文提示。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
public enum BizErrorCode implements ErrorCode {

    /** 业务校验失败（兜底，优先使用更具体的错误码） */
    BIZ_CHECK_FAIL("3001", "业务校验失败"),
    /** 数据不存在（兜底，优先使用更具体的错误码） */
    DATA_NOT_FOUND("3002", "数据不存在"),
    /** 数据已存在（兜底，优先使用更具体的错误码） */
    DATA_ALREADY_EXISTS("3003", "数据已存在"),
    /** 大模型任务失败 */
    DASHSCOPE_TASK_FAILED("3004", "大模型任务执行失败"),
    /** 大模型任务超时 */
    DASHSCOPE_TASK_TIMEOUT("3005", "大模型任务超时"),
    /** 大模型返回异常 */
    DASHSCOPE_RESPONSE_INVALID("3006", "大模型返回数据异常"),
    /** 未获取到登录信息 */
    AUTH_CONTEXT_MISSING("3007", "未获取到登录信息"),
    /** 流程未指定起始节点 */
    FLOW_START_NODE_MISSING("3008", "流程未指定起始节点"),
    /** 流程存在环，无法拓扑排序 */
    FLOW_HAS_CYCLE("3009", "流程存在环，无法拓扑排序"),
    /** 用户名已存在 */
    USERNAME_EXISTS("3010", "用户名已存在"),
    /** 用户名或密码不正确 */
    LOGIN_FAILED("3011", "用户名或密码不正确"),
    /** 分类素材已达上传数量上限 */
    ASSET_LIMIT_EXCEEDED("3012", "该分类素材已达上传数量上限，请先删除再上传"),
    /** 只能操作本人的图片 */
    ASSET_NOT_OWNER("3013", "只能操作本人的图片"),
    /** 至少需要一件衣物 */
    OUTFIT_NO_CLOTHES("3014", "至少需要一件衣物，请先上传或在请求中指定"),
    /** 任务不存在或已过期 */
    TASK_NOT_FOUND("3015", "任务不存在或已过期"),
    /** 只能使用本人的素材 */
    ASSET_OWNER_MISMATCH("3016", "只能使用本人的素材"),
    /** 同步对话模型不支持异步提交 */
    MODEL_SYNC_NO_SUBMIT("3017", "当前对话模型为同步模式，不支持异步提交"),
    /** 同步对话模型不支持任务轮询 */
    MODEL_SYNC_NO_POLL("3018", "当前对话模型为同步模式，不支持任务轮询"),
    /** 当前模型仅支持异步调用 */
    MODEL_ASYNC_ONLY("3019", "当前模型为异步模式，请使用异步提交与轮询"),
    /** 图片记录不存在或已删除 */
    ASSET_NOT_FOUND("3037", "图片记录不存在或已删除"),
    /** 情境不存在 */
    SCENE_NOT_FOUND("3038", "情境不存在或已删除"),
    /** 情境名称已存在 */
    SCENE_NAME_EXISTS("3039", "该情境名称已存在"),
    /** 图片已在该情境中 */
    SCENE_IMAGE_EXISTS("3040", "该图片已在此情境中"),
    /** 用户不存在 */
    USER_NOT_FOUND("3020", "用户不存在"),
    /** 未注册的模型编码 */
    MODEL_NOT_REGISTERED("3021", "未注册的模型编码"),
    /** 未知厂商 */
    PROVIDER_UNKNOWN("3022", "未知厂商，请先登记厂商信息"),
    /** 厂商未配置有效凭证 */
    PROVIDER_CREDENTIAL_MISSING("3023", "该厂商未配置有效凭证，请先配置"),
    /** 当前模型类型不受支持 */
    MODEL_TYPE_UNSUPPORTED("3024", "当前模型类型不受支持"),
    /** 原密码不正确 */
    OLD_PASSWORD_WRONG("3025", "原密码不正确"),
    /** 新密码不能为空 */
    NEW_PASSWORD_BLANK("3026", "新密码不能为空"),
    /** 单次上传图片数量超限 */
    UPLOAD_BATCH_EXCEEDED("3027", "单次最多上传 10 张图片"),
    /** 上传文件为空 */
    UPLOAD_FILE_EMPTY("3028", "上传文件不能为空"),
    /** 任务成功但未找到结果图 */
    RESULT_IMAGE_MISSING("3029", "任务成功但未找到结果图"),
    /** 未知素材类别 */
    ASSET_CATEGORY_UNKNOWN("3030", "未知素材类别"),
    /** 任务类型下没有任何可用节点 */
    TASK_CHAIN_MISSING("3031", "任务类型下没有可用节点，无法创建任务"),
    /** 节点绑定的任务类型为空 */
    TASK_CHAIN_TYPE_BLANK("3032", "节点绑定的任务类型不能为空"),
    /** 节点链为空 */
    TASK_CHAIN_EMPTY("3033", "节点链不能为空"),
    /** 节点类型为空 */
    TASK_NODE_TYPE_BLANK("3034", "节点类型不能为空"),
    /** 同一任务类型下节点类型重复 */
    TASK_NODE_TYPE_DUPLICATED("3035", "同一任务类型下节点类型不能重复"),
    /** 任务指定的重试策略不存在 */
    TASK_RETRY_STRATEGY_MISSING("3036", "任务指定的重试策略不存在");

    private final String code;
    private final String msg;

    /**
     * 构造器
     *
     * @param code
     *     错误码
     * @param msg
     *     错误提示文案
     */
    BizErrorCode(String code, String msg) {
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
