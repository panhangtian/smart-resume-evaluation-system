# 智能简历评估系统

> 基于 SpringBoot 3.2 + Vue3 的简历解析、能力评估与智能优化平台

## 技术栈

| 层 | 技术 |
| --- | --- |
| 前端 | Vue3 + Element Plus + Vite + Pinia + Vue Router |
| 后端 | Spring Boot 3.2.5 + MyBatis-Plus + JWT + H2(文件型数据库) |
| 外挂服务 | Node.js / Express 岗位服务(job-service)，动态合成多来源岗位 |
| 运行 | JDK 25(后端运行) · Node 22(前端 / 岗位服务) |

## 功能概览

- **三角色体系**：求职者(JOBSEEKER)、HR、管理员(ADMIN)，JWT 鉴权
- **简历管理**：上传 / 解析简历(JSON)，Word 下载，内容查看与润色
- **能力评估**：针对目标岗位的多维能力评分与历史记录
- **智能优化**：目标岗位选择(后端 + 外源合并)、AI 模型一键优化或规则引擎优化，结果回写简历
- **岗位浏览**：后端种子岗位 + 外源岗位(猎聘 / Boss直聘 / 拉勾 / 智联 / 51job / 牛客)，行业分类筛选、无限滚动、定时刷新；外源岗位引导至对应平台投递
- **HR 工作台**：投递管理(查看 / 处理用户投递)、人才推荐

## 默认账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | admin123 |
| HR | hr | hr123 |
| 求职者 | seeker | seeker123 |
| 求职者(注册示例) | 123 | 123456 |

## 目录结构

```
.
├── resume-web/        前端 (Vue3 + Vite)
├── resume-system/     后端 (SpringBoot)，含已编译 jar
│   └── target/resume-system-1.0.0.jar   可直接运行的成品
├── job-service/       外挂岗位服务 (Node/Express)
└── start.sh           一键启动脚本
```

## 启动方式

> 后端启动**必须用 JDK 25**，并以参数指定端口与 h2 环境：
> `java -jar resume-system/target/resume-system-1.0.0.jar --server.port=8080 --spring.profiles.active=h2`

简化为执行项目根目录 `start.sh`(Git Bash 环境)：`bash start.sh`

各服务端口：

| 服务 | 端口 |
| --- | --- |
| 后端 API | 8080 |
| 前端 | 5173 |
| 外挂岗位服务 | 3001 |

启动后访问前端：http://localhost:5173

## 说明与已知约束

- 数据库为文件型 H2(`resume-system/data/resume-demo.mv.db`)，首次启动由 `DataInitializer` 灌入种子数据；删除该文件即重置。
- 本仓库已附带编译好的 `resume-system-1.0.0.jar`(运行需 JDK 25)。如需重编译需 JDK 17+ 与 Maven。
- 外源岗位 id 为字符串(后端评估 / 投递接口要求数值型)，故其评估与优化在前端本地计算、投递改为引导至对应平台。
- 人才推荐页的联系方式：当前后端推荐结果未返回邮箱 / 电话字段，页面占位显示「暂无联系方式」，待后端扩展接口后填充。
