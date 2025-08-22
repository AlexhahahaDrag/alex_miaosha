# Token 过期时间更新功能

## 概述

本次更新实现了在用户重新登录时自动更新 token 过期时间的功能，提高了用户体验和系统安全性。

## 功能说明

### 1. 问题背景

在原来的实现中，当用户已经登录且 token 仍然有效时，系统会直接返回现有的 token，但不会更新 token 的过期时间。这可能导致以下问题：

- 用户频繁登录但 token 过期时间不延长
- 用户体验不佳，需要频繁重新登录
- 安全性考虑，token 过期时间应该随用户活动而延长

### 2. 解决方案

在 `TUserServiceImp.login()` 方法中，当检测到用户已登录且 token 有效时，现在会：

1. **计算新的过期时间**：根据是否记住登录状态确定过期时间
2. **更新 Redis 中的相关 key**：
   - `LoginKey.loginAdmin`：用户登录信息
   - `LoginKey.loginToken`：token 对应的用户信息
   - `LoginKey.loginUuid`：uuid 与 token 的映射关系
3. **记录日志**：记录 token 过期时间更新操作

### 3. 实现代码

```java
if (redisUser != null && StringUtils.isNotBlank(headers) && authToken(headers)) {
    // 更新token过期时间
    long expiration = isRemember != null && isRemember ? isRememberMeExpiresSecond : audience.getExpiresSecond();

    // 重新设置Redis中相关key的值和过期时间
    redisUtils.setEx(LoginKey.loginAdmin, ip + RedisConstants.SEGMENTATION + username, JSONObject.toJSONString(redisUser), expiration, TimeUnit.SECONDS);
    redisUtils.setEx(LoginKey.loginToken, headers, JSONObject.toJSONString(redisUser), expiration, TimeUnit.SECONDS);

    // 获取token对应的uuid并更新过期时间
    String tokenId = redisUtils.get(LoginKey.loginUuid, headers, String.class);
    if (StringUtils.isNotBlank(tokenId)) {
        redisUtils.setEx(LoginKey.loginUuid, tokenId, headers, expiration, TimeUnit.SECONDS);
    }

    log.info("用户 {} 已登录，更新token过期时间，新过期时间：{} 秒", username, expiration);
    result.put(SysConf.TOKEN, headers);
    result.put(SysConf.ADMIN, redisUser);
    return result;
}
```

## 配置参数

### 1. 记住登录过期时间

在 `application-dev.yaml` 中配置：

```yaml
isRememberMeExpiresSecond: 86400 #记住密码一天
```

### 2. 普通登录过期时间

在 `Audience` 配置类中配置：

```yaml
audience:
  expiresSecond: 7200 # 普通登录2小时
  refreshSecond: 1800 # 刷新时间30分钟
```

## 工作流程

### 1. 用户登录流程

1. 用户提交登录请求
2. 系统检查用户是否已登录且 token 有效
3. 如果已登录：
   - 更新所有相关 Redis key 的过期时间
   - 返回现有 token 和用户信息
4. 如果未登录：
   - 验证用户名密码
   - 生成新 token
   - 保存登录信息到 Redis

### 2. Token 验证流程

在 `authToken()` 方法中，系统会：

1. 验证 token 是否存在于 Redis 中
2. 检查 token 格式是否正确
3. 验证 JWT token 是否过期
4. 自动刷新即将过期的 token（在 `refreshToken()` 方法中）

## 优势

### 1. 用户体验提升

- 用户重新登录时 token 过期时间会延长
- 减少用户因 token 过期而需要重新登录的频率
- 支持"记住我"功能，提供更长的登录状态

### 2. 安全性提升

- token 过期时间随用户活动动态调整
- 自动刷新即将过期的 token
- 保持登录状态的同时确保安全性

### 3. 系统性能优化

- 减少不必要的 token 生成
- 优化 Redis 存储使用
- 提供详细的日志记录便于监控

## 注意事项

### 1. 过期时间设置

- 记住登录状态：24 小时（86400 秒）
- 普通登录：2 小时（7200 秒）
- 刷新时间：30 分钟（1800 秒）

### 2. Redis Key 管理

- 确保所有相关的 Redis key 都得到更新
- 保持 key 之间的一致性
- 避免内存泄漏

### 3. 日志监控

- 记录 token 更新操作
- 监控异常情况
- 便于问题排查

## 测试验证

### 1. 功能测试

1. 用户首次登录，获取 token
2. 在 token 有效期内重新登录
3. 验证 token 过期时间是否延长
4. 检查 Redis 中相关 key 的过期时间

### 2. 边界测试

1. token 即将过期时的处理
2. 记住登录状态与普通登录的区别
3. 网络异常时的处理

### 3. 性能测试

1. 高并发登录场景
2. Redis 操作性能
3. 内存使用情况

这个改进显著提升了用户体验，同时保持了系统的安全性和性能。
