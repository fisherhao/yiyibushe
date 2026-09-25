# 衣衣不舍 · 工程编码规范

> 本项目是正经业务系统（AI 能力平台 + 虚拟试衣），不是算法练习项目。
> 技术栈：JDK 21、Spring Boot 4.x、MyBatis、MySQL、Flyway、Reactor、RocketMQ 官方客户端。
> 根包：`com.dayu.yiyibushe`。本文件是项目工程定义的唯一权威规范，与历史记忆冲突时以本文件为准。

---

## 一、工作方式（硬约束）

1. **绝不自行启动应用**：禁止 `mvn spring-boot:run`、`java -jar`、JDWP、后台进程等任何拉起应用的方式；启动由用户在 IntelliJ IDEA 点 Debug。可代为执行 `mvn compile`，编译完成后提醒用户启动。仅当用户同一句话明确授权"你来启动"时例外。
2. **禁止打 jar 启动**：不得执行 `mvn package` 以 jar 方式运行，不得使用 target 产物启动。
3. **不得终止 IDEA 进程**：IDEA Debug 启动的进程（含 `-agentlib:jdwp`）是用户有意为之，只允许停止我自己拉起的进程。
4. 不主动创建文档/README，不主动 commit；未经用户明确要求不做计划外改动。
5. 修改配置文件前先按 `settings_日期.xml` 格式备份。
6. 沟通用中文，表达简洁、严谨、官方。

## 二、命名规范

1. 包名全小写，领域包按业务语义划分，学习性包名禁止出现在本项目。
2. 类名大驼峰、有意义的英文名词：`ModelConnectionPool`、`AiPlatformService`、`FunctionPO`。
3. 方法名小驼峰、动词开头：`requireSecret`、`reloadFromDatabase`、`printResult`。
4. 变量名必须表达用途：`taskQueue`、`credentialMap`；禁止 `a`、`b`、`list1`、`map` 等无意义命名。
5. 常量全大写下划线分隔：`MAX_CAPACITY`、`DEFAULT_WORKSPACE_ID`。
6. 编码类（kebab-case）统一短横线风格：模型/工具/技能/插件编码如 `dashscope-wanx-t2i`、`current-weather`。

## 三、注释规范

1. 每个类必须有类头 Javadoc：说明职责，含 `@author Witty·Kid Fisher`、`@version`。
2. **getter/setter 必须逐个带 Javadoc，字段注释不能代替方法注释。** 布尔取值方法同样需要。标准格式：

```java
/**
 * 获取函数编码
 *
 * @return 函数编码
 */
public String getFunctionCode() {
    return functionCode;
}

/**
 * 是否只读
 *
 * @return true 表示只读
 */
public boolean isReadOnly() {
    return readOnly;
}

/**
 * 设置函数编码
 *
 * @param functionCode
 *                     函数编码
 */
public void setFunctionCode(String functionCode) {
    this.functionCode = functionCode;
}
```

3. setter 描述独占一行，对齐到参数名结束位置后空 1 列（与 UserPO 一致）。
4. 业务方法 Javadoc 写清用途、参数含义、返回值；可加学习性注释解释底层原理与设计意图。
5. 注释为中文；禁止保留 "Getter method for property xxx" 之类英文模板注释。

## 四、数据库设计规范

1. 表名用 `ai_` 等语义前缀 + 下划线单数命名：`ai_model`、`ai_credential`、`ai_plugin_install`；禁止 `base_*` 前缀。
2. 资产表统一**双 ID 制**：`id` 为自增物理主键（BIGINT），业务 ID（`model_id`、`function_id`、`tool_id`、`skill_id`、`plugin_id`、`install_id`、`mount_id`）由 `IdUtil` 生成 18 位分段 ID，类型 BIGINT。
3. 时间字段固定为 `gmt_create` / `gmt_modify`，禁止 `gmt_modified` 等变体。
4. 用状态字段表达数据状态，不建 `is_deleted` 字段。
5. 资产的发布态与运行态分离：`publish_status`（DRAFT/PUBLISHED/OFFLINE/DEPRECATED）、`runtime_status`（INIT/ACTIVE/FAILED/UNLOADED）。
6. 插件多版本并存，`(plugin_code, version)` 唯一；安装独立成 `ai_plugin_install` 实例层，支持同一插件在不同范围使用不同凭证。
7. 索引按实际查询建立：全文索引只覆盖展示名、描述等检索字段，禁止把无关字段塞进全文索引。
8. Flyway 脚本命名 `VYYYYMMDD_HHMM__描述.sql`；**已应用的脚本禁止修改**，结构演进一律新增脚本。

## 五、DAO 层规范

1. 每张主表采用**四件套**：
   - `dao/mapper/XxxMapper`：领域接口；
   - `dao/impl/MysqlXxxMapper`：实现，负责 PO ↔ Definition 转换；
   - `dao/mybatis/XxxMybatisMapper`：`@Mapper` 接口；
   - `resources/mapper/XxxMybatisMapper.xml`：SQL 全部写在 XML。
2. **SQL 零注解**：禁止在 Mapper 接口上用注解写 SQL。
3. 从属/明细关系通过主表 XML 的 `<collection>` 映射，不单独建四件套。
4. JSON 列在 PO 层一律以 `String` 承载，由领域层解析；禁止在 PO 上挂复杂对象。
5. 集合转换用 `CollectionUtilExt` 的现成方法（如 `mapToList`），禁止重复造轮子。

## 六、Java 编码规范

1. Spring 依赖统一 **`@Autowired` 字段注入**，禁止构造器注入；`@Autowired` 独占一行，紧贴私有字段正上方、缩进对齐。
2. 代码体内禁止全限定类名，所有引用走 import。
3. 判空：
   - 对象判空：JDK `java.util.Objects`；
   - 字符串：`StringUtilExt.isBlank()` / `isNotBlank()`；
   - 集合：`CollectionUtilExt.isEmpty()` / `isNotEmpty()`；
   - 需要 Stream 时用 `CollectionUtilExt.toStream()`，禁止业务代码无封装直接 `.stream()`。
4. Service 必须接口与实现分离：`XxxService` + `impl.XxxServiceImpl`。
5. `common` 包是纯静态、无状态、无业务的工具模块，不放 Spring 组件。
6. 自建工具类放 `common/util`，类名以 `UtilExt` 结尾：`StringUtilExt`、`CollectionUtilExt`、`JsonUtilExt`、`LogUtilExt`。
7. 同步阻塞代码进入 Reactor 链路时，用 `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())` 或 WebClient；禁止用 `Mono.just()` 包裹阻塞调用。
8. 动态 Schema/参数容器用 `LinkedHashMap`，禁止 `Map.of()`（不可变且不允许 null）。

## 七、异常与错误码

1. 禁止 `throw new RuntimeException(...)`；业务异常统一抛 `BizException`，必须携带错误码。
2. 错误码分段（以现有枚举为准）：
   - 系统类 `1xxx`：`SystemErrorCode`（1001-1004）；
   - 参数类 `2xxx`：`ParamErrorCode`（2001 起）；
   - 业务类 `3xxx`：`BizErrorCode`（3001 起）。
3. 新增错误码归入对应枚举，按段位续号，禁止随意造码。
4. 全局异常由 `GlobalExceptionHandler` 统一捕获，包装为统一返回结构。

## 八、日志规范

1. 日志唯一入口是 `LogUtilExt`：`LogUtilExt.info(log, "模板{0}", args)`，禁止直接调 `logger.info()`。
2. 占位符用编号风格 `{0}`、`{1}`、`{2}`，禁止 `{}`。
3. 打印异常时把 `Throwable` 作为最后一个参数，保留完整堆栈。
4. 业务代码用日志输出，不用 `System.out.println`。

## 九、Web 层规范

1. 统一返回结构：成功用 `ApiResult`/`BaseResult`，分页参数走 `BaseRequest`（pageIndex/pageSize）。
2. 请求/响应对象放 `web/dto`，命名 `XxxRequest`、`XxxResponseDTO`；视图对象放 `web/vo`。
3. 登录态由拦截器与 `LoginUtil` 统一处理，业务层不自行解析凭证。

## 十、AI 能力体系规范

1. **模型与密钥运行期只从数据库加载**：
   - 模型定义读 `ai_model` 表（`ModelRegistry`），密钥读 `ai_credential` 表（`DbCredentialStore`，`CredentialStore` 唯一实现）；
   - 不读环境变量、不读 application properties、不做本地兜底；DB 无密钥时返回 null，由 `CredentialManager` 抛"凭证缺失"；
   - 环境变量/properties 的存量密钥仅由迁移源在首次启动一次性入库，之后数据库是唯一来源；
   - 静态默认模型清单仅作首次种子，不参与运行期。
2. 启动时禁止建立大模型与 MCP 连接；连接在首次发问或平台配置/切换模型时建立。
3. 模型连接由 `ModelConnectionPool` 按模型编码缓存复用；新增/切换经 `initialize` 入池，下线经 `evict` 移除。
4. 能力分层与依赖方向单向向下：Function（执行体）→ Tool（模型门面）→ Skill（SOP）→ Plugin（分发/安装）；下层不知道上层。Function 必须经 Tool 门面，不直接打包进 Plugin。
5. 运行时只处理**已挂载**的 Plugin/Skill/Tool 小集合（单任务挂载数量受控，上限 50）；不直接访问全量资产库；扩展中心检索只在"挂载/添加"动作时使用。
6. 挂载关系是任务/业务级的，非全局永久；挂载表只开放 PLUGIN/SKILL/TOOL，FUNCTION 不开放挂载。
7. 技能正文与工具详情懒加载、渐进披露，不常驻系统提示；技能触发与工具调用顺序由模型决策，禁止硬编码路由。
8. 缓存热更新走 `version` + `gmt_modify` 增量拉取，配合本地 RocketMQ Broker 广播失效；禁止只缓存不失效。
9. 种子装载（`AiAssetDataSeeder`）必须幂等：已有凭证、默认模型、NATIVE 函数、工具、技能、插件与工作空间安装实例重复启动不重复灌入。
10. 本地 `resources/skills/{current-weather,daily-tech-news}/SKILL.md` 原样保留，不得删除或改动。
11. HTTP 客户端收敛为共享 `RestClient` Bean（统一 8 秒超时）；外部 API 调用需有超时与重试。

## 十一、消息规范

1. 消息体系使用官方 RocketMQ 客户端（`rocketmq-client 4.9.8`），本地注册、本地消费，不连远端服务器。
2. 消息载体必须是官方 `org.apache.rocketmq.common.message.Message`，消息体 JSON 序列化。
3. 消费端使用与 `RocketMqListener<T>` 匹配的泛型反序列化。
