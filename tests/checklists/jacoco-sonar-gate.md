# 后端微服务 Jacoco 覆盖率门禁与 Sonar 扫描硬化测试清单

## 一、 领域边界与质量目标
本清单覆盖 `alex_miaosha` 后端微服务群（`finance`、`common`、`user`、`oss`、`ai`、`gateway`）的 Jacoco 代码覆盖率质量门禁与 SonarQube/SonarScanner 静态扫描硬化体系。
核心目标与架构约束：
1. **Jacoco 0.8.11 覆盖率探针与报告治理**：父 POM 统一引入 `jacoco-maven-plugin`，在 Maven 生命周期中打通 `prepare-agent` 自动注入 Surefire JVM `argLine`，并在 `verify` 或执行 `jacoco:report` 时输出标准 `target/site/jacoco/jacoco.xml` 与 HTML 分析报告；
2. **多模块排除规约（防指标稀释）**：精准排除 VO、DTO、Entity 实体、MyBatis Mapper 接口、Spring Boot 启动类（`*Application.class`）、系统配置类（`**/config/**`）、常量与枚举，防止模板代码稀释业务核心逻辑覆盖率；
3. **Surefire 参数防冲撞**：子模块 `maven-surefire-plugin` 保障 `@{argLine}` 注入通道顺畅，防止自定义 JVM 参数覆盖 Jacoco Agent；
4. **覆盖率质量红线 (-Pcoverage Profile)**：在 CI/CD 流水线中支持通过 `-Pcoverage` 激活严苛的 `check` 目标，对核心业务包行覆盖率（LINE ≥ 60%）与分支覆盖率（BRANCH ≥ 50%）进行自动化阻断；
5. **Sonar 扫描标准化与代码异味硬化**：
   - `sonar.java.coveragePlugin=jacoco`
   - `sonar.coverage.jacoco.xmlReportPaths=${project.build.directory}/site/jacoco/jacoco.xml`
   - 固化 `java:S6888`（时区安全：严禁裸调 `LocalDateTime.now()`，统一走 `DateUtils.now()`）、`java:S2142`（线程中断恢复）、`java:S1170`（常量声明 `public static final`）、`java:S112`（禁止抛通用 `Exception`）等防腐基线。

---

## 二、 验证矩阵与执行实测

| 验证项编号 | 模块 / 目标 | 验证指令 / 场景 | 预期结果与质量指标 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| **JAC-POM-01** | `alex_miaosha` (根 POM) | `mvn validate` (JDK 17) | 根 POM 及全部 20 个子模块成功解析 `jacoco.version` (0.8.11) 与 `sonar-maven-plugin.version`，0 警告 0 报错 | ✅ PASS |
| **JAC-AGENT-02** | `common_core` | `mvn test jacoco:report -pl alex_miaosha_common/common_core -am "-Dtest=DateUtilsTest,AESGcmUtilsTest"` | 探针正常注入 `target/jacoco.exec`，产出 `target/site/jacoco/jacoco.xml`（160KB），36 个类成功分析 | ✅ PASS |
| **JAC-REPORT-03** | `finance_boot` | `mvn test jacoco:report -pl alex_miaosha_finance/finance_boot -am "-Dtest=SysMessage*,UserNotice*"` | 42 项单元/集成测试全部绿灯，产出 `target/site/jacoco/jacoco.xml`（516KB）与 HTML 报告，69 个类成功分析 | ✅ PASS |
| **JAC-COVER-04** | `SysMessageInfoServiceImpl` | 查看 `finance_boot/target/site/jacoco/jacoco.csv` 统计指标 | 指令覆盖率 **85.1%**（2257/2652），行覆盖率 **86.5%**（493/570），分支覆盖率 **62.6%**（254/406），达标 | ✅ PASS |
| **JAC-COVER-05** | `UserNoticeConfigServiceImpl`| 查看 `finance_boot/target/site/jacoco/jacoco.csv` 统计指标 | 指令覆盖率 **68.5%**（289/422），行覆盖率 **72.9%**（62/85），分支覆盖率 **72.9%**（51/70），达标 | ✅ PASS |
| **SONAR-PROP-06**| 根 POM 属性配置 | 校验 `sonar.coverage.jacoco.xmlReportPaths` 与 `sonar.coverage.exclusions` | 路径指向标准路径，排除项与 Jacoco `<excludes>` 严格对齐，避免模板类拉低 Sonar 得分 | ✅ PASS |
| **SCRIPT-RUN-07**| 便捷脚本 | `scripts/quality/run-coverage-check.bat` | 封装 JDK 17 与 Maven 覆盖率一键执行与路径指引 | ✅ PASS |
| **JAC-USER-08**  | `user_boot` 覆盖率 | `mvn test jacoco:report -pl alex_miaosha_user/user_boot` | 231 项测试全部绿灯，产出 `jacoco.xml`（252KB），核心服务行覆盖率普遍在 82%~95% | ✅ PASS |
| **SONAR-USER-09**| `user_boot` 异味消除 | 修复 `UserPermissionContextServiceTest` 线程中断标志位与 `UserTokenAndOnlineLifecycleIT` 时区时间 | 彻底消除 Sonar `java:S2142` 与 `java:S6888` 潜在告警，15 项回归测试 100% 通过 | ✅ PASS |
| **SONAR-USER-10**| `user_boot` 泛化异常 | 彻底重构 `TUserService.login/addOnLineAdmin`、`TUserController`、`IpUtils.getIpAddr` 泛化异常 | 消除 Sonar `java:S112` 告警，增加 IP 解析防御性降级，231 项测试全绿通过 | ✅ PASS |
| **SONAR-USER-11**| `user_boot` 安全热点 | 抽取 OAuth `clientSecret` 至 `OAuthProperties`，消除 `UserApplication` 绝对路径硬编码 | 彻底消除 Sonar `java:S2068` 与 `java:S5443` 安全热点，231 项测试全绿通过 | ✅ PASS |

---

## 三、 明确不测项及理由（Out of Scope）
1. **动态编译生成的 MyBatis-Plus 代理类与 Mapper 接口**：Mapper 仅声明接口与 SQL 映射，无需也无法由 Jacoco 统计字节码，统一在 `<excludes>` 中排除；
2. **VO/DTO/Entity 贫血模型的 Getter/Setter**：由 Lombok 编译期生成，无业务判定逻辑，统计在覆盖率内无工程意义，统一在 `<excludes>` 中排除；
3. **秒杀模块（`alex_miaosha_product` 秒杀逻辑）**：严格遵照用户指令排除秒杀相关逻辑。
