#!/usr/bin/env bash
# 智能简历评估与岗位推荐系统 —— 一键启动脚本
# 用法 (Git Bash):  bash start.sh
# 说明:
#   后端必须用 JDK 25 启动(系统默认 JDK1.8 跑不了 Spring Boot 3.2)
#   前端用 WorkBuddy 自带 node22, 不要直接用系统 PATH 里的 node
#   数据库是文件型 H2, 数据持久化在 resume-system/data/resume-demo.mv.db, 切勿删除

BASE="/c/Users/潘航天/WorkBuddy/2026-07-13-09-42-37"
JAVA="/c/Program Files/Java/jdk-25/bin/java"
NODE="/c/Users/潘航天/.workbuddy/binaries/node/versions/22.22.2/node.exe"

echo ">>> 启动后端 (JDK25 -> :8080) ..."
cd "$BASE/resume-system" || exit 1
nohup "$JAVA" -jar target/resume-system-1.0.0.jar --spring.profiles.active=h2 --server.port=8080 > /tmp/resume-backend.log 2>&1 &

echo ">>> 启动外挂岗位服务 (node -> :3001) ..."
cd "$BASE/job-service" || exit 1
nohup "$NODE" server.js > /tmp/job-service.log 2>&1 &

echo ">>> 启动前端 (vite -> :5173) ..."
cd "$BASE/resume-web" || exit 1
nohup "$NODE" node_modules/vite/bin/vite.js --host > /tmp/resume-frontend.log 2>&1 &

echo ">>> 完成。稍候访问  http://localhost:5173/"
echo ">>> 后端日志: /tmp/resume-backend.log"
echo ">>> 外源服务日志: /tmp/job-service.log"
echo ">>> 前端日志: /tmp/resume-frontend.log"
