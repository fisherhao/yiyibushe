package com.dayu.yiyibushe.infra.flowtask;

import com.dayu.yiyibushe.app.task.FailOnceNode;
import com.dayu.yiyibushe.app.task.PassThroughNode;
import com.dayu.yiyibushe.app.task.SubmitCallbackNode;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.infra.flowtask.retry.FixedIntervalRetryStrategy;
import com.dayu.yiyibushe.infra.mq.MessageProducer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 说明：flowtask 框架集成测试，覆盖三条主链路：
 * <ol>
 *   <li>回调消息经 MQ 推送驱动 load1 完成；load2 失败后任务回 INIT，
 *       再次 trigger 从断点续跑到收口；</li>
 *   <li>trigger 幂等（RUNNING 任务被重复触发直接忽略）+ callback 唤醒待回调节点；</li>
 *   <li>重试预算耗尽后任务进入终态 FAILED，再触发也不再执行。</li>
 * </ol>
 * 任务数据落在 MySQL 双表（flow_task + task_node），前后均清理，不污染真实数据。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.5
 */
@Import(FlowTaskFrameworkTest.TestFailChainConfig.class)
@SpringBootTest(properties = {"flowtask.retry.fixed-interval-millis=100"})
class FlowTaskFrameworkTest {

    /** 状态等待超时（毫秒） */
    private static final long AWAIT_TIMEOUT_MILLIS = 5000L;

    /** 状态轮询间隔（毫秒） */
    private static final long AWAIT_SLEEP_MILLIS = 50L;

    /** 失败测试链任务类型 */
    private static final String TEST_FAIL_CHAIN = "TEST_FAIL_CHAIN";

    @Autowired
    private FlowTaskTemplate flowTaskTemplate;

    @Autowired
    private TaskEngine taskEngine;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 每个用例前清空 flow_task 与 task_node 双表，保证状态干净
     */
    @BeforeEach
    void cleanTaskTables() {
        jdbcTemplate.update("DELETE FROM task_node");
        jdbcTemplate.update("DELETE FROM flow_task");
    }

    /**
     * 每个用例后清空 flow_task 与 task_node 双表，不污染真实数据
     */
    @AfterEach
    void removeTaskTables() {
        jdbcTemplate.update("DELETE FROM task_node");
        jdbcTemplate.update("DELETE FROM flow_task");
    }

    /**
     * 用例一：回调消息经 MQ 推送驱动 load1 完成；load2 首跑失败后
     * 任务回到 INIT（gmtFire 已被引擎改为下次触发时间），再次 trigger
     * 从 load2 断点续跑到 load3 收口。
     */
    @Test
    void task_shouldAdvanceByCallbackAndResumeFromFailedNode() throws InterruptedException {
        Long taskId = flowTaskTemplate.createTask(SubmitCallbackNode.TASK_TYPE,
                System.currentTimeMillis(), new HashMap<>(), 3,
                FixedIntervalRetryStrategy.CODE);

        // 首次触发：load1 提交后进入待回调（任务保持 RUNNING）
        taskEngine.trigger(taskId);
        FlowTask dispatched = flowTaskTemplate.requireTask(taskId);
        assertEquals(FlowTaskStatus.RUNNING, dispatched.getStatus());
        assertEquals(TaskNodeStatus.WAIT,
                dispatched.getNodeRecords().get(0).getStatus());
        // 指针停在待回调节点上
        assertEquals(SubmitCallbackNode.NODE_TYPE, dispatched.getCurrentNodeType());

        // 回调消息走 MQ 推送通道（本地发送、本地消费），消息只带任务 ID
        messageProducer.send(TaskCallbackMessage.TOPIC, TaskCallbackMessage.of(taskId));

        // load1 成功 -> load2 首跑失败 -> 任务回 INIT 等下次触发
        FlowTask retryWaiting = awaitTask(taskId,
                task -> task.getStatus() == FlowTaskStatus.INIT);
        assertEquals(1, retryWaiting.getRetryCount());
        assertEquals(FailOnceNode.NODE_TYPE, retryWaiting.getCurrentNodeType());

        // 模拟调用方定时捞取：对 INIT 任务再次 trigger，从 load2 断点续跑到收口
        FlowTask finished = awaitTaskByTriggering(taskId,
                task -> task.getStatus() == FlowTaskStatus.SUCCESS);

        // 断点续跑验证：load1 不重跑、load2 第二次成功、load3 收口
        assertEquals(1, finished.getRetryCount());
        assertEquals("", finished.getCurrentNodeType());
        assertEquals(TaskNodeStatus.SUCCESS, finished.getNodeRecords().get(0).getStatus());
        assertEquals("回调处理完成", finished.getNodeRecords().get(0).getOutput());
        assertEquals(TaskNodeStatus.SUCCESS, finished.getNodeRecords().get(1).getStatus());
        assertEquals(TaskNodeStatus.SUCCESS, finished.getNodeRecords().get(2).getStatus());
        assertEquals(Integer.valueOf(2),
                CollectionUtilExt.getInteger(finished.getTaskContext(), FailOnceNode.NODE_TYPE
                        + ".executeCount"));
    }

    /**
     * 用例二：RUNNING 任务的重复 trigger 被幂等忽略；callback 按 currentNodeType
     * 定位 WAIT 节点执行 receipt 并继续推进（经 load2 失败回 INIT 再 trigger 收口）。
     */
    @Test
    void task_shouldIgnoreDuplicateTriggerAndResumeByCallback() throws InterruptedException {
        Long taskId = flowTaskTemplate.createTask(SubmitCallbackNode.TASK_TYPE,
                System.currentTimeMillis(), new HashMap<>(), 3,
                FixedIntervalRetryStrategy.CODE);

        taskEngine.trigger(taskId);
        // 重复触发：任务 RUNNING，引擎直接忽略（状态机硬门槛）
        taskEngine.trigger(taskId);
        FlowTask afterDuplicate = flowTaskTemplate.requireTask(taskId);
        assertEquals(FlowTaskStatus.RUNNING, afterDuplicate.getStatus());
        assertEquals(TaskNodeStatus.WAIT, afterDuplicate.getNodeRecords().get(0).getStatus());
        assertEquals(1, CollectionUtilExt.getSize(afterDuplicate.getNodeRecords()));

        // 手动回调（等价 MQ 推送）：load1 收口，load2 首跑失败回 INIT
        taskEngine.callback(taskId);
        FlowTask retryWaiting = awaitTask(taskId,
                task -> task.getStatus() == FlowTaskStatus.INIT);
        assertEquals(FailOnceNode.NODE_TYPE, retryWaiting.getCurrentNodeType());

        // 再次触发续跑到收口
        FlowTask finished = awaitTaskByTriggering(taskId,
                task -> task.getStatus() == FlowTaskStatus.SUCCESS);
        assertEquals(3, CollectionUtilExt.getSize(finished.getNodeRecords()));
    }

    /**
     * 用例三：失败测试链第二个节点永远失败，验证重试预算耗尽后
     * 任务进入终态 FAILED，且再次触发也不执行。
     */
    @Test
    void task_shouldFailFinallyWhenRetryBudgetExhausted() throws InterruptedException {
        Long taskId = flowTaskTemplate.createTask(TEST_FAIL_CHAIN,
                System.currentTimeMillis(), new HashMap<>(), 2,
                FixedIntervalRetryStrategy.CODE);

        // 首跑：node1 成功，node2 失败 -> 任务回 INIT 等重试
        taskEngine.trigger(taskId);
        FlowTask firstRetry = awaitTask(taskId,
                task -> task.getStatus() == FlowTaskStatus.INIT);
        assertEquals(1, firstRetry.getRetryCount());

        // 模拟调用方定时捞取：node2 连续失败，两次重试预算耗尽后进入终态 FAILED
        FlowTask failed = awaitTaskByTriggering(taskId,
                task -> task.getStatus() == FlowTaskStatus.FAILED);
        assertEquals(2, failed.getRetryCount());
        assertEquals("testNode2", failed.getCurrentNodeType());
        assertEquals(TaskNodeStatus.SUCCESS, failed.getNodeRecords().get(0).getStatus());
        assertEquals(TaskNodeStatus.FAILED, failed.getNodeRecords().get(1).getStatus());

        // 终态任务再触发也不执行（硬门槛：只有 INIT 允许开跑）
        taskEngine.trigger(taskId);
        assertEquals(FlowTaskStatus.FAILED, flowTaskTemplate.requireTask(taskId).getStatus());
    }

    /**
     * 轮询等待任务到达目标状态，超时直接失败
     *
     * @param taskId
     *     任务 ID
     * @param condition
     *     目标状态条件
     * @return 满足条件的任务
     * @throws InterruptedException
     *     轮询等待被中断
     */
    private FlowTask awaitTask(Long taskId, Predicate<FlowTask> condition)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + AWAIT_TIMEOUT_MILLIS;
        FlowTask task = null;
        while (System.currentTimeMillis() < deadline) {
            task = flowTaskTemplate.requireTask(taskId);
            if (condition.test(task)) {
                return task;
            }
            Thread.sleep(AWAIT_SLEEP_MILLIS);
        }
        fail("等待任务状态超时 taskId=" + taskId + " 当前=" + task);
        return task;
    }

    /**
     * 模拟调用方定时捞取的等待：每轮对 INIT 任务执行一次 {@link TaskEngine#trigger(Long)}
     * （时间到期判断由调用方保证，本测试直接触发），直到任务到达目标状态或超时
     *
     * @param taskId
     *     任务 ID
     * @param condition
     *     目标状态条件
     * @return 满足条件的任务
     * @throws InterruptedException
     *     轮询等待被中断
     */
    private FlowTask awaitTaskByTriggering(Long taskId, Predicate<FlowTask> condition)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + AWAIT_TIMEOUT_MILLIS;
        FlowTask task = null;
        while (System.currentTimeMillis() < deadline) {
            taskEngine.trigger(taskId);
            task = flowTaskTemplate.requireTask(taskId);
            if (condition.test(task)) {
                return task;
            }
            Thread.sleep(AWAIT_SLEEP_MILLIS);
        }
        fail("定时触发等待任务状态超时 taskId=" + taskId + " 当前=" + task);
        return task;
    }

    /**
     * 测试用节点：直接成功，用于构造最短失败测试链
     */
    private static class DirectSuccessNode implements TaskNodeAction {

        /** 节点类型 */
        public static final String NODE_TYPE = "testNode1";

        /**
         * 声明节点类型
         *
         * @return testNode1
         */
        @Override
        public String getNodeType() {
            return NODE_TYPE;
        }

        /**
         * 直接返回成功
         *
         * @param context
         *     任务上下文
         * @return 成功结果
         */
        @Override
        public TaskNodeResult execute(TaskContext context) {
            return TaskNodeResult.success("ok");
        }
    }

    /**
     * 测试用节点：永远失败，用于验证重试预算耗尽的终态流转
     */
    private static class AlwaysFailNode implements TaskNodeAction {

        /** 节点类型 */
        public static final String NODE_TYPE = "testNode2";

        /**
         * 声明节点类型
         *
         * @return testNode2
         */
        @Override
        public String getNodeType() {
            return NODE_TYPE;
        }

        /**
         * 永远返回失败
         *
         * @param context
         *     任务上下文
         * @return 失败结果
         */
        @Override
        public TaskNodeResult execute(TaskContext context) {
            return TaskNodeResult.failed("ALWAYS_FAIL_FOR_TEST");
        }
    }

    /**
     * 测试编排装配：业务侧显式编排（数组顺序即执行顺序），
     * 以 @Primary 替换主上下文的演示编排，并同时注册演示链与失败测试链
     */
    @TestConfiguration
    static class TestFailChainConfig {

        /**
         * 注册测试编排（含演示链 + 失败测试链）
         *
         * @return 测试编排
         */
        @Bean
        @Primary
        TaskNodeStrategy testTaskNodeStrategy() {
            return new TaskNodeStrategy() {
                {
                    registerChain(SubmitCallbackNode.TASK_TYPE,
                            new SubmitCallbackNode(), new FailOnceNode(), new PassThroughNode());
                    registerChain(TEST_FAIL_CHAIN, new DirectSuccessNode(), new AlwaysFailNode());
                }
            };
        }
    }
}
