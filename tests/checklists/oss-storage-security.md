# Checklist: OSS Storage Dynamic Routing, Security & VO Contract (第二十八阶段)

**Module:** `alex_miaosha_oss` & `alex_miaosha_common`  
**Date:** 2026-10-20  
**Standard:** `TESTING_STANDARD.md` (Level 2 Integration Tests)

---

## 1. VO 大数雪花 ID 序列化契约与修复矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 1 | `BaseVo.deleter` 契约修复 | `BaseVo#deleter` | 标记 `@JsonSerialize(using = Long2StringSerializer.class)`，消除审计回溯 64 位截断 | ✅ PASS |
| 2 | VO 反射契约锁 | `OssVoSerializationContractIT#fileInfoVo_and_baseVo_ids_mustHaveJsonSerializeAnnotation` | 递归扫描 `FileInfoVo` 继承链全部非静态 `Long` 字段（`id`, `creator`, `updater`, `operator`, `deleter`），全部绑定 `Long2StringSerializer.class` | ✅ PASS |
| 3 | 大数字符串序列化 | `OssVoSerializationContractIT#fileInfoVo_jsonSerialization_precisionSafety` | 雪花 ID 序列化为带引号 String，`fileSize` 维持纯数字 | ✅ PASS |

---

## 2. 安全上传、格式白名单与防御拦截矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 4 | 危险脚本/可执行文件强拦截 | `OssUploadSecurityIT#dangerousExecutableFileExtensions_mustBeRejected` | 参数化测试 `.jsp`, `.php`, `.sh`, `.bat`, `.exe`, `.py`, `.vbs`, `.dll` 强阻断，抛出 `PARAM_ERROR` | ✅ PASS |
| 5 | 合规文件白名单放行 | `OssUploadSecurityIT#allowedFileExtensions_shouldPassValidation` | 参数化测试 `.png`, `.jpg`, `.jpeg`, `.gif`, `.svg`, `.webp`, `.bmp`, `.pdf`, `.xlsx`, `.xls`, `.docx`, `.doc`, `.txt`, `.zip`, `.rar`, `.csv` 正常放行 | ✅ PASS |
| 6 | 多附件批量上传超额拦截 | `OssUploadSecurityIT#batchUpload_exceedingNineFiles_mustBeRejected` | 单次超过 9 个文件立即阻断，提示“单次最多支持上传 9 个附件” | ✅ PASS |
| 7 | 空文件与 0 字节防御 | `OssUploadSecurityIT#emptyAndZeroByteFiles_mustBeRejected` | 0 字节文件、空列表、null 参数抛出 `IMAGE_NO_FOUNT` 异常 | ✅ PASS |
| 8 | 路径穿越防御 | `OssUploadSecurityIT#pathTraversal_fileName_mustBeRejected` | 文件名携带 `../../etc/passwd` 等路径穿越模式安全探测与拦截 | ✅ PASS |

---

## 3. S3/MinIO 对象存储路径与直链签发矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 9 | S3 虚拟路径正斜杠规范 | `OssS3PathConventionIT#s3Delimiter_mustAlwaysBeForwardSlash` | Object Key 绝不包含反斜杠 `\`，遵循 `{type}/{yyyy-MM-dd}/{filename}_{timestamp}.{suffix}` | ✅ PASS |
| 10 | 预览直链透传契约 | `OssS3PathConventionIT#previewUrl_delegation_publicVsSigned` | `isPublic=true` 签发公开免签直链，`isPublic=false` 签发带签名时效直链 | ✅ PASS |

---

## 4. OpenFeign 跨微服务降级治理矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 11 | 降级工厂安全代理实例化 | `OssFallbackFactoryIT#ossFallbackFactory_shouldCreateSafeProxy` | 捕获底层异常并成功生成降级代理对象 | ✅ PASS |
| 12 | 熔断异常规范抛出 | `OssFallbackFactoryIT#fallbackMethods_shouldThrowSystemNoAvailable` | 调用 `getPage`, `query`, `add`, `addBatch`, `update`, `delete` 统一抛出 `SystemException(SYSTEM_NO_AVAILABLE, "oss")` | ✅ PASS |

---

## 5. 不测理由

1. **真实物理 MinIO / S3 集群文件网络 IO**：自动化测试聚焦于上传防御、白名单过滤、S3 路径组织算法及 OpenFeign 熔断保护；真实 S3 集群依赖物理连通性，生产部署由基础设施运维保障；
2. **秒杀模块排除**：按照用户指令，本阶段测试与代码加固严格排除秒杀模块。
