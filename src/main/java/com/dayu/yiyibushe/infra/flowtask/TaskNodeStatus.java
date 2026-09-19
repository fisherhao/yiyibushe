package com.dayu.yiyibushe.infra.flowtask;

/**
 * 说明：节点状态机。
 * <ul>
 *   <li>{@link #INIT}：初始态，还没轮到执行；</li>
 *   <li>{@link #RUNNING}：执行中；</li>
 *   <li>{@link #WAIT}：待回调——execute 返回 WAIT 后进入，等外部 callback 唤醒走 receipt；</li>
 *   <li>{@link #SUCCESS}：节点收口（执行成功或 receipt 处理成功）；</li>
 *   <li>{@link #FAILED}：节点失败（重试续跑时会被重置回 RUNNING 重新执行）。</li>
 * </ul>
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
public enum TaskNodeStatus {

    /** 初始态 */
    INIT,

    /** 执行中 */
    RUNNING,

    /** 待回调 */
    WAIT,

    /** 节点成功（收口） */
    SUCCESS,

    /** 节点失败 */
    FAILED
}
