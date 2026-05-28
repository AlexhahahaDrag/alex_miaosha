# alex_miaosha 运行状态 Runbook

## 配置和凭据边界

- `.env` 只用于本地开发或本机临时运维，不提交真实生产配置。
- `.env.example` 只保存变量名模板，不保存真实服务器地址、数据库账号或密码。
- Drone 发布使用 Drone secrets：`ssh_ip`、`ssh_root`、`ssh_password`。
- 数据库连接如需进入 CI，必须使用 Drone secrets 注入，不允许从仓库读取明文密码。
- 后续应将 SSH 密码迁移为 `ssh_private_key`。

## 当前重点风险

- `user`：截图中内存约 81%，优先优化 JVM 和容器运行参数。
- `gateway`：入口服务，内存约 71%，发布顺序应放在业务服务之后。
- `oss`：内存约 68%，需要确认上传下载是否流式处理。
- `mysql`：网络和 Block I/O 高，需要开启慢查询并观察索引与连接池。

## Drone 发布前检查

每次发布前必须在 Drone 日志中确认：

- `docker ps` 能看到 `gateway`、`user`、`oss`、`finance`。
- `docker stats --no-stream` 中 `user`、`gateway`、`oss` 内存未持续超过 85%。
- 最近 30 分钟没有 OOM 或异常 restart 事件。

对应 Drone 步骤：`pre-deploy-runtime-snapshot`

该步骤执行：

```bash
date -Is
docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
docker stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.NetIO}}\t{{.BlockIO}}\t{{.PIDs}}'
docker events --since 30m --filter event=oom --filter event=restart || true
```

## 2026-05-26 线上处理记录

已处理：

- 清理 Docker build cache 和 dangling images，根分区从 81% 降到 61%。
- 将 `alex_front`、`monitor`、`gateway`、`finance`、`user`、`oss` 的 restart 策略更新为 `unless-stopped`。
- 将 `user`、`gateway`、`oss` 的容器内存限制从 512m 在线调整为 768m，给 JVM native 内存、线程栈和 Metaspace 留出余量。
- 修复服务器 `/usr/local/soft/alex_miaosha/docker-compose.yml` 中 Redis healthcheck，改为使用实际端口 `6679` 和配置文件中的认证密码。
- 修复服务器 `/usr/local/soft/alex_miaosha/xxljob/docker-compose.yaml` 中 XXL-JOB healthcheck，移除对容器内不存在的 `curl` 的依赖。
- 已重建 `redis` 和 `xxl-job-admin` 容器，当前健康状态为 `healthy`。
- 已修改服务器 MySQL healthcheck 配置为带 root 密码的 `mysqladmin ping`，但未主动重启 MySQL；该配置将在下次维护窗口重建 MySQL 容器后生效。
- 已同步 `.drone.yml` 中 `gateway`、`user`、`oss`、`finance` 的运行参数，避免后续发布覆盖线上修复。

验证结果：

- `user`：约 422MiB / 768MiB，约 55%。
- `gateway`：约 363MiB / 768MiB，约 47%。
- `oss`：约 354MiB / 768MiB，约 46%。
- `finance` 最近 2 分钟无 `xxl-job` 注册错误刷屏。

## 2026-05-26 MySQL 维护窗口处理记录

已处理：

- 修复服务器 `/usr/local/soft/alex_miaosha/docker-compose.yml` 中 MySQL 配置挂载错误：
  - 原配置将目录 `./mysql/conf/my.cnf` 挂载到容器文件路径 `/etc/mysql/my.cnf`。
  - 新配置将文件 `./mysql/conf/my.cnf/my.cnf` 挂载到 `/etc/my.cnf`。
- 新增 MySQL 性能配置：
  - `slow_query_log=ON`
  - `slow_query_log_file=/var/log/mysql/slow.log`
  - `long_query_time=1`
  - `innodb_buffer_pool_size=1G`
  - `max_connections=200`
- 重建 MySQL 容器，当前 `health=healthy`。

验证结果：

- `slow_query_log=ON`
- `long_query_time=1.000000`
- `innodb_buffer_pool_size=1073741824`
- `max_connections=200`
- `Threads_connected=16`
- `gateway`、`user`、`oss`、`finance` 最近 5 分钟无数据库连接错误。

后续观察：

- 观察 `/var/log/mysql/slow.log` 24 小时，收集慢 SQL 后再决定索引或 SQL 优化。
- 暂不继续增大 `innodb_buffer_pool_size`，先观察 MySQL 实际内存曲线和系统 `MemAvailable`。

## 业务服务日志挂载说明

2026-05-26 排查发现：

- 业务容器实际日志配置写入 `/logs/<spring.application.name>`，例如 finance 写入 `/logs/alex-finance-prod`。
- 线上旧容器曾挂载为 `/usr/local/soft/alex_miaosha/drone/alex_miaosha/finance/logs -> /logs/alex-finance`，没有覆盖 `/logs/alex-finance-prod`，所以宿主机看不到主日志。
- `.drone.yml` 已改为统一挂载：`/usr/local/soft/alex_miaosha/drone/alex_miaosha/logs:/logs`。
- 当前统一日志目录：
  - `/usr/local/soft/alex_miaosha/drone/alex_miaosha/logs/alex-finance-prod`
  - `/usr/local/soft/alex_miaosha/drone/alex_miaosha/logs/alex-user-prod`
  - `/usr/local/soft/alex_miaosha/drone/alex_miaosha/logs/alex-oss-prod`
  - `/usr/local/soft/alex_miaosha/drone/alex_miaosha/logs/alex-gateway-prod`

`APP_NAME_IS_UNDEFINED` 说明：

- 该目录来自 `logback-spring.xml` 中 `<contextName>${APP_NAME}</contextName>` 和默认日志路径。
- Spring Boot 启动早期 `spring.application.name` 尚未注入时，`APP_NAME` 为空，logback 会先使用 `APP_NAME_IS_UNDEFINED` 写入极早期日志。
- 这通常不影响业务日志；主日志已经写入 `alex-*-prod` 目录。
- 若要彻底消除，需要修改 logback 默认值或在 JVM 启动参数中提前传入应用名。

生效方式：

- 下次 Drone 发布重建对应服务后自动生效。
- 当前线上 `finance`、`user`、`oss`、`gateway` 已重建，统一日志目录已生效。

## 2026-05-26 业务容器重建与 Drone 发布顺序调整

已处理：

- `.drone.yml` 发布顺序调整为 `finance -> user -> oss -> gateway`，入口网关最后发布。
- `.drone.yml` 构建镜像改为双标签：
  - `alex_miaosha_<service>:${DRONE_BUILD_NUMBER}`
  - `alex_miaosha_<service>:latest`
- `.drone.yml` 的 `docker run` 改为使用构建号镜像启动，便于后续回滚。
- 线上已按 `finance -> user -> oss -> gateway` 顺序重建业务容器，让新的日志挂载立即生效。

验证结果：

- `finance`、`user`、`oss`、`gateway` 均为 `restart=unless-stopped`。
- `gateway` 端口 `30001` 已监听。
- 最近 90 秒四个业务容器无 `ERROR`、`Exception`、`Connection refused`、`UnknownHostException`。
- 宿主机已能看到业务日志：
  - `/usr/local/soft/alex_miaosha/drone/alex_miaosha/logs/alex-finance-prod/alex-finance-prod-info.log`
  - `/usr/local/soft/alex_miaosha/drone/alex_miaosha/logs/alex-user-prod/alex-user-prod-info.log`
  - `/usr/local/soft/alex_miaosha/drone/alex_miaosha/logs/alex-oss-prod/alex-oss-prod-info.log`
  - `/usr/local/soft/alex_miaosha/drone/alex_miaosha/logs/alex-gateway-prod/alex-gateway-prod-info.log`

当前线上容器仍使用 `latest` 镜像标签，因为本次是使用现有镜像手动重建；构建号标签会在下一次 Drone 构建时产生。
