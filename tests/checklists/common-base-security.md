# Checklist: Common Base Exception Standard, Distributed Aspects & Security Utilities (第三十一阶段)

**Module:** `alex_miaosha_common` (`common_core` & `common_api`)  
**Date:** 2026-10-20  
**Standard:** `TESTING_STANDARD.md` (Level 2 Integration Tests)

---

## 1. 全局异常处理与错误码映射保真矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 1 | 系统基础异常（SystemException）精确处理 | `GlobalExceptionHandlingIT#testSystemException_WithParamError_Returns400` | 修复此前遗漏 `@ExceptionHandler(SystemException.class)` 缺陷，参数非法错误码精确映射为 400，拒绝被兜底转为 500 | ✅ PASS |
| 2 | 系统服务不可用精确映射 | `GlobalExceptionHandlingIT#testSystemException_WithServiceUnavailable_Returns500102` | `ResultEnum.SYSTEM_NO_AVAILABLE` 错误码（500102）精确透传保真，非 500 抹杀 | ✅ PASS |
| 3 | Spring 参数校验错误信息聚合 | `GlobalExceptionHandlingIT#testBindException_AggregatesValidationErrors` | `BindException` 提取所有字段的 `defaultMessage` 并通过逗号优雅聚合，返回 400 提示 | ✅ PASS |
| 4 | JSON 反序列化格式异常友好提示 | `GlobalExceptionHandlingIT#testHttpMessageNotReadableException_FriendlyMessage` | `HttpMessageNotReadableException` 拦截并转换为包含"请求参数格式有误"的友好提示，消除底层堆栈泄露 | ✅ PASS |
| 5 | 各垂直领域业务异常精确保真 | `GlobalExceptionHandlingIT#testDomainExceptions_PreserveSpecificCodes` | `UserException`, `FinanceException`, `AiException` 各自保持其专用错误码与业务文案 | ✅ PASS |
| 6 | 兜底未知异常统一拦截 | `GlobalExceptionHandlingIT#testUnhandledException_Returns500Fallback` | 未知 RuntimeException 统一返回标准 500 错误码，保证接口返回结构一致性 | ✅ PASS |

---

## 2. 分布式并发切面（防重复提交与频次限流）矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 7 | 基于 Token 的分布式防重抢占 | `DistributedConcurrencyAspectIT#testAvoidRepeatableCommit_WithTokenHeader` | 携带 Token 请求头时优先使用 Token 生成 Redis Key 执行原子 SET NX，首次成功放行 | ✅ PASS |
| 8 | 客户端 IP 兜底防重与原子拦截 | `DistributedConcurrencyAspectIT#testAvoidRepeatableCommit_FallbackToIpAndBlocked` | 无 Token 时以真实客户端 IP 兜底，并发/短时重复提交被拦截并返回 `REPEAT_COMMIT` | ✅ PASS |
| 9 | 文件上传参数指纹提取防重 | `DistributedConcurrencyAspectIT#testAvoidRepeatableCommit_MultipartFileFingerprint` | 识别 `MultipartFile` 参数提取原始文件名与大小生成哈希指纹，防止对象默认哈希值不稳定导致防重失效 | ✅ PASS |
| 10 | 接口频次限流阈值控制 | `DistributedConcurrencyAspectIT#testAccessLimit_ThresholdAndBlock` | 基于 Redis 原子递增并在首击时写入 TTL，达到 limit 阈值时拦截并返回 `ACCESS_LIMIT_REACHED` (429) | ✅ PASS |
| 11 | URI + IP 维度隔离与配额互不干扰 | `DistributedConcurrencyAspectIT#testAccessLimit_UriIsolation` | 限流 Key 绑定 `URI + ":" + IP`，接口 A 限流不会串扰阻塞同一客户端访问接口 B | ✅ PASS |

---

## 3. 大数雪花 ID 序列化契约与 null 安全矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 12 | 19 位雪花 ID 字符串保真 | `Long2StringSerializationContractIT#testSnowflakeIdSerializedToString` | 大于 JS `MAX_SAFE_INTEGER` 的 64 位雪花算法 ID 强制序列化为带引号纯字符串，杜绝前端末位截断 | ✅ PASS |
| 13 | null 值安全序列化 | `Long2StringSerializationContractIT#testNullValueHandling` | 修复此前 `Long2StringSerializer` 缺失 null 分支缺陷，通过 `jsonGenerator.writeNull()` 安全输出 null | ✅ PASS |
| 14 | 边界值序列化一致性 | `Long2StringSerializationContractIT#testZeroAndSmallValues` | `0L`、`-1L` 等小数值统一输出为字符串 `"0"`、`"-1"`，保证前端数据契约一致 | ✅ PASS |
| 15 | 集合与嵌套对象属性批量序列化 | `Long2StringSerializationContractIT#testCollectionAndNestedContainerSerialization` | 容器对象与嵌套集合内的 Long 字段均保持准确转换无丢失 | ✅ PASS |

---

## 4. 核心安全加密与基础工具健壮性矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 16 | AES-GCM 随机 Nonce/IV 安全加密 | `SecurityAndUtilityHardeningIT#testAesGcm_EncryptAndDecrypt` | 每次加密使用 SecureRandom 生成 12 字节全新 IV，同一明文多次加密密文均不相同，解密全生命周期可逆 | ✅ PASS |
| 17 | AEAD 认证标签防篡改校验 | `SecurityAndUtilityHardeningIT#testAesGcm_TamperedCiphertextThrowsAEADBadTagException` | 密文被单字节篡改时通过 128 位 GCM 认证标签检验阻断攻击，严格抛出 `AEADBadTagException` | ✅ PASS |
| 18 | 密文非法长度前置防御 | `SecurityAndUtilityHardeningIT#testAesGcm_IllegalLengthGuard` | 密文小于 28 字节（12 字节 IV + 16 字节 Tag）时前置防御抛出 `IllegalArgumentException` | ✅ PASS |
| 19 | DateUtils 多线程高并发安全 | `SecurityAndUtilityHardeningIT#testDateUtils_ConcurrentSafety` | 基于不可变静态预编译 `DateTimeFormatter`，10 线程高并发下格式化 100% 成功无线程安全隐患 | ✅ PASS |
| 20 | 时区安全与当天起止边界 | `SecurityAndUtilityHardeningIT#testDateUtils_StartAndEndTime` | `DateUtils.now()` 统一绑定 `ZoneId.systemDefault()`，当天起始时间准确对齐 `00:00:00` | ✅ PASS |
| 21 | 反向代理客户端真实 IP 解析 | `SecurityAndUtilityHardeningIT#testIpUtils_XForwardedForMultiProxy` | 多级代理（Nginx/CDN）下准确截取首个 IP 作为客户端真实 IP | ✅ PASS |
| 22 | 客户端环境 User-Agent 提取 | `SecurityAndUtilityHardeningIT#testIpUtils_OsAndBrowserExtraction` | 准确提取操作系统与浏览器大写标识（`OS` / `BROWSER`） | ✅ PASS |
| 23 | null 请求防御性安全处理 | `SecurityAndUtilityHardeningIT#testIpUtils_NullRequestHandling` | 请求对象为 null 时安全返回空字符串，杜绝空指针异常 | ✅ PASS |

---

## 5. 不测理由

1. **真实物理 Redis 集群故障转移**：切面集成测试聚焦于原子命令调用、分布式键构造、Token/IP 提取与多维度隔离语义；中间件底层网络复制由基础运维环境保证；
2. **秒杀模块排除**：遵循用户明确指令“秒杀的东西都先不做”，本次测试严格排除秒杀限流切面（`SeckillLimitAspect`）与秒杀业务逻辑。
