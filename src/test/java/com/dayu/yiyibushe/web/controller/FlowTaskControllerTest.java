package com.dayu.yiyibushe.web.controller;

import com.dayu.yiyibushe.app.task.SubmitCallbackNode;
import com.dayu.yiyibushe.common.util.JsonUtilExt;
import com.dayu.yiyibushe.infra.auth.LoginPrincipal;
import com.dayu.yiyibushe.infra.auth.SessionManager;
import com.dayu.yiyibushe.web.interceptor.LoginInterceptor;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 说明：FlowTaskController 的 MockMvc 集成测试，覆盖手动 HTTP 调用的四条路径：
 * create 创建、trigger 触发、receipt 回调、detail 详情，以及两个参数异常分支。
 * <p>
 * 业务接口受登录拦截器保护，测试在 setup 中通过 SessionManager 建立会话，
 * 每个请求携带会话 Cookie。任务落 MySQL 双表（flow_task + task_node），
 * 前后均清表不污染真实数据；任务 ID 由 sequence 号段生成，因此从 create 响应动态取回。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.3
 */
@AutoConfigureMockMvc
@SpringBootTest
class FlowTaskControllerTest {

    /** 不存在的任务 ID（异常分支用） */
    private static final long MISSING_TASK_ID = 999999L;

    /** 异步状态等待时长（毫秒） */
    private static final long CALLBACK_WAIT_MILLIS = 3000L;

    /** 轮询等待步长（毫秒） */
    private static final long AWAIT_SLEEP_MILLIS = 50L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SessionManager sessionManager;

    /** 测试会话 Cookie（每个用例新建） */
    private Cookie sessionCookie;

    /**
     * 每个用例清空 flow_task 与 task_node 双表并建立登录会话，保证状态干净
     */
    @BeforeEach
    void prepareSessionAndCleanTables() {
        jdbcTemplate.update("DELETE FROM task_node");
        jdbcTemplate.update("DELETE FROM flow_task");
        String sessionId = sessionManager.create(new LoginPrincipal(1L, "tester", "测试用户"));
        sessionCookie = new Cookie(LoginInterceptor.SESSION_COOKIE, sessionId);
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
     * 给请求挂上会话 Cookie
     *
     * @param requestBuilder
     *     请求构造器
     * @return 挂上 Cookie 后的请求构造器
     */
    private MockHttpServletRequestBuilder withSession(MockHttpServletRequestBuilder requestBuilder) {
        return requestBuilder.cookie(sessionCookie);
    }

    /**
     * 创建到点任务并从响应中取回任务 ID（sequence 号段生成，不可假定为 1）
     *
     * @return 新任务 ID
     * @throws Exception
     *     MockMvc 调用异常
     */
    private long createTaskAndGetId() throws Exception {
        String createBody = "{\"taskType\":\"" + SubmitCallbackNode.TASK_TYPE
                + "\",\"gmtFireMillis\":" + System.currentTimeMillis() + "}";
        MvcResult createResult = mockMvc.perform(withSession(post("/api/flow-tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        Object taskIdObject = JsonUtilExt.parseObject(
                createResult.getResponse().getContentAsString()).get("data");
        assertNotNull(taskIdObject, "create 响应应返回任务 ID");
        return ((Number) taskIdObject).longValue();
    }

    /**
     * 创建未来任务并查询详情：返回任务 ID，详情中任务类型与 INIT 状态正确
     *
     * @throws Exception
     *     MockMvc 调用异常
     */
    @Test
    void createAndDetail_shouldPersistWaitingTask() throws Exception {
        long futureFireMillis = System.currentTimeMillis() + 3_600_000L;
        String createBody = "{\"taskType\":\"" + SubmitCallbackNode.TASK_TYPE
                + "\",\"gmtFireMillis\":" + futureFireMillis + "}";

        MvcResult createResult = mockMvc.perform(withSession(post("/api/flow-tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        Object taskIdObject = JsonUtilExt.parseObject(
                createResult.getResponse().getContentAsString()).get("data");
        assertNotNull(taskIdObject, "create 响应应返回任务 ID");
        long taskId = ((Number) taskIdObject).longValue();

        // detail 查询任务详情
        mockMvc.perform(withSession(post("/api/flow-tasks/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskId\":" + taskId + "}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.taskContext.taskType").value(SubmitCallbackNode.TASK_TYPE))
                .andExpect(jsonPath("$.data.status").value("INIT"))
                .andExpect(jsonPath("$.data.priority").value(0));
    }

    /**
     * trigger 触发到点任务：INIT -> RUNNING，首节点进入待回调
     *
     * @throws Exception
     *     MockMvc 调用异常
     */
    @Test
    void trigger_shouldStartTask() throws Exception {
        long taskId = createTaskAndGetId();

        mockMvc.perform(withSession(post("/api/flow-tasks/trigger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskId\":" + taskId + "}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(withSession(post("/api/flow-tasks/detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskId\":" + taskId + "}")))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.nodeRecords[0].status").value("WAIT"));
    }

    /**
     * callback 手动唤醒首节点：节点成功后推进到 load2，load2 首跑失败，
     * 任务回到 INIT 等下次触发
     *
     * @throws Exception
     *     MockMvc 调用异常
     */
    @Test
    void callback_shouldAdvanceNodeToRetryWaiting() throws Exception {
        long taskId = createTaskAndGetId();
        mockMvc.perform(withSession(post("/api/flow-tasks/trigger")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskId\":" + taskId + "}")));

        // 回调只带任务 ID：引擎按 currentNodeType 定位待回调节点
        String callbackBody = "{\"taskId\":" + taskId + "}";
        mockMvc.perform(withSession(post("/api/flow-tasks/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(callbackBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // MQ 消费为异步（手动 callback 走引擎同步，此处等待引擎落库完成），等待任务回到 INIT
        long deadline = System.currentTimeMillis() + CALLBACK_WAIT_MILLIS;
        String taskStatus = "";
        while (System.currentTimeMillis() < deadline) {
            MvcResult detailResult = mockMvc.perform(withSession(post("/api/flow-tasks/detail")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"taskId\":" + taskId + "}")))
                    .andReturn();
            taskStatus = detailResult.getResponse().getContentAsString();
            if (taskStatus.contains("\"status\":\"INIT\"")) {
                break;
            }
            Thread.sleep(AWAIT_SLEEP_MILLIS);
        }
        assertTrue(taskStatus.contains("\"status\":\"INIT\""), "load2 首跑失败任务应回到 INIT");
        assertTrue(taskStatus.contains("\"retryCount\":1"), "失败后重试次数应为 1");
    }

    /**
     * trigger 不存在的任务：返回失败与 TASK_NOT_FOUND 错误码 3015
     *
     * @throws Exception
     *     MockMvc 调用异常
     */
    @Test
    void triggerMissingTask_shouldReturnNotFoundError() throws Exception {
        mockMvc.perform(withSession(post("/api/flow-tasks/trigger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"taskId\":" + MISSING_TASK_ID + "}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errCode").value("3015"));
    }
}
