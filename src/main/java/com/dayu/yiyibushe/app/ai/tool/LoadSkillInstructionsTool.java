package com.dayu.yiyibushe.app.ai.tool;

import com.dayu.yiyibushe.common.util.CollectionUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import com.dayu.yiyibushe.infra.ai.constant.AiConstants;
import com.dayu.yiyibushe.infra.ai.prompt.PromptStore;
import com.dayu.yiyibushe.infra.ai.skill.SkillDocument;
import com.dayu.yiyibushe.infra.ai.skill.SkillDocumentLoader;
import com.dayu.yiyibushe.infra.ai.trace.AgentToolTraceSupport;
import com.dayu.yiyibushe.infra.ai.trace.ExecutionTrace;
import com.dayu.yiyibushe.infra.ai.trace.TracePhase;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 技能正文懒加载工具（function calling，非 MCP）。
 * <p>
 * 启动阶段只把 SKILL.md 的 name + description（metadata）放进系统提示供模型路由；
 * 模型判断命中某技能后，先调用本工具按 {@code skillName} 取出该 SKILL.md 正文指令，
 * 再按指令调用对应业务工具。正文全程从内存读取、不打外网，这就是"渐进式披露"。
 * <p>
 * 工具描述、入参描述与缺参提示均来自提示词库，改库即生效。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
@Component
public class LoadSkillInstructionsTool implements AgentTool {

    /** 工具名（模型 function calling 选择用，需唯一） */
    private static final String TOOL_NAME = "load-skill-instructions";

    /** 命中次数：测试用于验证技能正文是否被按需加载 */
    private final AtomicInteger callCount = new AtomicInteger();

    /** 提示词存储：工具描述与错误提示从此读取 */
    @Autowired
    private PromptStore promptStore;

    /** SKILL.md 加载器：正文在启动时已解析进内存，这里直接取 */
    @Autowired
    private SkillDocumentLoader skillDocumentLoader;

    /**
     * 工具名称
     *
     * @return 工具名称
     */
    @Override
    public String getName() {
        return TOOL_NAME;
    }

    /**
     * 工具描述：供模型判断何时调用
     *
     * @return 工具描述
     */
    @Override
    public String getDescription() {
        return promptStore.require(AiConstants.PROMPT_LOAD_SKILL_TOOL_DESC);
    }

    /**
     * 入参 JSON Schema：skillName 必填（参数描述来自提示词库）
     *
     * @return 参数 schema
     */
    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> skillNameProperty = new LinkedHashMap<>();
        skillNameProperty.put("type", "string");
        skillNameProperty.put("description",
                promptStore.require(AiConstants.PROMPT_LOAD_SKILL_ARG_DESC));

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("skillName", skillNameProperty);

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        parameters.put("required", List.of("skillName"));
        return parameters;
    }

    /**
     * 执行懒加载：从内存取出指定技能正文回传模型
     *
     * @param param
     *     调用参数（skillName）
     * @return 技能正文或错误块
     */
    @Override
    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
        callCount.incrementAndGet();
        ExecutionTrace trace = AgentToolTraceSupport.resolve(param);

        Object skillNameValue = param.getInput().get("skillName");
        String skillName = Objects.isNull(skillNameValue) ? null : String.valueOf(skillNameValue);
        if (Objects.nonNull(trace)) {
            trace.hitTool(TOOL_NAME);
            trace.step(TracePhase.TOOL, "模型决策命中工具 {0}，入参：skillName={1}，开始懒加载技能正文",
                    TOOL_NAME, StringUtilExt.defaultIfBlank(skillName, "未传"));
        }

        if (StringUtilExt.isBlank(skillName)) {
            return Mono.just(ToolResultBlock.error(
                    promptStore.require(AiConstants.PROMPT_LOAD_SKILL_MISSING_ARG)));
        }

        SkillDocument skillDocument = skillDocumentLoader.getSkill(StringUtilExt.trim(skillName));
        if (Objects.isNull(skillDocument)) {
            if (Objects.nonNull(trace)) {
                trace.step(TracePhase.TOOL, "技能【{0}】不存在，可用技能：{1}",
                        skillName, CollectionUtilExt.mapToList(skillDocumentLoader.listSkills(),
                                SkillDocument::name));
            }
            return Mono.just(ToolResultBlock.error(
                    promptStore.format(AiConstants.PROMPT_LOAD_SKILL_NOT_FOUND, skillName)));
        }

        if (Objects.nonNull(trace)) {
            // hitSkill 首次命中时记录"路由命中并加载指令"；正文在内存，直接读取不打外网
            trace.hitSkill(skillDocument.name());
            trace.step(TracePhase.SKILL,
                    "技能正文懒加载完成：从内存读取【{0}】SKILL.md 正文 {1} 字符（无外网请求），指令回传模型",
                    skillDocument.name(), StringUtilExt.length(skillDocument.instructions()));
            // 打开闸门：此后该技能的业务工具才允许执行
            trace.markSkillInstructionsLoaded(skillDocument.name());
        }
        return Mono.just(ToolResultBlock.text(skillDocument.instructions()));
    }

    /**
     * 获取本工具被模型命中的次数
     *
     * @return 命中次数
     */
    public int getCallCount() {
        return callCount.get();
    }
}
