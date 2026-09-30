# 衣衣不舍 · 项目结构与分层导览（AI 速查）

> 本文件回答两个问题：**代码在哪里、各部分如何协作**。
> 编码"怎么写"见同目录 [project_rules.md](./project_rules.md)；两文件配合使用。
> 本文依据当前真实代码结构编写，结构演进后须同步更新。

---

## 一、项目概览

- **定位**：正经业务系统——AI 能力平台 + 虚拟试衣（衣衣不舍）。
- **根包**：`com.dayu.yiyibushe`。
- **技术栈**：JDK 21、Spring Boot 4.0.8、MyBatis（XML）、MySQL、Flyway、Reactor、AgentScope 2.0.1、RocketMQ 官方客户端 4.9.8（本地 Broker）、Log4j2。
- **启动入口**：[YiyibusheApplication.java](../../src/main/java/com/dayu/yiyibushe/YiyibusheApplication.java)。启动由用户在 IDE 点 Debug，AI 禁止自行拉起应用。

---

## 二、分层总览与依赖方向（核心铁律）

项目采用 DDD 风格分层，源码根包下六大顶层包：

```
com.dayu.yiyibushe
├── web      接入层：HTTP 入口、登录拦截
├── app      应用层：业务编排（具体 Agent / Skill / Tool / Service / 任务节点）
├── domain   领域层：纯领域模型与接口（"是什么"，无技术细节）
├── infra    基础设施层：技术实现（AI 平台、流程引擎、消息、存储、会话、配置）
├── dao      数据访问层：数据库读写（四件套）
└── common   通用层：跨层工具、统一返回、异常体系（无 Spring 业务）
```

**依赖方向（单向，禁止反向与跨层混乱）：**

```
web  ──→  app  ──→  domain
 │          │        ↑
 └──────────┼──→ infra（infra 实现 domain 接口，依赖倒置）
            └──→ dao  ──→ domain（PO/Definition）
common 被以上所有层依赖，自身不依赖任何业务层
```

- `domain` 是核心：只放领域概念与接口，**不依赖** Spring/AgentScope/DB 等任何技术实现。
- `infra` 依赖并实现 `domain` 定义的接口（如 `BaseAiAgent implements domain 的 AiAgent`）——依赖倒置。
- `app` 面向用例编排，组合 `infra`、`dao`、`domain` 完成业务。
- `web` 只做协议转换与登录态，不写业务逻辑。
- 判不清归属时，优先让领域概念下沉 `domain`、技术细节上浮 `infra`、用例流程放 `app`。

---

## 三、各层详解

### 1. `common` —— 通用层（无状态、无业务）

被所有层共享的基础设施型代码，**不放 Spring 业务组件**。

| 路径 | 职责 |
|---|---|
| `common/ApiResult.java`、`BaseResult.java` | 统一返回结构（成功/失败 + 错误码） |
| `common/BaseRequest.java` | 分页等基础请求参数（pageIndex/pageSize） |
| `common/ExecuteTemplate.java` | 执行模板（重试/容错封装，纯静态） |
| `common/constant/TaskStatus.java` | 任务状态常量 |
| `common/exception/` | 异常体系：`BizException`（唯一业务异常）、`ErrorCode` 接口、`GlobalExceptionHandler`（全局兜底）、三段错误码枚举 `SystemErrorCode`(1xxx)/`ParamErrorCode`(2xxx)/`BizErrorCode`(3xxx) |
| `common/id/IdUtil.java` | 18 位分段业务 ID 生成器 |
| `common/util/` | 自建工具，类名以 `UtilExt` 结尾：`StringUtilExt`、`CollectionUtilExt`、`JsonUtilExt`、`LogUtilExt`、`PasswordUtilExt`。业务代码统一走此封装，不直接调原生/第三方 API |

### 2. `domain` —— 领域层（最稳定，零技术依赖）

描述业务"是什么"的纯模型与接口，可被任意层引用，自身不引入框架。

| 路径 | 职责 |
|---|---|
| `domain/ai/agent/AiAgent.java` | 智能体统一接口（执行单元契约），技术基座在 infra、业务实现在 app |
| `domain/ai/execution/ExecutionContext.java`、`ExecutionResult.java` | Agent 执行的共享上下文与结果 |
| `domain/ai/skill/AiSkill.java` | 技能领域接口 |
| `domain/asset/AssetCategory.java`、`AssetItem.java` | 素材分类与素材项 |
| `domain/model/UserModel.java` | 用户模型（人体/模特信息） |
| `domain/tryon/OutfitGenerateRequest.java`、`TryOnTaskResult.java` | 试衣生成请求与任务结果 |

### 3. `infra` —— 基础设施层（技术实现）

把领域接口落地为具体技术，承载所有"与外部世界打交道"的能力。

#### 3.1 `infra/ai` —— AI 能力平台（本项目核心）

| 子包 | 职责 |
|---|---|
| `agent/BaseAiAgent.java` | Agent 抽象基座：基于 AgentScope `ReActAgent` 封装"取模型连接→建 Agent→对话"，字段注入，子类只赋 name/modelCode |
| `connection/` | 模型连接：`ModelConnectionPool`（按 modelCode 缓存复用）、`AsyncDashScopeClient`（DashScope 异步客户端，静态工厂 create）、`AgentScopeModelFactory` |
| `core/` | AI 原语：`AiMessage`（静态工厂 of）、`AiRequest`、`AiResponse`、`ModelType` |
| `credential/` | 密钥管理：`CredentialManager`、`CredentialStore` 接口与唯一实现 `DbCredentialStore`（只从 `ai_credential` 读）、`ApiCredential`（静态工厂 create）、`LocalCredentialMigrationSource`（首次启动一次性迁移源） |
| `registry/` | `ModelRegistry`（从 `ai_model` 加载模型定义）、`ProviderInfo`（厂商与通信协议单一事实源）、ModelDefinition |
| `prompt/PromptStore.java` | 提示词统一读取入口：启动载入内存，按 `version` 定时增量拉取，改库即热生效 |
| `definition/` | 领域定义对象：Function/Plugin/Prompt/Skill/Tool Definition（PO 与领域之间的中间模型） |
| `seed/AiAssetDataSeeder.java` | 资产种子装载（幂等）：模型/凭证/函数/工具/技能/插件/提示词的首次基线，唯一允许硬编码文案处 |
| `service/AiPlatformService.java` | AI 平台对外门面：模型初始化/切换、密钥装配、能力调度 |
| `skill/` | `SkillDocument`、`SkillDocumentLoader`（技能正文懒加载、目录拼接） |
| `tool/` | `AiSkillToolAdapter`（技能适配为工具）、`SkillToolkitConfig`（工具箱装配） |
| `trace/` | 调用链追踪：`ExecutionTrace`、`TraceStep`、`TracePhase`、`AgentToolTraceSupport` |
| `state/AgentStateStoreConfig.java` | Agent 状态存储配置 |

#### 3.2 其余 infra 子系统

| 路径 | 职责 |
|---|---|
| `infra/auth/` | 登录主体 `LoginPrincipal`、会话管理 `SessionManager`（超时/清理/服务端会话） |
| `infra/config/` | Spring 配置：`WebConfig`（拦截器/CORS）、`RestClientConfig`（共享 8 秒超时 HTTP 客户端）、`OssConfig`/`OssProperties`、`LocalResourceConfig` |
| `infra/flowtask/` | 流程任务引擎框架：`TaskEngine`（驱动执行）、`TaskStore`（持久化）、`FlowTask`/`FlowTaskTemplate`、`TaskNode`/`TaskNodeAction`/`TaskNodeResult`/`TaskNodeStrategy`、状态枚举、`FlowTaskScheduleConfig`（调度）、回调消息与监听；`retry/` 重试策略体系（`RetryStrategy` 接口 + 三种策略 + `RetryStrategyRegistry`） |
| `infra/mq/` | 本地 RocketMQ：`LocalRocketMqBroker`（内嵌 Broker）、`RocketMqTemplate`（发送）、`RocketMqListener`/`RocketMqMessageListener`（消费） |
| `infra/storage/` | 文件存储抽象 `StorageService` + 双实现：`LocalFileStorageService`（本地盘）、`AliyunOssStorageService`（阿里云 OSS） |
| `infra/resource/` | 本地资源管理 `LocalResourceManager` + `LocalResourceProperties` |

### 4. `dao` —— 数据访问层

每张主表采用**四件套**，SQL 全部写在 XML，接口上零 SQL 注解。

| 子包 | 角色 |
|---|---|
| `dao/mapper/XxxMapper.java` | 领域接口（业务语义） |
| `dao/impl/MysqlXxxMapper.java` | 实现：PO ↔ Definition 转换、调用 MyBatis |
| `dao/mybatis/XxxMybatisMapper.java` | `@Mapper` 接口，绑定 XML |
| `resources/mapper/XxxMybatisMapper.xml` | 全部 SQL（位于 resources，见下节） |

- `dao/po/`：持久化对象，与表一一对应；JSON 列在 PO 层一律以 `String` 承载。
- 现有四件套覆盖：User、Model、Function、Tool、Skill、Plugin、PluginInstall、TargetMount、Prompt。
- `dao/mybatis/` 内另含跨表/通用件：`AssetMybatisMapper`+`AssetPO`、`TaskNodeMybatisMapper`+`TaskNodePO`、`FlowTaskMybatisMapper`、`CredentialMybatisMapper`、`SequenceMybatisMapper`、`JsonMapTypeHandler`（JSON↔Map 类型处理器）。
- `dao/sequence/`：`SequenceDao` + MyBatis 实现（序号分配）。
- 从属/明细通过主表 XML 的 `<collection>` 映射，不单独建四件套。

### 5. `app` —— 应用层（用例编排，业务的"主战场"）

面向具体业务用例，组合下层能力。这里的类多为 `@Component`/`@Service`。

| 子包 | 职责 |
|---|---|
| `app/ai/agent/` | 4 个业务智能体：`AssistantChatAgent`（中文助手/技能路由）、`PlanAgent`（规划）、`ExecuteAgent`（执行）、`ReviewAgent`（评审）。均继承 `BaseAiAgent`，提示词全部走 PromptStore |
| `app/ai/skill/OutfitSkill.java` | 穿搭技能（领域 AiSkill 的业务实现） |
| `app/ai/tool/` | 4 个业务工具：`GetCurrentLocationTool`、`GetWeatherTool`、`GetTechNewsTool`、`LoadSkillInstructionsTool`（技能正文加载）。描述/模板全部入库 |
| `app/service/` | 5 个业务服务（接口 + impl）：`AuthService`、`UserService`、`OutfitService`、`PersonalAssetService`、`ShareService` |
| `app/task/` | 流程任务的**具体节点与编排**：`LoadDemoTaskNodeStrategy`（@PostConstruct 编排节点链）、节点 `SubmitCallbackNode`、`FailOnceNode`、`PassThroughNode`。节点框架在 infra，节点实现在 app |

### 6. `web` —— 接入层

| 子包 | 职责 |
|---|---|
| `web/controller/` | 7 个 HTTP 入口：`AuthController`、`UserController`、`ChatController`、`OutfitController`、`AssetController`、`FlowTaskController`、`PromptController`（提示词管理） |
| `web/dto/` | 请求/响应对象：`XxxRequest`、`XxxDTO`（登录、注册、改密、会话、流程任务、提示词增改等） |
| `web/vo/CurrentUserVO.java` | 视图对象（当前登录用户） |
| `web/interceptor/` | `LoginInterceptor`（登录态拦截）、`LoginUtil`（登录态读取），业务层不自行解析凭证 |

---

## 四、`resources` 资源目录（src/main/resources）

| 路径 | 职责 |
|---|---|
| `application.properties` | 应用配置（DB、服务端口、各模块参数/默认值） |
| `log4j2-spring.xml` | Log4j2 配置（含 Disruptor 异步日志） |
| `db/migration/` | **Flyway 迁移脚本**：命名 `VyyyyMMdd_HHmm__描述.sql`，启动自动执行；**已应用脚本禁止修改**，结构演进一律新增脚本 |
| `mapper/` | **MyBatis XML**：与四件套对应，全部 SQL 在此 |
| `skills/current-weather/SKILL.md`、`skills/daily-tech-news/SKILL.md` | 两个本地技能正文，**原样保留，不得删除或改动** |
| `static/` | 前端静态页：`index.html` + `auth/`（登录/注册/重置密码）、`wardrobe/`（衣橱）、`common/`（通用样式与 http 封装） |

---

## 五、测试结构（src/test/java）

测试包路径与主代码对应：

| 路径 | 覆盖 |
|---|---|
| `common/ExecuteTemplateTest.java` | 执行模板 |
| `infra/flowtask/FlowTaskFrameworkTest.java`、`RetryStrategyTest.java` | 流程引擎与重试策略（配置用 ReflectionTestUtils 注入） |
| `skill/weather/WeatherSkillTest.java`、`skill/news/NewsSkillTest.java` | 两个技能（期望值从 PromptStore 取） |
| `web/controller/FlowTaskControllerTest.java` | 流程任务控制器 |

---

## 六、典型端到端调用链（建立全局感）

1. **登录认证**：`AuthController → AuthService/UserService(app) → UserMapper(dao) → users 表`，会话由 `infra/auth/SessionManager` 管理，全程经 `LoginInterceptor` 校验。
2. **AI 对话**：`ChatController → AssistantChatAgent(app) → BaseAiAgent(infra) → ModelConnectionPool → ModelRegistry(ai_model) + DbCredentialStore(ai_credential)`，提示词与技能目录由 `PromptStore(ai_prompt)` 提供，工具经 AgentScope function calling 触发。
3. **规划-执行-评审**：`PlanAgent → ExecuteAgent → ReviewAgent`（app 层三智能体）共享 `ExecutionContext`，各自系统提示/用户模板均来自 PromptStore。
4. **流程任务**：`FlowTaskController → FlowTaskTemplate/TaskEngine(infra) → TaskNodeStrategy → app/task 节点执行 → TaskStore(dao) 落库`，失败按 `retry/` 策略重试、断点续跑，调度由 `FlowTaskScheduleConfig` 驱动。
5. **虚拟试衣**：`OutfitController → OutfitService(app) → AiPlatformService(infra) 调 DashScope 通义万相`，图片经 `StorageService`（本地或 OSS）存取。
6. **提示词热更新**：`PromptController → PromptMapper 更新 ai_prompt 并 version+1 → PromptStore 定时增量拉取`，改库秒级生效、不重启进程。
7. **消息链路**：业务侧 `RocketMqTemplate` 以官方 `Message`（JSON body）发到 `LocalRocketMqBroker`，`RocketMqListener` 泛型反序列化本地消费。

---

## 七、AI 新增/修改功能时的落位指引

| 要做的事 | 放到哪里 |
|---|---|
| 新增一个 HTTP 接口 | `web/controller` + 请求/响应放 `web/dto` 或 `web/vo` |
| 新增一段业务用例/编排 | `app/service`（接口+impl）或 `app/ai`、`app/task` |
| 新增一个业务 Agent | `app/ai/agent`，继承 `infra/ai/agent/BaseAiAgent` |
| 新增一个工具/技能 | 工具 `app/ai/tool`、技能 `app/ai/skill`；描述与文案入 `ai_prompt` |
| 新增一个流程节点 | `app/task` 写节点，在 `@PostConstruct` 编排；框架能力在 `infra/flowtask` |
| 新增领域概念/对外契约 | `domain`（纯模型/接口，不引技术实现） |
| 新增外部技术对接 | `infra` 对应子系统，实现 domain 接口 |
| 新增一张表/字段 | 新增 Flyway 脚本（不改旧脚本）+ `dao` 四件套 + `dao/po` + XML |
| 新增任何提示词/固定话术 | 入 `ai_prompt`（种子由 AiAssetDataSeeder 维护），代码只经 PromptStore 读取 |
| 通用可复用工具 | `common/util`，以 `UtilExt` 结尾，纯静态无状态 |

**先定位层、再动手；不跨层写逻辑、不让依赖反向。**
