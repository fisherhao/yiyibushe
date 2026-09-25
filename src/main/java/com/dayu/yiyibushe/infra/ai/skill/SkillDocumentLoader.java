package com.dayu.yiyibushe.infra.ai.skill;

import com.dayu.yiyibushe.common.util.LogUtilExt;
import com.dayu.yiyibushe.common.util.StringUtilExt;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skill 文档加载器：启动时扫描 classpath 下全部 SKILL.md（skills 目录下每个技能一个子目录），
 * 按 Agent Skills 开放标准（agentskills.io）解析 YAML frontmatter 与正文。
 * <p>
 * 对应"渐进式披露"：
 * <ul>
 *   <li>启动阶段只把 name + description（metadata）加载进内存并可拼入系统提示；</li>
 *   <li>SKILL.md 正文（instructions）只在技能命中时由 {@link #getSkill(String)} 取出注入。</li>
 * </ul>
 * frontmatter 用 Spring Boot 自带 snakeyaml 解析，不新增依赖。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.1
 */
@Component
public class SkillDocumentLoader {

    private static final Logger log = LogUtilExt.getLogger(SkillDocumentLoader.class);

    /** Skill 扫描路径：classpath 下每个 skill 一个目录 */
    private static final String SKILL_LOCATION_PATTERN = "classpath*:skills/*/SKILL.md";

    /** frontmatter 起始标记 */
    private static final String FRONTMATTER_DELIMITER = "---";

    /** 技能文档表：name -> document */
    private final Map<String, SkillDocument> skillDocuments = new ConcurrentHashMap<>();

    /**
     * 启动时扫描并解析全部 SKILL.md（metadata 与正文都解析进内存；
     * 正文只在技能命中、模型调用 load-skill-instructions 时才取出注入，不进启动系统提示）
     */
    @PostConstruct
    public void init() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources(SKILL_LOCATION_PATTERN);
            for (Resource resource : resources) {
                loadOne(resource);
            }
            LogUtilExt.info(log, "[SkillLoader] 已加载 {0} 个 Skill 元数据: {1}",
                    skillDocuments.size(), new ArrayList<>(skillDocuments.keySet()));
        } catch (Exception e) {
            LogUtilExt.error(log, "[SkillLoader] Skill 扫描失败: {0}", e.getMessage(), e);
        }
    }

    /**
     * 解析单个 SKILL.md：frontmatter（snakeyaml）取 name/description，其余为正文
     *
     * @param resource
     *     SKILL.md 资源
     * @throws Exception
     *     读取失败时抛出
     */
    private void loadOne(Resource resource) throws Exception {
        String content;
        try (InputStream inputStream = resource.getInputStream()) {
            content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        if (!content.startsWith(FRONTMATTER_DELIMITER)) {
            LogUtilExt.warn(log, "[SkillLoader] 跳过缺少 frontmatter 的文件: {0}", resource.getURI());
            return;
        }

        // frontmatter：第一个 --- 与第二个 --- 之间
        int closingIndex = content.indexOf('\n' + FRONTMATTER_DELIMITER, FRONTMATTER_DELIMITER.length());
        String frontmatterText = content.substring(FRONTMATTER_DELIMITER.length(), closingIndex).trim();
        String instructions = content.substring(closingIndex + FRONTMATTER_DELIMITER.length() + 1).trim();

        Map<String, Object> metadata = new Yaml().load(frontmatterText);
        String name = Objects.toString(metadata.get("name"), "");
        String description = Objects.toString(metadata.get("description"), "");
        if (StringUtilExt.isBlank(name) || StringUtilExt.isBlank(description)) {
            LogUtilExt.warn(log, "[SkillLoader] 跳过 name/description 缺失的文件: {0}", resource.getURI());
            return;
        }
        skillDocuments.put(name, new SkillDocument(name, description, instructions));
    }

    /**
     * 列出全部已加载 Skill
     *
     * @return Skill 文档列表
     */
    public List<SkillDocument> listSkills() {
        return new ArrayList<>(skillDocuments.values());
    }

    /**
     * 按名称获取 Skill（命中时取正文 instructions）
     *
     * @param name
     *     技能名
     * @return Skill 文档，不存在返回 null
     */
    public SkillDocument getSkill(String name) {
        return skillDocuments.get(name);
    }

    /**
     * 把全部 Skill 的 metadata 拼成系统提示片段（启动预加载，供模型做触发判断）
     *
     * @return metadata 文本，无 Skill 时返回空串
     */
    public String buildMetadataPrompt() {
        if (skillDocuments.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("以下是可用的技能，用户请求与描述匹配时按技能指令执行：\n");
        for (SkillDocument document : skillDocuments.values()) {
            builder.append("- ").append(document.name()).append(": ").append(document.description()).append('\n');
        }
        return builder.toString();
    }
}
