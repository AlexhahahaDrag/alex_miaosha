# 用户认证、Token 刷新与在线生命周期安全质量清单 (`user-auth-security.md`)

## 1. 业务场景与覆盖范围

涵盖 `alex_miaosha_user` 服务的核心身份认证、Token 异步刷新、会话生命周期与密码安全防御：
- **Token 刷新**：`TokenRefreshService.refreshTokenAsync` 临近过期刷新、并发防雪崩锁；
- **在线会话管理**：`OnlineUserService` 秒级过期时间计算防失真（纠正 `MICROS` -> `SECONDS`）；
- **登出联动**：`TUserServiceImpl#logout` 原子清理 `loginUuid`、`loginToken` 与 `loginOnlineUser`；
- **密码安全**：`updateTUser` 忽略明文 `password` 防篡改，新增 `resetUserPassword` 强校验与盐值加密；
- **认证契约**：账号禁用（`status=0`）拦截、密码错误累计锁定（5次/30分钟）、空参防御。

---

## 2. 字段边界与输入防御矩阵（七点法）

| 字段 | 极小值/空值 | 边界下值 | 正常边界值 | 越权/非法值 | 超长值 | 注入/特殊字符 | 防御策略 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `username` | `null` / `""` | 长度 1 | 常规用户名/邮箱/手机号 | 禁用用户状态 | 长度 > 64 | SQL/XSS 符号 | 抛出 `USER_USERNAME_OR_PASSWORD_EMPTY`，状态限定为 `ENABLE` |
| `password` | `null` / `""` | 长度 5 | 长度 $\ge 6$ 位强口令 | 明文覆盖已有密文 | 长度 > 128 | 空格串 `"   "` | `resetUserPassword` 强校验 $\ge 6$，`updateTUser` 显式忽略 |
| `uuidToken` | `null` / `""` | "undefined" | UUID-v4 字符串 | 任意伪造 TokenId | 长度 > 128 | 跨站请求伪造 | `logout` 拦截无效 Token 返回 `PARAM_ERROR`，阻断误删 |
| `newPassword`| 空白字符串 | 5位字符 | 6位及以上字符 | 绕过权限修改他人密码 | 256字符 | 特殊符号支持 | `assertUserAccessible` 数据权限校验，BCrypt 加盐散列 |

---

## 3. 状态机与会话流转规范

```mermaid
stateDiagram-v2
    [*] --> 未登录
    未登录 --> 密码校验中: POST /login
    密码校验中 --> 锁定30分钟: 失败重试次数 >= 5
    密码校验中 --> 登录成功: 密码匹配 & status=1
    登录成功 --> 在线状态: 写入 loginUuid / loginToken / loginOnlineUser
    在线状态 --> Token刷新中: 存活时间 < refreshSecond
    Token刷新中 --> 在线状态: 获取 loginRefreshLock 成功并更新
    在线状态 --> 未登录: POST /logout 清理全部缓存会话
    在线状态 --> 会话过期: Redis TTL 到期逐出
```

---

## 4. 权限矩阵

| 接口 / 操作 | 超级管理员 (`SUPER`) | 机构管理员 (`ORG_ADMIN`) | 普通用户 (`USER`) | 未认证请求 |
| :--- | :---: | :---: | :---: | :---: |
| 用户资料更新 (`updateTUser`) | 全系统放行 (忽略密码) | 仅本机构用户 (忽略密码) | 仅本人资料 (忽略密码) | 401 Unauthorized |
| 重置用户密码 (`resetUserPassword`) | 允许重置全员密码 | 仅限本机构归属用户 | 禁止越权操作 | 401 Unauthorized |
| 用户主动登出 (`logout`) | 仅清理自身 Token | 仅清理自身 Token | 仅清理自身 Token | 400 PARAM_ERROR |
| Token 异步刷新 (`refreshTokenAsync`) | 自动延期 (锁防重) | 自动延期 (锁防重) | 自动延期 (锁防重) | 401 Unauthorized |

---

## 5. 自动化测试套件与覆盖情况

1. **`UserTokenAndOnlineLifecycleIT`**（6 项测试用例全部 PASS）：
   - `onlineUserService_buildAdmin_expireTimeInSeconds`：验证秒级 `SECONDS` 过期时间累加；
   - `logout_cleansUuidTokenAndOnlineUser`：验证登出联动清理三大 Redis 登录键；
   - `logout_withInvalidOrMissingToken_returnsParamError`：验证非法 Header 防呆拦截；
   - `tokenRefresh_whenExpiringSoon_triggersRefreshAndSetsLock`：验证刷新时效判定与互斥锁加锁；
   - `tokenRefresh_concurrentCalls_blockedByLock`：验证并发防重刷与旧 Token 防误删；
   - `tokenRefresh_survivalTimeAmple_skipsRefresh`：验证存活时间充裕时的静默跳过。
2. **`UserPasswordSecurityAndOwnershipIT`**（5 项测试用例全部 PASS）：
   - `updateTUser_ignoresPlaintextPassword_neverOverwritesWithPlaintext`：验证常规资料更新不覆盖密码；
   - `resetPassword_superAdmin_encodesWithSaltAndInvalidatesContext`：验证加盐散列与上下文失效；
   - `resetPassword_weakOrBlankPassword_rejected`：验证空密码与小于6位弱密拦截；
   - `resetPassword_nonExistentUser_rejected`：验证不存在用户的防呆拦截；
   - `resetPassword_orgAdminAccessCrossOrg_rejected`：验证机构管理员跨租户/跨机构越权拦截。
3. **`UserAuthenticationContractIT`**（3 项测试用例全部 PASS）：
   - `login_blankUsernameOrPassword_rejected`：验证空参拦截；
   - `login_exceedMaxRetryLimit_rejected`：验证连续错误锁定；
   - `login_disabledAccount_rejected`：验证禁用账号拦截与重试计数累加。

---

## 6. 不测理由说明 (Exclusions)
- **前端原生密码输入强度评分组件**：由 PC 端与移动端基于 zxcvbn / 正则在端侧校验，后端聚焦于长度、空值、越权与加盐安全底线；
- **第三方 OAuth 回调网络重试**：由 JustAuth 第三方 SDK 保障，本服务已在 Unit/Mock 层完成状态机闭环。
