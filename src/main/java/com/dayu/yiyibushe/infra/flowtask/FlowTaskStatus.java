package com.dayu.yiyibushe.infra.flowtask;

/**
 * 说明：任务状态机。
 * <ul>
 *   <li>{@link #INIT}：初始态，等待被调起（外部定时捞取 / 手动触发）；</li>
 *   <li>{@link #RUNNING}：执行中（含节点等待回调期间——等回调的是节点，任务仍在执行中）；</li>
 *   <li>{@link #SUCCESS}：终态，全部节点成功；</li>
 *   <li>{@link #FAILED}：终态，重试预算耗尽，怎么调都不再执行。</li>
 * </ul>
 * 节点失败且有重试预算时任务回到 INIT 并刷新 gmtFire，由调用方（定时捞取）再次调起。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.4
 */
public enum FlowTaskStatus {

    /** 初始态：等待被调起 */
    INIT,

    /** 执行中 */
    RUNNING,

    /** 终态：全部节点成功 */
    SUCCESS,

    /** 终态：重试预算耗尽 */
    FAILED
}
