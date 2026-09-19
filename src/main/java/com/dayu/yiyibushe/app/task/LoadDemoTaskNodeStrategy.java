package com.dayu.yiyibushe.app.task;

import com.dayu.yiyibushe.infra.flowtask.TaskNodeStrategy;
import org.springframework.stereotype.Component;

/**
 * 说明：演示任务类型的节点编排——业务侧继承 {@link TaskNodeStrategy}，
 * 在构造器里用 {@link #registerChain} 把 LOAD_DEMO 链的三个节点
 * <strong>按执行顺序</strong>显式传入：submitCallback（链首，等回调）
 * -> failOnce（第二个，首跑失败演示重试）-> passThrough（链尾，收口）。
 * <p>
 * 数组顺序即执行顺序，框架没有排序值的概念；引擎用 for 循环按链从头跑到尾。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class LoadDemoTaskNodeStrategy extends TaskNodeStrategy {

    /**
     * 构造器：编排 LOAD_DEMO 任务类型的节点链
     */
    public LoadDemoTaskNodeStrategy() {
        registerChain(SubmitCallbackNode.TASK_TYPE,
                new SubmitCallbackNode(),
                new FailOnceNode(),
                new PassThroughNode());
    }
}
