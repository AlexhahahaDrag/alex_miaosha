# alex_miaosha Drone 发布与运行状态优化实施计划

> **给 agentic workers：** 必须使用子技能 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 来逐任务执行本计划。所有步骤使用 checkbox（`- [ ]`）跟踪。

**目标：** 基于真实 `.drone.yml`，优化 alex_miaosha 的 Drone 构建、部署、容器运行参数、发布验证和回滚能力，降低 `user`、`gateway`、`oss` 等服务的内存/OOM 风险，并提升线上发布可靠性。

**架构：** 当前流水线先用 Maven 在 Drone 容器中打包所有模块，再通过多个 `appleboy/drone-ssh` 步骤进入服务器目录 `/usr/local/soft/alex_miaosha/drone/alex_miaosha/<service>`，执行 `docker rm -f`、`docker rmi -f`、`docker build`、`docker run`。优化方向是在保留现有部署方式的前提下，先补发布前后观测、容器运行参数和回滚标签，再逐步拆分构建缓存、服务发布顺序和 SSH 安全策略。

**技术栈：** Drone CI、Maven 3.8/OpenJDK 17、Docker、appleboy/drone-ssh、Spring Boot 微服务、Nacos、MySQL、Redis。

---

## 零、`.env` 配置现状补充

文件：`F:\workplace\project\myself\backend\alex_miaosha\.env`

当前 `.env` 中包含两类信息：

- 数据库连接：`DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD`
- Linux 服务器连接：`LINUX_URL`、`LINUX_USER`、`LINUX_PASSWORD`

这说明 Linux 服务器配置入口确实在 `.env` 中，但也带来一个高优先级安全问题：`.env` 当前包含生产服务器地址、root 用户、数据库账号和明文密码。后续优化不能只改 `.drone.yml`，还要治理 `.env` 的用途边界。

建议原则：

- `.env` 可以保留非敏感默认值和变量名模板。
- 真实密码、root 登录密码、数据库密码必须迁移到 Drone secrets、服务器本机环境变量或专用密钥管理。
- 仓库内保留 `.env.example`，不保留真实 `.env`。
- `.drone.yml` 不直接读取本地 `.env` 中的密码；Drone 发布使用 `from_secret`。
- 如果本地开发确实需要 `.env`，必须加入 `.gitignore`，并确认 Git 历史中没有继续传播明文凭据。

---

## 一、当前 `.drone.yml` 关键问题

文件：`F:\workplace\project\myself\backend\alex_miaosha\.drone.yml`

### 1. 发布方式有停机和回滚风险

当前每个服务都是：

```bash
docker rm -f <service>
docker rmi -f alex_miaosha_<service>:latest
docker build -t alex_miaosha_<service>:latest .
docker run ... --name=<service> ... -d alex_miaosha_<service>:latest
```

风险：

- `docker rm -f` 会直接杀掉线上容器，发布期间存在短暂停机。
- `docker rmi -f ...:latest` 删除旧镜像，失败后没有直接可用的上一版本镜像。
- 所有镜像都叫 `latest`，无法定位某次 Drone 构建对应的镜像版本。
- `docker run -d` 只表示容器启动命令提交成功，不代表 Spring Boot 服务已经健康。

### 2. 容器运行参数缺少恢复和 JVM 约束

当前 `gateway`、`user`、`oss` 使用：

```bash
--memory=512m --cpus=0.5
```

`finance` 使用：

```bash
--memory=1024m --cpus=0.5
```

缺少：

- `--restart unless-stopped`
- JVM `JAVA_TOOL_OPTIONS`
- OOM 后退出策略
- healthcheck
- 发布后 `docker stats --no-stream` 验证

结合服务器截图，`user` 已到 81.43%，`gateway` 约 70.98%，`oss` 约 68.18%，这三个服务应优先治理。

### 3. 发布顺序不合理

当前顺序是：

```text
gateway -> finance -> user -> oss -> notify
```

风险：

- `gateway` 是入口服务，应该最后发布。
- 如果后端服务发布失败，先发布网关会把入口切到潜在异常链路。
- `product` 被注释，但构建阶段仍复制 product jar 和 Dockerfile。
- `monitor` 被注释，但构建阶段仍复制 monitor jar 和 Dockerfile。
- `mission` 创建目录但没有复制 jar，也没有部署步骤。
- `generator` 复制 jar 和 Dockerfile，但没有部署步骤。

### 4. 构建效率偏低

当前 Maven 命令：

```bash
mvn clean package -DskipTests=true -s settings.xml -B -U
```

风险：

- `-U` 每次强制检查依赖更新，会增加构建网络和时间成本。
- 当前 volume 只挂载打包产物目录，没有挂载 Maven 本地仓库 `/root/.m2`。
- 每次都全量构建和复制所有模块，即使只部署 4 个服务。

### 5. 安全和可维护性问题

当前 SSH 使用：

```yaml
password:
  from_secret: ssh_password
```

建议逐步迁移到 SSH key：

```yaml
key:
  from_secret: ssh_private_key
```

邮箱收件人 `734663446@qq.com` 明文写在 `.drone.yml` 中，建议迁移到 secret 或团队通知渠道配置。

---

## 二、文件结构

- 修改：`.drone.yml`
  - 调整构建缓存、部署顺序、镜像标签、容器运行参数、发布前后检查和回滚 pipeline。
- 修改：`.env`
  - 移除真实生产敏感信息，只保留本地开发所需且不应提交的配置。
- 新建：`.env.example`
  - 提供变量名模板，不包含真实 IP、账号或密码。
- 修改：`.gitignore`
  - 确保 `.env` 不再被提交。
- 新建：`docs/ops/alex-miaosha-runtime-runbook.md`
  - 记录 Drone 发布流程、服务器运行检查、回滚命令和告警阈值。
- 修改：`AGENTS.md`
  - 记录本次 Drone/运行状态优化后的工程约束，满足仓库中“关键代码后同步文档”的要求。

---

## 任务 1：治理 `.env` 中的 Linux 和数据库敏感配置

**文件：**
- 修改：`.env`
- 新建：`.env.example`
- 修改：`.gitignore`
- 修改：`docs/ops/alex-miaosha-runtime-runbook.md`

- [ ] **步骤 1：确认 `.env` 是否已被 Git 跟踪**

执行：

```bash
git ls-files .env
```

预期结果：

- 如果有输出 `.env`，说明真实凭据已经被 Git 跟踪，需要执行步骤 2。
- 如果没有输出，说明 `.env` 未被跟踪，只需要确认 `.gitignore`。

- [ ] **步骤 2：如果 `.env` 已被跟踪，从 Git 索引移除**

执行：

```bash
git rm --cached .env
```

预期结果：本地 `.env` 文件仍然存在，但不再进入后续提交。

- [ ] **步骤 3：在 `.gitignore` 中忽略 `.env`**

确认 `.gitignore` 包含：

```gitignore
.env
.env.local
.env.*.local
```

- [ ] **步骤 4：创建 `.env.example`**

创建 `.env.example`，只保留变量名模板：

```env
DB_HOST=
DB_PORT=
DB_NAME=
DB_USER=
DB_PASSWORD=

LINUX_URL=
LINUX_USER=
LINUX_PASSWORD=
```

说明：`.env.example` 不允许写真实服务器 IP、root 用户密码、数据库密码。

- [ ] **步骤 5：把 Drone 所需值迁移到 Drone secrets**

在 Drone 后台确认或新增这些 secret：

```text
ssh_ip
ssh_root
ssh_password
ssh_email_username
ssh_email_password
db_host
db_port
db_name
db_user
db_password
```

说明：

- 当前 `.drone.yml` 已经使用 `ssh_ip`、`ssh_root`、`ssh_password`。
- `.env` 中的 Linux 配置应只作为本地人工连接参考，不应作为 CI/CD 权威凭据。
- 数据库凭据如需用于 CI 测试，应通过 Drone secrets 注入。

- [ ] **步骤 6：在 runbook 记录凭据边界**

写入 `docs/ops/alex-miaosha-runtime-runbook.md`：

```markdown
## 配置和凭据边界

- `.env` 只用于本地开发，不提交真实生产配置。
- `.env.example` 只保存变量名模板。
- Drone 发布使用 Drone secrets：`ssh_ip`、`ssh_root`、`ssh_password`。
- 数据库连接如需进入 CI，必须使用 Drone secrets 注入，不允许从仓库读取明文密码。
- 后续应将 SSH 密码迁移为 `ssh_private_key`。
```

- [ ] **步骤 7：提交本任务**

```bash
git add .gitignore .env.example docs/ops/alex-miaosha-runtime-runbook.md
git add -u .env
git commit -m "chore: move environment secrets out of tracked config"
```

---

## 任务 2：先给 Drone 发布增加运行状态采样

**文件：**
- 修改：`.drone.yml`
- 新建：`docs/ops/alex-miaosha-runtime-runbook.md`

- [ ] **步骤 1：在 `build-package` 后增加发布前采样步骤**

在 `ssh-gateway` 之前插入：

```yaml
  - name: pre-deploy-runtime-snapshot
    pull: if-not-exists
    image: appleboy/drone-ssh
    settings:
      host:
        from_secret: ssh_ip
      port: 22
      username:
        from_secret: ssh_root
      password:
        from_secret: ssh_password
      script:
        - date -Is
        - docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
        - docker stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.NetIO}}\t{{.BlockIO}}\t{{.PIDs}}'
        - docker events --since 30m --filter event=oom --filter event=restart || true
```

- [ ] **步骤 2：运行 Drone 构建验证日志**

触发 `develop-1.0` 或合适分支构建后，在 Drone 日志确认出现：

```text
pre-deploy-runtime-snapshot
docker stats --no-stream
docker events --since 30m
```

- [ ] **步骤 3：写入 runbook**

创建 `docs/ops/alex-miaosha-runtime-runbook.md`：

```markdown
# alex_miaosha 运行状态 Runbook

## Drone 发布前检查

每次发布前必须在 Drone 日志中确认：
- `docker ps` 能看到 `gateway`、`user`、`oss`、`finance`。
- `docker stats --no-stream` 中 `user`、`gateway`、`oss` 内存未持续超过 85%。
- 最近 30 分钟没有 OOM 事件。

## 当前重点风险

- `user`：截图中约 81.43% 内存，优先优化。
- `gateway`：入口服务，约 70.98% 内存，必须最后发布。
- `oss`：约 68.18% 内存，需要确认大文件处理方式。
- `mysql`：网络和 Block I/O 高，需要另行开启慢查询观察。
```

- [ ] **步骤 4：提交本任务**

```bash
git add .drone.yml docs/ops/alex-miaosha-runtime-runbook.md
git commit -m "ci: add pre-deploy runtime snapshot"
```

---

## 任务 3：给业务容器补齐 restart、JVM 和 OOM 参数

**文件：**
- 修改：`.drone.yml`

- [ ] **步骤 1：修改 `ssh-user` 的 `docker run`**

把原 `docker run` 替换为：

```bash
docker run -p 30006:30006 --network=alex_miaosha_net \
  --log-opt max-size=10m --log-opt max-file=30 \
  --memory=512m --cpus=0.5 --restart unless-stopped \
  -e JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=40 -XX:MaxRAMPercentage=65 -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/logs" \
  --name=user \
  -v /usr/local/soft/alex_miaosha/drone/alex_miaosha/logs:/logs \
  -d alex_miaosha_user:${DRONE_BUILD_NUMBER}
```

- [ ] **步骤 2：修改 `ssh-gateway` 的 `docker run`**

把原 `docker run` 替换为：

```bash
docker run -p 30001:30001 --network=alex_miaosha_net \
  --log-opt max-size=10m --log-opt max-file=30 \
  --memory=512m --cpus=0.5 --restart unless-stopped \
  -e JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=40 -XX:MaxRAMPercentage=65 -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/logs" \
  --name=gateway \
  -v /usr/local/soft/alex_miaosha/drone/alex_miaosha/logs:/logs \
  -d alex_miaosha_gateway:${DRONE_BUILD_NUMBER}
```

- [ ] **步骤 3：修改 `ssh-oss` 的 `docker run`**

把原 `docker run` 替换为：

```bash
docker run -p 30009:30009 --network=alex_miaosha_net \
  --log-opt max-size=10m --log-opt max-file=30 \
  --memory=512m --cpus=0.5 --restart unless-stopped \
  -e JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=40 -XX:MaxRAMPercentage=65 -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/logs" \
  --name=oss \
  -v /usr/local/soft/alex_miaosha/drone/alex_miaosha/logs:/logs \
  -d alex_miaosha_oss:${DRONE_BUILD_NUMBER}
```

- [ ] **步骤 4：修改 `ssh-finance` 的 `docker run`**

把原 `docker run` 替换为：

```bash
docker run -p 30008:30008 --network=alex_miaosha_net \
  --log-opt max-size=10m --log-opt max-file=30 \
  --memory=1024m --cpus=0.5 --restart unless-stopped \
  -e JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=35 -XX:MaxRAMPercentage=70 -XX:MaxMetaspaceSize=192m -XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/logs" \
  --name=finance \
  -v /usr/local/soft/alex_miaosha/drone/alex_miaosha/logs:/logs \
  -d alex_miaosha_finance:${DRONE_BUILD_NUMBER}
```

- [ ] **步骤 5：提交本任务**

```bash
git add .drone.yml
git commit -m "ci: tune runtime options for deployed services"
```

---

## 任务 4：改用构建号镜像标签，保留可回滚版本

**文件：**
- 修改：`.drone.yml`

- [ ] **步骤 1：修改 gateway 构建命令**

把：

```bash
docker rmi -f alex_miaosha_gateway:latest
docker build  -t alex_miaosha_gateway:latest .
```

替换为：

```bash
docker build -t alex_miaosha_gateway:${DRONE_BUILD_NUMBER} -t alex_miaosha_gateway:latest .
```

- [ ] **步骤 2：修改 finance 构建命令**

把：

```bash
docker rmi -f alex_miaosha_finance:latest
docker build  -t alex_miaosha_finance:latest .
```

替换为：

```bash
docker build -t alex_miaosha_finance:${DRONE_BUILD_NUMBER} -t alex_miaosha_finance:latest .
```

- [ ] **步骤 3：修改 user 构建命令**

把：

```bash
docker rmi -f alex_miaosha_user:latest
docker build  -t alex_miaosha_user:latest .
```

替换为：

```bash
docker build -t alex_miaosha_user:${DRONE_BUILD_NUMBER} -t alex_miaosha_user:latest .
```

- [ ] **步骤 4：修改 oss 构建命令**

把：

```bash
docker rmi -f alex_miaosha_oss:latest
docker build  -t alex_miaosha_oss:latest .
```

替换为：

```bash
docker build -t alex_miaosha_oss:${DRONE_BUILD_NUMBER} -t alex_miaosha_oss:latest .
```

- [ ] **步骤 5：确认不再删除上一版镜像**

执行一次构建后，在服务器查看：

```bash
docker images | grep alex_miaosha_
```

预期结果：同一服务至少保留 `latest` 和一个构建号标签，例如 `alex_miaosha_user:128`。

- [ ] **步骤 6：提交本任务**

```bash
git add .drone.yml
git commit -m "ci: tag service images with Drone build number"
```

---

## 任务 5：调整发布顺序，gateway 最后发布

**文件：**
- 修改：`.drone.yml`

- [ ] **步骤 1：移动 Drone 步骤顺序**

调整为：

```text
build-package
pre-deploy-runtime-snapshot
ssh-finance
ssh-user
ssh-oss
ssh-gateway
post-deploy-healthcheck
notify
```

说明：

- `gateway` 是入口服务，最后发布。
- `finance`、`user`、`oss` 先完成发布和基础检查。
- 如果业务服务发布失败，Drone 会停止后续步骤，不会继续发布 gateway。

- [ ] **步骤 2：确认 Drone 步骤实际执行顺序**

触发一次构建，在 Drone 页面确认日志顺序为：

```text
build-package -> pre-deploy-runtime-snapshot -> ssh-finance -> ssh-user -> ssh-oss -> ssh-gateway -> post-deploy-healthcheck -> notify
```

- [ ] **步骤 3：提交本任务**

```bash
git add .drone.yml
git commit -m "ci: deploy gateway after backend services"
```

---

## 任务 6：增加发布后健康检查和资源验证

**文件：**
- 修改：`.drone.yml`
- 修改：`docs/ops/alex-miaosha-runtime-runbook.md`

- [ ] **步骤 1：在 `ssh-gateway` 后增加 `post-deploy-healthcheck`**

插入：

```yaml
  - name: post-deploy-healthcheck
    pull: if-not-exists
    image: appleboy/drone-ssh
    settings:
      host:
        from_secret: ssh_ip
      port: 22
      username:
        from_secret: ssh_root
      password:
        from_secret: ssh_password
      script:
        - docker ps --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'
        - docker stats --no-stream gateway finance user oss
        - docker logs --tail 100 gateway
        - docker logs --tail 100 finance
        - docker logs --tail 100 user
        - docker logs --tail 100 oss
        - curl -fsS http://127.0.0.1:30001/actuator/health
```

- [ ] **步骤 2：如果 health 接口未启用，改用端口检查**

如果上一步 `curl` 返回 404 或连接失败，但服务实际没有暴露 actuator，则临时替换为：

```bash
docker exec gateway sh -c "netstat -tln 2>/dev/null | grep ':30001' || ss -tln | grep ':30001'"
```

同时在 runbook 记录“后续需要启用 actuator health”。

- [ ] **步骤 3：写入 runbook 发布后检查**

追加到 `docs/ops/alex-miaosha-runtime-runbook.md`：

```markdown
## Drone 发布后检查

发布后必须确认：
- `gateway`、`finance`、`user`、`oss` 都处于运行状态。
- `docker stats --no-stream` 没有服务内存超过 85%。
- `gateway` 日志没有启动失败或 Nacos 注册失败。
- 网关健康检查 `http://127.0.0.1:30001/actuator/health` 成功，若未启用 actuator，则必须至少确认 30001 端口已监听。
```

- [ ] **步骤 4：提交本任务**

```bash
git add .drone.yml docs/ops/alex-miaosha-runtime-runbook.md
git commit -m "ci: add post-deploy health checks"
```

---

## 任务 7：降低 Maven 构建和 Drone runner 压力

**文件：**
- 修改：`.drone.yml`

- [ ] **步骤 1：增加 Maven 缓存 volume**

在 `volumes` 中增加：

```yaml
  - name: maven-cache
    host:
      path: /usr/local/soft/alex_miaosha/drone/cache/m2
```

- [ ] **步骤 2：给 `build-package` 挂载 Maven 缓存**

在 `build-package` 的 `volumes` 下增加：

```yaml
      - name: maven-cache
        path: /root/.m2
```

- [ ] **步骤 3：去掉 Maven 的强制更新参数**

把：

```bash
mvn clean package -DskipTests=true -s settings.xml -B -U
```

替换为：

```bash
mvn clean package -DskipTests=true -s settings.xml -B
```

- [ ] **步骤 4：验证构建耗时下降**

连续触发两次 Drone 构建，对比 `build-package` 步骤耗时。预期第二次构建不再大量下载 Maven 依赖。

- [ ] **步骤 5：提交本任务**

```bash
git add .drone.yml
git commit -m "ci: add Maven cache for Drone builds"
```

---

## 任务 8：收敛无效构建产物和注释服务

**文件：**
- 修改：`.drone.yml`

- [ ] **步骤 1：确认本次实际部署服务**

当前实际启用的 SSH 部署步骤是：

```text
ssh-gateway
ssh-finance
ssh-user
ssh-oss
```

当前注释掉的部署步骤是：

```text
ssh-monitor
ssh-product
```

当前只复制但不部署：

```text
generator
```

当前只创建目录但不复制、不部署：

```text
mission
```

- [ ] **步骤 2：移除本次不发布服务的复制命令**

如果本次只发布 `gateway`、`finance`、`user`、`oss`，从 `build-package` 移除：

```bash
mkdir -p /usr/local/soft/alex_miaosha/drone/alex_miaosha/monitor
mkdir -p /usr/local/soft/alex_miaosha/drone/alex_miaosha/mission
mkdir -p /usr/local/soft/alex_miaosha/drone/alex_miaosha/generator
mkdir -p /usr/local/soft/alex_miaosha/drone/alex_miaosha/product
cp /drone/src/alex_miaosha_monitor/target/alex_miaosha_monitor-1.0-SNAPSHOT.jar  /usr/local/soft/alex_miaosha/drone/alex_miaosha/monitor/
cp /drone/src/alex_generator/target/alex_generator-1.0-SNAPSHOT.jar  /usr/local/soft/alex_miaosha/drone/alex_miaosha/generator/
cp /drone/src/alex_miaosha_product/product_boot/target/product_boot-1.0-SNAPSHOT.jar  /usr/local/soft/alex_miaosha/drone/alex_miaosha/product/
cp /drone/src/alex_miaosha_monitor/Dockerfile /usr/local/soft/alex_miaosha/drone/alex_miaosha/monitor/
cp /drone/src/alex_generator/Dockerfile /usr/local/soft/alex_miaosha/drone/alex_miaosha/generator/
cp /drone/src/alex_miaosha_product/product_boot/Dockerfile /usr/local/soft/alex_miaosha/drone/alex_miaosha/product/
```

- [ ] **步骤 3：保留注释说明**

在 `.drone.yml` 注释中写明：

```yaml
# monitor、product、generator、mission 当前不在默认发布链路中。
# 如需发布，先恢复对应 jar/Dockerfile 复制命令，再恢复对应 ssh-* 步骤。
```

- [ ] **步骤 4：提交本任务**

```bash
git add .drone.yml
git commit -m "ci: remove unused deployment artifacts"
```

---

## 任务 9：增加手动回滚 pipeline

**文件：**
- 修改：`.drone.yml`
- 修改：`docs/ops/alex-miaosha-runtime-runbook.md`

- [ ] **步骤 1：在 `.drone.yml` 末尾增加 rollback pipeline**

追加：

```yaml
---
kind: pipeline
type: docker
name: rollback-miaosha

trigger:
  event:
    - custom

steps:
  - name: rollback-service
    pull: if-not-exists
    image: appleboy/drone-ssh
    settings:
      host:
        from_secret: ssh_ip
      port: 22
      username:
        from_secret: ssh_root
      password:
        from_secret: ssh_password
      script:
        - test -n "$ROLLBACK_SERVICE"
        - test -n "$ROLLBACK_IMAGE"
        - docker rm -f "$ROLLBACK_SERVICE"
        - docker run --network=alex_miaosha_net --restart unless-stopped --name="$ROLLBACK_SERVICE" -d "$ROLLBACK_IMAGE"
        - sleep 30
        - docker ps --filter "name=$ROLLBACK_SERVICE"
        - docker stats --no-stream "$ROLLBACK_SERVICE"
```

- [ ] **步骤 2：在 runbook 记录回滚变量**

追加：

```markdown
## Drone 手动回滚

触发 `rollback-miaosha` 自定义构建时传入：
- `ROLLBACK_SERVICE`：容器名，例如 `user`。
- `ROLLBACK_IMAGE`：镜像名，例如 `alex_miaosha_user:128`。

回滚前先执行：

```bash
docker images | grep alex_miaosha_user
docker ps --filter "name=user"
```
```

- [ ] **步骤 3：提交本任务**

```bash
git add .drone.yml docs/ops/alex-miaosha-runtime-runbook.md
git commit -m "ci: add manual rollback pipeline"
```

---

## 任务 10：迁移 SSH 密码到密钥认证

**文件：**
- 修改：`.drone.yml`
- 修改：`docs/ops/alex-miaosha-runtime-runbook.md`

- [ ] **步骤 1：在 Drone 后台新增 secret**

新增：

```text
ssh_private_key
```

保留现有：

```text
ssh_ip
ssh_root
ssh_password
```

直到密钥验证通过后再删除 `ssh_password`。

- [ ] **步骤 2：把一个低风险步骤改为 key 认证**

先改 `pre-deploy-runtime-snapshot`：

```yaml
      key:
        from_secret: ssh_private_key
```

删除该步骤中的：

```yaml
      password:
        from_secret: ssh_password
```

- [ ] **步骤 3：验证后替换所有 SSH 步骤**

确认 `pre-deploy-runtime-snapshot` 成功后，将 `ssh-finance`、`ssh-user`、`ssh-oss`、`ssh-gateway`、`post-deploy-healthcheck`、`rollback-service` 都改为：

```yaml
      key:
        from_secret: ssh_private_key
```

- [ ] **步骤 4：提交本任务**

```bash
git add .drone.yml docs/ops/alex-miaosha-runtime-runbook.md
git commit -m "ci: use SSH key authentication for deploys"
```

---

## 任务 11：同步仓库工程约束文档

**文件：**
- 修改：`AGENTS.md`

- [ ] **步骤 1：在 `AGENTS.md` 增加 Drone 发布约束**

追加：

```markdown
- 修改 `.drone.yml` 发布链路时，必须保留发布前 `docker stats --no-stream` 采样、发布后健康检查和构建号镜像标签；`gateway` 必须在业务服务之后发布。
- 线上 Docker 容器必须配置 `--restart unless-stopped`，Java 服务必须显式设置 `JAVA_TOOL_OPTIONS`，避免 512MiB 容器内 JVM 默认占用过高。
- Drone 发布不得删除上一版构建号镜像；回滚必须优先使用已存在的构建号镜像。
```

- [ ] **步骤 2：运行 graphify 更新**

执行：

```bash
graphify update .
```

如果本机没有 `graphify` 命令，执行：

```bash
npm run graphify:update
```

预期结果：知识图谱或相关分析文件更新成功；如果命令不存在，在提交说明中记录未运行原因。

- [ ] **步骤 3：提交本任务**

```bash
git add AGENTS.md graphify-out docs
git commit -m "docs: record Drone deployment constraints"
```

---

## 三、推荐执行顺序

1. 任务 1：先治理 `.env` 明文凭据，避免继续传播生产密码。
2. 任务 2：加发布前采样，马上提高可观测性。
3. 任务 4：先加构建号镜像标签，建立回滚基础。
4. 任务 3：给 `user`、`gateway`、`oss`、`finance` 加 JVM 和 restart 参数。
5. 任务 5：调整顺序，让 `gateway` 最后发布。
6. 任务 6：增加发布后健康检查。
7. 任务 7：加 Maven 缓存，降低 Drone runner 压力。
8. 任务 8：清理未部署服务的复制命令。
9. 任务 9：增加手动回滚 pipeline。
10. 任务 10：SSH 密码迁移到密钥。
11. 任务 11：同步 AGENTS 和 graphify。

---

## 四、成功标准

- Drone 日志中能看到发布前和发布后的 `docker stats --no-stream`。
- `.env` 不再提交真实 Linux/数据库密码，仓库中只保留 `.env.example` 模板。
- `gateway` 在 `finance`、`user`、`oss` 后发布。
- `user`、`gateway`、`oss` 容器都带 `--restart unless-stopped` 和 JVM 内存参数。
- 每次构建都会产生 `alex_miaosha_<service>:${DRONE_BUILD_NUMBER}` 镜像。
- 服务器至少保留上一版构建号镜像，能通过 Drone 手动回滚。
- 第二次 Maven 构建耗时下降，依赖不再大量重复下载。
- `.drone.yml` 中不再依赖 SSH 密码作为长期部署方式。

---

## 五、自检

- 需求覆盖：计划已经基于真实 `.drone.yml` 和 `.env`，覆盖构建、部署、Linux 连接配置、凭据治理、运行参数、发布顺序、健康检查、回滚和文档同步。
- 占位符扫描：所有路径、服务名、端口、secret 名称均来自现有 `.drone.yml`、`.env` 或当前运行截图；没有保留不可执行占位。
- 风险控制：计划先增强观测和回滚，再调整运行参数和发布顺序，避免一上来大改部署方式。
