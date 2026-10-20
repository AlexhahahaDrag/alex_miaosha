# Checklist: OSS Upload Security, Path Sanitization & Presigned Contract (第三十四阶段)

**Module:** `alex_miaosha_oss` (`oss_boot`)  
**Date:** 2026-10-20  
**Standard:** `TESTING_STANDARD.md` (Level 2 Integration Tests)

---

## 1. 单文件上传与修改白名单安全防御矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 1 | 单文件新增拦截危险扩展名 | `OssSingleFileSecurityIT#addFileInfo_dangerousExtension_mustBeRejected` | 参数化测试 `.jsp`, `.exe`, `.php`, `.sh`, `.bat`, `.py`, `.vbs`, `.dll` 强阻断，抛出 `PARAM_ERROR`，底层 0 次上传调用 | ✅ PASS |
| 2 | 单文件更新拦截危险扩展名 | `OssSingleFileSecurityIT#updateFileInfo_dangerousExtension_mustBeRejected` | 参数化测试修改时传入危险脚本强拦截，抛出 `PARAM_ERROR`，绝不更新数据库 | ✅ PASS |
| 3 | 单文件新增合规格式放行 | `OssSingleFileSecurityIT#addFileInfo_validExtension_shouldSucceed` | 合规白名单文件（`.png` 等）正常调用底层上传并持久化至 `file_info` 表 | ✅ PASS |
| 4 | 单文件更新合规格式放行 | `OssSingleFileSecurityIT#updateFileInfo_validExtension_shouldSucceed` | 合规新文件成功上传并更新数据库记录 | ✅ PASS |
| 5 | 单文件更新跳过上传边界 | `OssSingleFileSecurityIT#updateFileInfo_nullFile_shouldSkipUpload` | 传 `file == null` 时跳过扩展名校验与上传，仅更新实体元数据 | ✅ PASS |
| 6 | 单文件更新记录不存在防御 | `OssSingleFileSecurityIT#updateFileInfo_recordNotFound_mustThrow` | 实体不存在时安全抛出 `SYSTEM_NO_AVAILABLE` 业务异常 | ✅ PASS |
| 7 | 空字节截断与空文件名防御 | `OssSingleFileSecurityIT#nullByteAndEmptyName_mustBeRejected` | 文件名包含 `\0` 空字节截断或纯空白文件名时前置拦截并返回友好提示 | ✅ PASS |

---

## 2. S3 存储对象键路径穿越清洗与特殊字符净化矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 8 | 业务类型路径穿越清洗 | `OssPathSanitizationIT#sanitizeType_shouldStripTraversalAndSpecialChars` | 参数化测试 `../../evil` -> `evil`，`goods/../../hack` -> `hack`，`###` -> `common`，空值 -> `common` | ✅ PASS |
| 9 | 基础文件名路径前缀剥离 | `OssPathSanitizationIT#sanitizeBaseFileName_shouldExtractPureBaseName` | 参数化测试 `../../etc/passwd.png` -> `passwd`，`dir/doc.pdf` -> `doc`，空值/畸变值 -> `file` | ✅ PASS |
| 10 | 空字节 `\0` 净化剥离 | `OssPathSanitizationIT#sanitizeBaseFileName_nullByte_shouldBeStripped` | 文件名中 `avatar\0evil.png` 的 `\0` 彻底剥离，绝不残留空字节 | ✅ PASS |
| 11 | 端到端穿越注入防御 | `OssPathSanitizationIT#uploadFile_withPathTraversalInputs_mustProduceSafeObjectKey` | 恶意 `type` 导致桶安全降级到配置桶，Object Key 强制约束为三段式 `type/yyyy-MM-dd/filename_timestamp.png`，杜绝 `..` 与 `\` | ✅ PASS |

---

## 3. S3 预签名直链、免签持久直链与防盗链时效契约矩阵

| # | 测试项 | 覆盖类 / 方法 | 验证断言 | 状态 |
|---|---|---|---|:---:|
| 12 | 公开桶免签持久直链 | `OssPresignedUrlAndContractIT#preview_withPublicTrue_shouldGeneratePermanentUrlWithoutSignature` | `isPublic=true` 时构造持久直链，支持 `publicUrl` 映射，底层 0 次 MinIO 预签名调用 | ✅ PASS |
| 13 | 私有桶预签名直链时效 | `OssPresignedUrlAndContractIT#preview_withPublicFalse_shouldInvokePresignedSdkWith1HourExpiry` | `isPublic=false` 时调用 MinIO 预签名 SDK 并强制配置 3600 秒时效，支持外部 CDN 域名替换 | ✅ PASS |
| 14 | 默认防盗链私有策略 | `OssPresignedUrlAndContractIT#preview_withNullPublic_shouldDefaultToPresignedUrl` | `isPublic=null` 时遵循安全基线，默认签发带签名预签名直链 | ✅ PASS |
| 15 | 空对象键优雅容错 | `OssPresignedUrlAndContractIT#preview_withBlankObjectKey_shouldReturnNullWithoutCallingSdk` | `objectKey` 为 null 或空白字符串时安全返回 null，避免无意义网络探测 | ✅ PASS |
| 16 | 未初始化状态防呆 | `OssPresignedUrlAndContractIT#preview_notInitialized_mustThrowIllegalStateException` | 客户端未完成初始化时强断言并抛出 `IllegalStateException` | ✅ PASS |
| 17 | URL 路径分段编码保真 | `OssPresignedUrlAndContractIT#encodePathKey_preservesPathSeparatorsAndEncodesChineseAndSpaces` | 保留斜杠 `/`，安全转义中文与空格，并防范二次编码 | ✅ PASS |
| 18 | 默认端点自动拼装 | `OssPresignedUrlAndContractIT#buildPublicDirectUrl_fallbackToEndpointWhenNoPublicUrl` | 未配置 `publicUrl` 时根据 endpoint、port 和 secure 自动组装正确直链 | ✅ PASS |

---

## 4. 不测理由与排除清单

1. **真实物理 MinIO / S3 集群文件网络 IO**：自动化测试聚焦于单文件上传防御、路径清洗算法、S3 对象键安全组装及预签名直链策略；真实物理 S3 集群依赖物理网络与密钥凭证，由生产基础设施运维保障；
2. **秒杀模块排除**：按照用户明确指令，本阶段测试与代码加固严格排除秒杀模块。
