package com.dayu.yiyibushe.app.task;

import com.dayu.yiyibushe.infra.flowtask.TaskContext;
import com.dayu.yiyibushe.infra.flowtask.TaskNodeAction;
import com.dayu.yiyibushe.infra.flowtask.TaskNodeResult;
import com.dayu.yiyibushe.common.util.LogUtilExt;
import org.slf4j.Logger;

/**
 * 说明：演示节点 failOnce——首次执行模拟失败，重试后成功，
 * 用来演示「failOnce 失败 -> 任务回 INIT -> 下一次从 failOnce 继续执行」的断点重试机制。
 * <p>
 * 用任务上下文里的执行计数判断是否为首跑：首跑失败，重跑成功。
 * 执行顺序由 {@link LoadDemoTaskNodeStrategy} 的编排数组决定，本节点排在第二个；
 * 本节点无回调场景，不重写 receipt（接口默认返回 null）。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
public class FailOnceNode implements TaskNodeAction {

    private static final Logger log = LogUtilExt.getLogger(FailOnceNode.class);

    /** 节点类型 */
    public static final String NODE_TYPE = "failOnce";

    /** 执行计数的上下文键 */
    private static final String KEY_EXECUTE_COUNT = NODE_TYPE + ".executeCount";

    /** 首次失败原因（模拟数据，非用户提示文案） */
    private static final String SIMULATED_FIRST_FAILURE = "SIMULATED_FIRST_ATTEMPT_FAILURE";

    /**
     * 声明节点类型
     *
     * @return failOnce
     */
    @Override
    public String getNodeType() {
        return NODE_TYPE;
    }

    /**
     * 首次执行模拟失败，重试执行成功
     *
     * @param context
     *     任务上下文
     * @return 首跑失败、重跑成功
     */
    @Override
    public TaskNodeResult execute(TaskContext context) {
        Integer executeCount = context.getInteger(KEY_EXECUTE_COUNT);
        int nextCount = (executeCount == null ? 0 : executeCount) + 1;
        context.put(KEY_EXECUTE_COUNT, nextCount);
        if (nextCount == 1) {
            LogUtilExt.warn(log, "[FlowTask-演示] failOnce 首次执行失败（模拟）");
            return TaskNodeResult.failed(SIMULATED_FIRST_FAILURE);
        }
        LogUtilExt.info(log, "[FlowTask-演示] failOnce 第 {0} 次执行成功", nextCount);
        return TaskNodeResult.success("第二次执行成功");
    }
}