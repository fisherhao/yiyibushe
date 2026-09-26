#!/bin/bash
# 一键关闭后台启动的 yiyibushe 应用（释放 8080 端口）
# 安全策略：只杀「java 直接运行 YiyibusheApplication」的进程；
# 带 -agentlib:jdwp 的进程是 IDEA Debug 启动的，绝不触碰。

PID_FILE="/tmp/yiyibushe-app.pid"
KILLED=0

# 1. 优先按 PID 文件关闭（后台脚本启动的场景）
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if kill -0 "$PID" 2>/dev/null; then
        kill "$PID" && echo "已停止 PID=$PID（来自 PID 文件）" && KILLED=1
    fi
    rm -f "$PID_FILE"
fi

# 2. 兜底：按主类名匹配，排除 jdwp（IDEA Debug）进程
for PID in $(pgrep -f "com.dayu.yiyibushe.YiyibusheApplication" 2>/dev/null); do
    CMD=$(ps -p "$PID" -o command= 2>/dev/null)
    case "$CMD" in
        *jdwp*|*agentlib*)
            echo "跳过 PID=$PID（IDEA Debug 进程，不动它）"
            ;;
        *java*)
            kill "$PID" 2>/dev/null && echo "已停止 PID=$PID" && KILLED=1
            ;;
    esac
done

[ "$KILLED" -eq 0 ] && echo "没有发现运行中的 yiyibushe 后台进程"

# 3. 确认端口释放
sleep 1
if lsof -ti :8080 >/dev/null 2>&1; then
    echo "警告：8080 端口仍被占用（可能是 IDEA 里启动的应用，请在 IDEA 里停止）"
else
    echo "8080 端口已释放"
fi
