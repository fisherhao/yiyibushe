package com.dayu.yiyibushe.infra.ai.trace;

import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 全链路执行轨迹：一次请求一个实例，在 Controller、Agent、工具之间透传
 * （经 AgentScope RuntimeContext 按类型存取，跨响应式线程不丢失）。
 * <p>
 * 记录的是"技术执行路径"：第几步调了哪个方法、命中哪个技能/工具、
 * 外部源还是缓存、耗时多少——全部由代码直接写入，与大模型的自然语言回复无关。
 * 同时以 info 级别输出到控制台，页面与控制台看到的步骤一致。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
public class ExecutionTrace {

    private static final Logger log = LogUtilExt.getLogger(ExecutionTrace.class);

    /** 控制台时间格式（只显示时分秒毫秒） */
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /** 步骤列表：Reactor 工具执行在 boundedElastic 线程，需线程安全 */
    private final CopyOnWriteArrayList<TraceStep> steps = new CopyOnWriteArrayList<>();

    /** 已命中的技能名（去重，保证同一技能只记录一次"加载"） */
    private final Set<String> hitSkills = ConcurrentHashMap.newKeySet();

    /** 命中技能的先后顺序 */
    private final CopyOnWriteArrayList<String> hitSkillOrder = new CopyOnWriteArrayList<>();

    /** 命中工具（function calling）的先后顺序，不去重（同一句问两个同类问题可重复） */
    private final CopyOnWriteArrayList<String> hitToolOrder = new CopyOnWriteArrayList<>();

    /** 已懒加载正文的技能（闸门据此判断业务工具能否放行） */
    private final Set<String> loadedSkillInstructions = ConcurrentHashMap.newKeySet();

    /**
     * 记录工具命中（模型 function calling 决策的直接证据）
     *
     * @param toolName
     *     工具名
     */
    public void hitTool(String toolName) {
        hitToolOrder.add(toolName);
    }

    /**
     * 获取本次请求命中的全部工具名（按命中先后）
     *
     * @return 工具名列表
     */
    public List<String> getHitTools() {
        return List.copyOf(hitToolOrder);
    }

    /**
     * 记录技能命中：同一技能首次命中时才写入步骤
     *
     * @param skillName
     *     技能名（对应 SKILL.md frontmatter 中 name）
     */
    public void hitSkill(String skillName) {
        if (hitSkills.add(skillName)) {
            hitSkillOrder.add(skillName);
            step(TracePhase.SKILL, "模型路由命中技能【{0}】，加载其 SKILL.md 指令", skillName);
        }
    }

    /**
     * 获取本次请求命中的全部技能名（按命中先后）
     *
     * @return 技能名列表
     */
    public List<String> getHitSkills() {
        return List.copyOf(hitSkillOrder);
    }

    /**
     * 标记某技能正文已懒加载（由 load-skill-instructions 成功后写入）
     *
     * @param skillName
     *     技能名
     */
    public void markSkillInstructionsLoaded(String skillName) {
        loadedSkillInstructions.add(skillName);
    }

    /**
     * 查询某技能正文是否已懒加载：业务工具据此做闸门判断
     *
     * @param skillName
     *     技能名
     * @return 已加载返回 true
     */
    public boolean isSkillInstructionsLoaded(String skillName) {
        return loadedSkillInstructions.contains(skillName);
    }

    /**
     * 记录一个瞬时步骤（无耗时）
     *
     * @param phase
     *     阶段类型
     * @param pattern
     *     内容模板（{0} 编号占位，经 LogUtilExt 同款规则渲染）
     * @param args
     *     占位参数
     */
    public void step(String phase, String pattern, Object... args) {
        recordStep(phase, null, pattern, args);
    }

    /**
     * 记录一个带耗时的步骤
     *
     * @param phase
     *     阶段类型
     * @param durationMillis
     *     耗时毫秒
     * @param pattern
     *     内容模板
     * @param args
     *     占位参数
     */
    public void stepWithDuration(String phase, long durationMillis, String pattern, Object... args) {
        recordStep(phase, durationMillis, pattern, args);
    }

    /**
     * 统一组装步骤并输出控制台
     *
     * @param phase
     *     阶段类型
     * @param durationMillis
     *     耗时（可空）
     * @param pattern
     *     内容模板
     * @param args
     *     占位参数
     */
    private void recordStep(String phase, Long durationMillis, String pattern, Object... args) {
        String content = renderPattern(pattern, args);
        TraceStep traceStep = new TraceStep(CollectionUtilExt.getSize(steps) + 1,
                System.currentTimeMillis(), durationMillis, phase, content);
        steps.add(traceStep);
        String durationText = Objects.isNull(durationMillis) ? ""
                : " (" + durationMillis + "ms)";
        LogUtilExt.info(log, "[Trace] 步骤{0} [{1}] {2}{3}",
                traceStep.index(), phase, content, durationText);
    }

    /**
     * 渲染 {0} 编号占位（与 LogUtilExt 语义一致：缺参数保留原占位，多余参数忽略）
     *
     * @param pattern
     *     模板
     * @param args
     *     参数
     * @return 渲染结果
     */
    private String renderPattern(String pattern, Object... args) {
        String result = pattern;
        for (int i = 0; i < args.length; i++) {
            String placeholder = "{" + i + "}";
            if (StringUtilExt.contains(result, placeholder)) {
                result = StringUtilExt.replace(result, placeholder, String.valueOf(args[i]));
            }
        }
        return result;
    }

    /**
     * 列出全部步骤（顺序即执行顺序）
     *
     * @return 不可变步骤列表快照
     */
    public List<TraceStep> getSteps() {
        return List.copyOf(steps);
    }

    /**
     * 统计指定阶段出现次数（如 TOOL 次数 = 模型命中工具的次数）
     *
     * @param phase
     *     阶段类型
     * @return 出现次数
     */
    public long countByPhase(String phase) {
        return CollectionUtilExt.toStream(steps)
                .filter(item -> StringUtilExt.equals(phase, item.phase()))
                .count();
    }

    /**
     * 生成控制台/页面可读的纯文本时间线（供调试与测试断言）
     *
     * @return 时间线文本
     */
    public String prettyPrint() {
        StringBuilder builder = new StringBuilder();
        for (TraceStep step : steps) {
            String time = LocalTime.ofInstant(Instant.ofEpochMilli(step.timestamp()),
                    ZoneId.systemDefault()).format(TIME_FORMATTER);
            builder.append(step.index()).append(". ")
                    .append(time)
                    .append(" [").append(step.phase()).append("] ")
                    .append(step.content());
            if (Objects.nonNull(step.durationMillis())) {
                builder.append(" (").append(step.durationMillis()).append("ms)");
            }
            builder.append('\n');
        }
        return StringUtilExt.defaultString(builder.toString());
    }
}
