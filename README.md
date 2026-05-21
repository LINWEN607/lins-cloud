<p align="center">
  <img src="./demo/logo.png">
</p>

# LINS 监控系统

LINS 采用 **Server-Agent** 微服务架构（SpringBoot），每个监控指标通过 HTTP JSON 由 Agent 上报至 Server，具备全自动发现、零模板脚本、快速部署的特点。

---

## 功能总览

| 类别 | 功能 |
|---|---|
| **主机监控** | CPU 使用率 & 温度、内存使用率、磁盘容量 & IO、硬盘 SMART 健康状态、系统负载、连接数、网卡流量、操作系统/硬件信息 |
| **进程监控** | 监测服务器上指定进程是否存在、CPU/内存占用 |
| **端口监控** | 监测 TCP 端口连通性 |
| **日志监控** | 实时匹配 SSH 登录/登出/失败日志，提取用户名与来源 IP，5 分钟去重，推送告警 |
| **文件防篡改** | 监测关键文件变更（通过磁盘状态上报） |
| **容器监控** | 自动发现并监控 Docker 容器状态 |
| **数据库监控** | 连接检查 + 数据表行数趋势监控 |
| **服务接口监控** | 监测 HTTP API 接口可用性 |
| **数通设备监控** | 监测交换机、路由器、打印机等 SNMP 设备 |
| **告警推送** | 邮件、钉钉、飞书、微信等（支持 CPU/内存/主机下线/进程下线/容器下线/日志匹配告警） |
| **大屏可视化** | 数据看板、趋势图表、统计分析 |
| **网络拓扑** | 自动生成网络拓扑图 |
| **Web SSH** | 浏览器端堡垒机（基于 ganymed-ssh2） |
| **批量指令** | 批量下发 shell/cmd 指令 |

---

## 架构说明

```
┌────────────────────────────────────────────────────┐
│                    LINS Server                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────┐  │
│  │Dashboard  │  │ 告警模块  │  │  Agent HTTP API   │  │
│  │Controller │  │WarnMail  │  │  (接收上报数据)   │  │
│  └──────────┘  └──────────┘  └────────┬─────────┘  │
│                                       │              │
│  ┌────────────────────────────────────┴──────────┐  │
│  │           BatchData 缓冲队列                     │  │
│  │  CPU/MEM/Disk/Net/SysLoad/App/Log/Container   │  │
│  └────────────────────────────────────┬──────────┘  │
│                                       │              │
│  ┌────────────────────────────────────┴──────────┐  │
│  │            Service / Mapper / DB               │  │
│  └───────────────────────────────────────────────┘  │
└──────────────────────┬─────────────────────────────┘
                       │ HTTP POST /agent/minTask
                       │ (每2分钟, JSON)
┌──────────────────────┴─────────────────────────────┐
│                 LINS Agent (N 台主机)               │
│  ┌──────────────────────────────────────────────┐  │
│  │  ScheduledTask (OSHI 采集)                    │  │
│  │  CPU / MEM / Disk / Net / Load / Process/Log │  │
│  └──────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────┘
```

### 通信协议

- Agent 每 **120 秒**（可配置）通过 **HTTP POST** 向 Server 的 `/agent/minTask` 上报 JSON
- Token（`wgToken`）校验身份
- Server 异步落库 + 实时告警判定
- 日志匹配服务端有 **5 分钟去重缓存**（SHA-1 指纹）

---

## 技术栈

| 组件 | 技术 |
|---|---|
| 后端框架 | Spring Boot 2.6.6 |
| 前端 | Thymeleaf + Bootstrap + AdminLTE + ECharts |
| 数据库 | MySQL 5.7+ / MariaDB / PostgreSQL |
| ORM | MyBatis + PageHelper |
| 主机信息采集 | OSHI（替代旧版 SIGAR） |
| SSH | Ganymed SSH-2 |
| 构建 | Maven |
| 容器化 | Docker + Docker Compose |
| JDK | 1.8 |

---

## 快速开始（Docker）

### 1. 构建 Server 镜像

```bash
cd lins-server
# 使用阿里云 Maven 镜像加速（settings.xml 已配置）
docker build -t lins-server:latest .
```

### 2. 启动 Server + MySQL

```bash
cd lins-server
# 确保宿主机 3306 端口未被占用
# 将 sql/cloud.sql 复制到 /data/lins/server/lins.sql
docker compose up -d
```

访问 `http://<host>:9999/lins`，默认账号密码 `admin / 111111`。

### 3. 构建 Agent 镜像

```bash
cd lins-agent
docker build -t lins-agent:latest .
```

### 4. 部署 Agent（每台监控主机）

修改 `application.yml`：

```yaml
base:
  serverUrl: http://<server-ip>:9999
  bindIp: <本机真实IP, 勿用127.0.0.1>
  wgToken: lins  # 与 server 一致
```

启动 Agent：

```bash
docker run -d --name lins-agent \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -p 9998:9998 \
  lins-agent:latest
```

> Agent 容器内需要访问宿主机 Docker（`docker.sock` 挂载）以采集容器状态。

---

## 配置文件说明

### Server `application.yml`

| 配置项 | 说明 | 默认值 |
|---|---|---|
| `server.port` | Server HTTP 端口 | 9999 |
| `server.servlet.context-path` | 上下文路径 | /lins |
| `base.admindPwd` | admin 登录密码 | 111111 |
| `base.wgToken` | 通信 Token，Agent 端需一致 | lins |
| `base.dbTableTimes` | 数据表监控间隔 | 3600000 (1h) |
| `base.heathTimes` | 服务接口监控间隔 | 600000 (10min) |
| `mail.allWarnMail` | 告警总开关 | yes |
| `mail.memWarnVal` | 内存告警阈值 | 98% |
| `mail.cpuWarnVal` | CPU 告警阈值 | 98% |

### Agent `application.yml`

| 配置项 | 说明 | 默认值 |
|---|---|---|
| `server.port` | Agent HTTP 端口 | 9998 |
| `server.servlet.context-path` | 上下文路径 | /lins-agent |
| `base.serverUrl` | Server 地址 | http://localhost:9999 |
| `base.bindIp` | 本机真实 IP | 192.168.1.2 |
| `base.wgToken` | 通信 Token | lins |

---

## 核心采集指标

Agent 每次上报的 JSON 结构包含以下对象和数组：

| 字段 | 类型 | 说明 |
|---|---|---|
| `cpuState` | Object | CPU 使用率、温度、频率 |
| `memState` | Object | 内存总量/已用/可用/使用率 |
| `sysLoadState` | Object | 系统负载 1/5/15 分钟 |
| `netIoState` | Object | 网卡收发字节/包量 |
| `systemInfo` | Object | 主机名、IP、OS、CPU 型号、磁盘分区 |
| `deskStateList` | Array | 磁盘使用率（每个分区） |
| `appInfoList` | Array | 进程列表及资源占用 |
| `appStateList` | Array | 进程存活状态 |
| `containerStateList` | Array | Docker 容器状态 |
| `logMonitorMatch` | Array | 日志匹配结果（SSH 事件） |
| `logInfo` | Object | Agent 端错误日志 |

---

## 告警规则

Server 端在接收数据时进行实时判定，触发以下告警时立即推送：

| 告警类型 | 判定条件 | 推送方式 |
|---|---|---|
| CPU 告警 | CPU 使用率 > `cpuWarnVal` | async 线程池 |
| 内存告警 | 内存使用率 > `memWarnVal` | async 线程池 |
| 主机下线 | 超过 5 分钟未收到上报 | 定时扫描 |
| 进程下线 | 进程不在运行状态 | 对比 appInfo |
| 容器下线 | 容器状态异常 | 对比 containerState |
| 日志匹配 | SSH 登录/登出/失败日志 | 5min 去重 |

---

## 项目结构

```
lins/
├── lins-server/           # 服务端
│   ├── src/main/java/com/lins/
│   │   ├── controller/    # 控制器(18个)
│   │   ├── service/       # 业务逻辑(24个)
│   │   ├── mapper/        # MyBatis DAO
│   │   ├── entity/        # 数据模型
│   │   ├── dto/           # 数据传输对象
│   │   ├── task/          # 定时任务
│   │   ├── filter/        # 过滤器
│   │   └── common/        # 公共组件
│   ├── src/main/resources/
│   │   ├── mybatis/mapper/ # SQL XML
│   │   ├── static/         # 前端静态资源
│   │   └── application.yml
│   ├── Dockerfile
│   └── docker-compose.yml
├── lins-agent/            # Agent端
│   ├── src/main/java/com/lins/
│   │   ├── entity/        # 数据模型(与server共享)
│   │   ├── ScheduledTask.java  # 核心采集(OSHI + 日志)
│   │   └── RestUtil.java       # HTTP上报
│   ├── src/main/resources/application.yml
│   └── Dockerfile
├── sql/                   # 数据库初始化脚本
├── bin/                   # 启动/停止脚本
└── demo/                  # 功能截图
```
