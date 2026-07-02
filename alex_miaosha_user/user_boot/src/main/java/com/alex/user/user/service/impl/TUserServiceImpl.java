package com.alex.user.user.service.impl;

import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.api.user.orgInfo.vo.OrgInfoVo;
import com.alex.api.user.roleInfo.vo.RoleInfoVo;
import com.alex.api.user.user.UserUtils;
import com.alex.api.user.userInfo.vo.OnlineAdmin;
import com.alex.api.user.userInfo.vo.TUserVo;
import com.alex.api.user.userInfo.vo.UserPermissionContextVo;
import com.alex.base.common.Result;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.constants.message.MessageConf;
import com.alex.common.constants.redis.RedisConstants;
import com.alex.common.enums.EStatus;
import com.alex.common.exception.LoginException;
import com.alex.common.exception.UserException;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.menuInfo.service.MenuInfoService;
import com.alex.user.online.service.OnlineUserService;
import com.alex.user.orgUserInfo.service.OrgUserInfoService;
import com.alex.user.rbac.service.UserPermissionContextService;
import com.alex.user.roleUserInfo.service.RoleUserInfoService;
import com.alex.user.tUserLogin.entity.TUserLogin;
import com.alex.user.token.service.TokenRefreshService;
import com.alex.user.user.entity.TUser;
import com.alex.user.user.mapper.TUserMapper;
import com.alex.user.user.service.TUserService;
import com.alex.user.utils.jwt.Audience;
import com.alex.user.utils.jwt.JwtTokenUtils;
import com.alex.user.utils.security.SecurityUserFactory;
import com.alex.utils.IpUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.enums.scope.AuthBaiduScope;
import me.zhyd.oauth.request.AuthBaiduRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeChatMpRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * <p>
 * <p>
 * description: 管理员表服务实现类
 * author: alex
 * createDate: 2022-12-26 17:20:38
 * version: 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUser> implements TUserService {

    private static final PasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();

    private final TUserMapper tUserMapper;

    private final RedisUtils redisUtils;

    private final JwtTokenUtils jwtTokenUtils;

    @Value(value = "${isRememberMeExpiresSecond}")
    private int isRememberMeExpiresSecond;

    private final Audience audience;

    private final OssApi ossApi;

    @Value(value = "${defaultPassword}")
    private String defaultPassword;

    private final MenuInfoService menuInfoService;

    private final UserUtils userUtils;

    private final OrgUserInfoService orgUserInfoService;

    private final RoleUserInfoService roleUserInfoService;

    private final TokenRefreshService tokenRefreshService;

    private final OnlineUserService onlineUserService;

    @Qualifier("asyncTaskExecutor")
    private final Executor asyncTaskExecutor;

    private final UserPermissionContextService userPermissionContextService;

    @Autowired(required = false)
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    public Page<TUserVo> getPage(Long pageNum, Long pageSize, TUserVo tUserVo) throws Exception {
        TUserVo curUser = userUtils.getLoginUser();
        log.info("当前用户:{}", curUser.getNickName());
        Page<TUserVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        Page<TUserVo> userPage = tUserMapper.getPage(page, tUserVo);
        List<TUserVo> records = userPage.getRecords();
        if (records == null || records.isEmpty()) {
            return userPage;
        }
        setAvatarUrls(records);
        return userPage;
    }

    @Override
    public TUserVo queryTUser(String id) {
        TUserVo user = tUserMapper.queryTUser(id);
        setAvatarUrls(user);
        if (user != null && user.getId() != null) {
            applyPermissionContext(user, userPermissionContextService.buildContext(user.getId()));
            user.setOrgId(user.getOrgInfoVo() == null ? null : user.getOrgInfoVo().getId());
            user.setRoleIds(user.getRoleInfoVoList() == null ? Collections.emptyList() :
                    user.getRoleInfoVoList().stream().map(RoleInfoVo::getId).toList());
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TUser addTUser(TUserVo tUserVo) {
        Map<String, Object> map = getStringObjectMap(tUserVo);
        //校验username,mobile,email
        judgeField(map, null);
        TUser tUser = new TUser();
        BeanUtils.copyProperties(tUserVo, tUser);
        String password = tUserVo.getPassword() == null ? defaultPassword : tUserVo.getPassword();
        tUser.setPassword(BCRYPT_ENCODER.encode(password + tUserVo.getUsername()));
        tUserMapper.insert(tUser);
        syncUserRbacAssignments(tUser.getId(), tUserVo);
        return tUser;
    }

    @NotNull
    private static Map<String, Object> getStringObjectMap(TUserVo tUserVo) {
        String username = tUserVo.getUsername();
        String mobile = tUserVo.getMobile();
        String email = tUserVo.getEmail();
        if (StringUtils.isEmpty(email) && StringUtils.isEmpty(mobile)) {
            throw new UserException(ResultEnum.USER_NO_MOBILE_EMAIL);
        }
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotEmpty(username)) {
            map.put(SysConf.USERNAME, username);
        }
        if (StringUtils.isNotEmpty(email)) {
            map.put(SysConf.EMAIL, email);
        }
        if (StringUtils.isNotEmpty(mobile)) {
            map.put(SysConf.MOBILE, mobile);
        }
        return map;
    }

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "12345";
        String pass = encoder.encode(password + "Bxh");
        System.out.println(pass);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TUser updateTUser(TUserVo tUserVo) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotEmpty(tUserVo.getUsername())) {
            map.put(SysConf.USERNAME, tUserVo.getUsername());
        }
        if (StringUtils.isNotEmpty(tUserVo.getEmail())) {
            map.put(SysConf.EMAIL, tUserVo.getEmail());
        }
        if (StringUtils.isNotEmpty(tUserVo.getMobile())) {
            map.put(SysConf.MOBILE, tUserVo.getMobile());
        }
        //校验username,mobile,email
        judgeField(map, tUserVo.getId());
        TUser tUser = new TUser();
        BeanUtils.copyProperties(tUserVo, tUser);
        tUserMapper.updateById(tUser);
        syncUserRbacAssignments(tUser.getId(), tUserVo);
        return tUser;
    }

    private void syncUserRbacAssignments(Long userId, TUserVo tUserVo) {
        if (userId == null || tUserVo == null) {
            return;
        }
        if (tUserVo.getOrgId() != null) {
            orgUserInfoService.assignSingleOrg(userId, tUserVo.getOrgId());
        }
        if (tUserVo.getRoleIds() != null) {
            roleUserInfoService.assignRoles(userId, tUserVo.getRoleIds());
        }
        // 清理权限上下文缓存
        try {
            redisUtils.delete(LoginKey.loginKey, "permission_context:" + userId);
            log.info("清理用户 {} 的权限上下文缓存", userId);
        } catch (Exception e) {
            log.error("清理用户权限上下文缓存异常，userId: {}", userId, e);
        }
    }

    @Override
    public Boolean deleteTUser(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return true;
        }
        List<String> idArr = Arrays.asList(ids.split(","));
        tUserMapper.deleteBatchIds(idArr);
        return true;
    }

    @Override
    public Map<String, Object> login(HttpServletRequest request, String username, String password, Boolean isRemember) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("1.参数校验与Redis检查");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new LoginException(ResultEnum.USER_USERNAME_OR_PASSWORD_EMPTY);
        }
        String ip = IpUtils.getIpAddr(request);
        
        // 并行从远端 Redis 异步读取校验限制次数与登录信息，消减公网网络 RTT 延迟开销
        final String finalIp = ip;
        CompletableFuture<String> limitCountFuture = CompletableFuture.supplyAsync(() -> 
            redisUtils.get(LoginKey.loginLimitCount.getPrefix() + RedisConstants.SEGMENTATION + finalIp + RedisConstants.SEGMENTATION + username), 
            asyncTaskExecutor);
            
        CompletableFuture<TUserVo> redisUserFuture = CompletableFuture.supplyAsync(() -> 
            redisUtils.get(LoginKey.loginAdmin, finalIp + RedisConstants.SEGMENTATION + username, TUserVo.class), 
            asyncTaskExecutor);
            
        CompletableFuture.allOf(limitCountFuture, redisUserFuture).join();
        
        String limitCount = null;
        TUserVo redisUser = null;
        try {
            limitCount = limitCountFuture.get();
            redisUser = redisUserFuture.get();
        } catch (Exception e) {
            log.error("并行读取 Redis 校验数据异常，进行安全降级", e);
        }
        
        Map<String, Object> result = new HashMap<>(RedisConstants.NUM_ONE);
        // 如果登录错误超过5次限制，抛出异常
        if (limitCount != null && !limitCount.isBlank()
                && Integer.parseInt(limitCount) >= RedisConstants.NUM_FIVE) {
            throw new LoginException(ResultEnum.USER_LOGIN_ERROR_MORE);
        }
        // 检查 Redis 是否命中，并验证 Token。注意由于 headers 参数不在 CompletableFuture 闭包内，我们可直接在主线程安全使用它
        String headers = request.getHeader(audience.getTokenHeader());
        if (redisUser != null && StringUtils.isNotBlank(headers) && authToken(headers)) {
            // 更新 token过期时间
            long expiration = isRemember != null && isRemember ? isRememberMeExpiresSecond : audience.getExpiresSecond();
            refreshLoginPermissionContext(redisUser);

            // headers 为 uuidToken，先取出其对应的 barToken(jwt)
            String barToken = redisUtils.get(LoginKey.loginUuid, headers, String.class);
            if (StringUtils.isBlank(barToken)) {
                // 缺少映射时无法安全复用，进入正常登录流程重新签发
                log.warn("检测到缺失 loginUuid 映射，降级为重新登录流程，username:{}", username);
            } else {
                final String finalBarToken = barToken;
                final TUserVo finalRedisUser = redisUser;
                final String finalLoginIp = ip;
                
                // 并行异步更新三大 Token 映射的过期时间，消减多段网络 RTT 的累积延迟
                CompletableFuture<Void> updateAdminFuture = CompletableFuture.runAsync(() -> 
                    redisUtils.setEx(LoginKey.loginAdmin, finalLoginIp + RedisConstants.SEGMENTATION + username, JSONObject.toJSONString(finalRedisUser, SerializerFeature.DisableCircularReferenceDetect), expiration, TimeUnit.SECONDS), 
                    asyncTaskExecutor);
                    
                CompletableFuture<Void> updateUuidFuture = CompletableFuture.runAsync(() -> 
                    redisUtils.setEx(LoginKey.loginUuid, headers, finalBarToken, expiration, TimeUnit.SECONDS), 
                    asyncTaskExecutor);
                    
                CompletableFuture<Void> updateTokenFuture = CompletableFuture.runAsync(() -> 
                    redisUtils.setEx(LoginKey.loginToken, finalBarToken, JSONObject.toJSONString(finalRedisUser, SerializerFeature.DisableCircularReferenceDetect), expiration, TimeUnit.SECONDS), 
                    asyncTaskExecutor);
                    
                CompletableFuture.allOf(updateAdminFuture, updateUuidFuture, updateTokenFuture).join();

                log.info("用户 {} 已登录，更新token过期时间，新过期时间：{} 秒", username, expiration);
                result.put(SysConf.TOKEN, headers);
                result.put(SysConf.ADMIN, redisUser);
                return result;
            }
        }
        stopWatch.stop();

        stopWatch.start("2.数据库查询用户");
        TUser admin;

        // 1. 优先使用 username 精确查询（此字段拥有索引 user_uesrname_index 与 t_username_status_IDX，查询极其高效）
        LambdaQueryWrapper<TUser> queryByUsername = Wrappers.<TUser>lambdaQuery()
                .eq(TUser::getStatus, EStatus.ENABLE.getCode())
                .eq(TUser::getUsername, username)
                .last(SysConf.LIMIT_ONE);
        admin = this.getOne(queryByUsername);

        // 2. 如果未查到，且输入包含 '@' 符号，通常为邮箱登录，使用 email 查询
        if (admin == null && username.contains("@")) {
            LambdaQueryWrapper<TUser> queryByEmail = Wrappers.<TUser>lambdaQuery()
                    .eq(TUser::getStatus, EStatus.ENABLE.getCode())
                    .eq(TUser::getEmail, username)
                    .last(SysConf.LIMIT_ONE);
            admin = this.getOne(queryByEmail);
        }

        // 3. 如果未查到，且输入为 11 位纯数字，通常为手机号登录，使用 mobile 查询
        if (admin == null && username.length() == 11 && username.matches("\\d+")) {
            LambdaQueryWrapper<TUser> queryByMobile = Wrappers.<TUser>lambdaQuery()
                    .eq(TUser::getStatus, EStatus.ENABLE.getCode())
                    .eq(TUser::getMobile, username)
                    .last(SysConf.LIMIT_ONE);
            admin = this.getOne(queryByMobile);
        }

        // 4. 兜底策略：如果是其它不合常规的输入，使用原来的 union/or 条件进行模糊/全面匹配
        if (admin == null) {
            LambdaQueryWrapper<TUser> fallbackQuery = Wrappers.<TUser>lambdaQuery()
                    .eq(TUser::getStatus, EStatus.ENABLE.getCode())
                    .last(SysConf.LIMIT_ONE);
            fallbackQuery.and(qr -> qr.eq(TUser::getEmail, username).or().eq(TUser::getMobile, username).or().eq(TUser::getUsername, username));
            admin = this.getOne(fallbackQuery);
        }

        if (admin == null) {
            //设置错误登录次数
            throw new LoginException(ResultEnum.USER_LOGIN_ERROR_MORE.getCode(), String.format(MessageConf.LOGIN_ERROR, setLoginCommit(request, username)));
        }
        stopWatch.stop();

        stopWatch.start("3.BCrypt密码校验");
        //对密码进行加盐加密验证，采用SHA-256 + 随机盐【动态加盐】 + 密钥对密码进行加密
        boolean isPassword = BCRYPT_ENCODER.matches(password + admin.getUsername(), admin.getPassword());
        if (!isPassword) {
            //密码错误，返回提示信息
            throw new LoginException(ResultEnum.USER_LOGIN_ERROR_MORE.getCode(), String.format(MessageConf.LOGIN_ERROR, setLoginCommit(request, username)));
        }
        stopWatch.stop();

        stopWatch.start("4.保存登录日志与JWT签发");
        String uuid = StringUtils.getUUID();
        result.put(SysConf.TOKEN, uuid);
        //保存登录信息
        TUserLogin userLogin = saveLoginLog(request, admin, uuid, ip, isRemember);
        //不返回密码到前端
        TUserVo tUserVo = new TUserVo();
        BeanUtils.copyProperties(admin, tUserVo, "password");
        // 在维护一张表，用于 uuid - token 互相转换
        long expiration = isRemember != null && isRemember ? isRememberMeExpiresSecond : audience.getExpiresSecond();
        redisUtils.setEx(LoginKey.loginUuid, userLogin.getTokenId(), userLogin.getToken(), expiration, TimeUnit.SECONDS);
        stopWatch.stop();

        stopWatch.start("5.异步获取头像");
        // 获取机构信息
        final TUser finalAdmin = admin;
        CompletableFuture<Void> avatarFuture = CompletableFuture.runAsync(() -> {
            if (finalAdmin.getAvatar() != null) {
                setAvatarUrls(tUserVo);
            }
        }, asyncTaskExecutor).exceptionally(ex -> {
            log.error("异步获取头像信息发生错误", ex);
            return null;
        });
        stopWatch.stop();

        stopWatch.start("6.构建权限上下文(buildContext)");
        // 移回主登录线程同步构建，避开子线程首次加载 MyBatis SQL 映射时产生的锁争用与阻塞
        UserPermissionContextVo permissionContext = userPermissionContextService.buildContext(tUserVo.getId());
        stopWatch.stop();

        stopWatch.start("7.等待头像与装配权限");
        completeLoginResponse(tUserVo, avatarFuture, permissionContext);
        stopWatch.stop();

        stopWatch.start("8.Redis缓存写入");
        try {
            String userJson = objectMapper.writeValueAsString(tUserVo);
            final String finalUserJson = userJson;
            final String finalWriteIp = ip;
            
            // 并行异步写入远程 Redis 节点，将两次串行写网络 RTT 及带宽限制时间缩短一半
            CompletableFuture<Void> writeAdminFuture = CompletableFuture.runAsync(() -> 
                redisUtils.setEx(LoginKey.loginAdmin, finalWriteIp + RedisConstants.SEGMENTATION + username, finalUserJson, expiration, TimeUnit.SECONDS), 
                asyncTaskExecutor);
                
            CompletableFuture<Void> writeTokenFuture = CompletableFuture.runAsync(() -> 
                redisUtils.setEx(LoginKey.loginToken, userLogin.getToken(), finalUserJson, expiration, TimeUnit.SECONDS), 
                asyncTaskExecutor);
                
            CompletableFuture.allOf(writeAdminFuture, writeTokenFuture).join();
        } catch (Exception e) {
            log.error("并行写入用户 Redis 缓存失败", e);
            // 降级使用 FastJSON
            String userJson = JSONObject.toJSONString(tUserVo, SerializerFeature.DisableCircularReferenceDetect);
            redisUtils.setEx(LoginKey.loginAdmin, ip + RedisConstants.SEGMENTATION + username, userJson, expiration, TimeUnit.SECONDS);
            redisUtils.setEx(LoginKey.loginToken, userLogin.getToken(), userJson, expiration, TimeUnit.SECONDS);
        }
        stopWatch.stop();

        result.put(SysConf.ADMIN, tUserVo);
        log.info("登录成功，耗时：\n{}, {} 秒", formatStopWatchInSeconds(stopWatch),
                formatNanosToSeconds(stopWatch.getTotalTimeNanos()));
        return result;
    }

    private static String formatStopWatchInSeconds(StopWatch stopWatch) {
        StringBuilder sb = new StringBuilder();
        sb.append("StopWatch '").append(stopWatch.getId()).append("': running time = ")
                .append(formatNanosToSeconds(stopWatch.getTotalTimeNanos())).append(" s");
        if (stopWatch.getTaskCount() > 0) {
            sb.append('\n');
            sb.append("---------------------------------------------\n");
            sb.append("s          %     Task name\n");
            sb.append("---------------------------------------------\n");
            long totalNanos = stopWatch.getTotalTimeNanos();
            for (StopWatch.TaskInfo task : stopWatch.getTaskInfo()) {
                long taskNanos = task.getTimeNanos();
                int percent = totalNanos > 0 ? (int) Math.round(100.0 * taskNanos / totalNanos) : 0;
                sb.append(String.format("%11s", formatNanosToSeconds(taskNanos)))
                        .append("  ")
                        .append(String.format("%03d", percent))
                        .append("%  ")
                        .append(task.getTaskName())
                        .append('\n');
            }
        }
        return sb.toString();
    }

    private static String formatNanosToSeconds(long nanos) {
        long seconds = nanos / 1_000_000_000L;
        long remainder = nanos % 1_000_000_000L;
        if (seconds == 0) {
            return String.format("0.%09d", remainder);
        }
        return String.format("%d.%09d", seconds, remainder);
    }

    public TUserVo refreshLoginPermissionContext(TUserVo userVo) {
        if (userVo == null || userVo.getId() == null) {
            return userVo;
        }
        applyPermissionContext(userVo, userPermissionContextService.buildContext(userVo.getId()));
        return userVo;
    }

    public static void completeLoginResponse(TUserVo userVo, CompletableFuture<Void> avatarFuture,
                                             UserPermissionContextVo permissionContext) {
        if (avatarFuture != null) {
            try {
                // 设置最大800毫秒的超时时间，防止微服务冷启动或RPC调用挂起阻塞登录接口
                avatarFuture.get(800, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.warn("获取用户头像信息超时，进行熔断降级，跳过头像URL装配");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UserException(ResultEnum.USER_GET_INFO_ERROR);
            } catch (Exception e) {
                log.error("获取用户头像发生异常，跳过头像URL装配", e);
            }
        }
        applyPermissionContext(userVo, permissionContext);
    }

    public static void applyPermissionContext(TUserVo userVo, UserPermissionContextVo context) {
        if (userVo == null || context == null) {
            return;
        }
        List<RoleInfoVo> roleList = context.getRoleList();
        userVo.setPermissionContext(context);
        userVo.setOrgInfoVo(context.getOrgInfo());
        userVo.setRoleInfoVoList(roleList);
        OrgInfoVo orgInfo = context.getOrgInfo();
        userVo.setOrgName(orgInfo == null ? null : orgInfo.getOrgName());
        userVo.setOrgCode(orgInfo == null ? null : orgInfo.getOrgCode());
        userVo.setPermissionCodes(context.getPermissionCodes());
        userVo.setButtonPermissionCodes(context.getButtonPermissionCodes());
        userVo.setMenuInfoVoList(context.getMenuList());
    }

    private TUserLogin saveLoginLog(HttpServletRequest request, TUser admin, String uuid, String ip, Boolean isRemember) {
        String roleName = "";
        long expiration = isRemember != null && isRemember ? isRememberMeExpiresSecond : audience.getExpiresSecond();
        String jwtToken = jwtTokenUtils.createJwt(admin.getUsername(), admin.getId(), roleName, audience.getClientId(), audience.getName()
                , expiration * 1000, audience.getBase64Secret());
        Map<String, String> map = new HashMap<>();
        String location = null;
        try {
            map = IpUtils.getOsAndBrowserInfo(request);
            location = IpUtils.getCityInfo(ip);
            log.info("ip:{},地址：{}", ip, location);
        } catch (Exception e) {
            log.error("获取ip地址和设备信息失败：{}", e.getMessage());
        }
        String token = audience.getTokenHead() + jwtToken;
        String os = map != null ? map.get(SysConf.OS) : "Unknown";
        String browser = map != null ? map.get(SysConf.BROWSER) : "Unknown";
        TUserLogin userLogin = TUserLogin.builder()
                .userId(admin.getId())
                .username(admin.getUsername())
                .nickName(admin.getNickName())
                .lastLoginTime(LocalDateTime.now())
                .tokenId(uuid)
                .token(token)
                .os(os)
                .broswer(browser)
                .loginIp(ip)
                .loginLocation(location)
                .build();
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        asyncTaskExecutor.execute(() -> {
            try {
                RequestContextHolder.setRequestAttributes(attributes);
                userLogin.insert();
            } catch (Exception e) {
                log.error("异步保存登录日志失败：{}", e.getMessage());
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });
        // 异步添加在线用户到 redis中
        onlineUserService.addOnlineUserAsync(userLogin, expiration);
        // 设置认证信息到 SecurityContextHolder
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userLogin, userLogin, new ArrayList<>());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        return userLogin;
    }

    /**
     * param request
     * param username 登录名称
     * description: 设置登录限制，返回剩余次数
     * author: alex
     * return: java.lang.Integer
     */
    private Integer setLoginCommit(HttpServletRequest request, String username) throws Exception {
        String ip = IpUtils.getIpAddr(request);
        String loginCountKey = LoginKey.loginLimitCount.getPrefix() + RedisConstants.SEGMENTATION + ip +
                RedisConstants.SEGMENTATION + username;
        String count = redisUtils.get(loginCountKey);
        int surplusCount = RedisConstants.NUM_FIVE;
        int exTime = 30;
        if (StringUtils.isNotEmpty(count)) {
            int curCount = Integer.parseInt(count) + 1;
            surplusCount -= curCount;
            redisUtils.setEx(loginCountKey, Integer.toString(curCount), exTime, TimeUnit.MINUTES);
        } else {
            surplusCount -= 1;
            redisUtils.setEx(loginCountKey, String.valueOf(RedisConstants.NUM_ONE), exTime, TimeUnit.MINUTES);
        }
        return surplusCount;
    }

    @Override
    public List<TUserVo> getList(TUserVo tUserVo) {
        List<TUserVo> records = tUserMapper.getList(tUserVo);
        if (records == null || records.isEmpty()) {
            return records;
        }
        setAvatarUrls(records);
        return records;
    }

    public TUserVo getUserInfo(TUserVo tUserVo) {
        TUserVo userInfo = tUserMapper.getUserInfo(tUserVo);
        if (userInfo == null || userInfo.getAvatar() == null) {
            return userInfo;
        }
        setAvatarUrls(userInfo);
        return userInfo;
    }

    @Override
    public TUserVo getUserByUsername(String username) {
        TUserVo tUserVo = new TUserVo();
        tUserVo.setUsername(username);
        return getUserInfo(tUserVo);
    }

    @Override
    public Result<Boolean> logout(HttpServletRequest request) {
        String uuidToken = request.getHeader(audience.getTokenHeader());
        if (StringUtils.isEmpty(uuidToken) || "undefined".equalsIgnoreCase(uuidToken) || "null".equalsIgnoreCase(uuidToken)) {
            return Result.error(ResultEnum.PARAM_ERROR);
        } else {
            // 获取在线用户信息
            String barToken = redisUtils.get(LoginKey.loginUuid, uuidToken);
            redisUtils.delete(LoginKey.loginUuid, uuidToken);
            // 移除Redis 中的用户
            redisUtils.delete(LoginKey.loginToken, barToken);
            SecurityContextHolder.clearContext();
            return Result.success();
        }
    }

    /**
     * param userLogin
     * param expiration
     * description: 添加在线用户
     * author:      alex
     * return:      void
     */
    @Override
    public void addOnLineAdmin(TUserLogin userLogin, long expiration) throws Exception {
        OnlineAdmin onlineAdmin = OnlineAdmin.builder()
                .userId(userLogin.getUserId())
                .tokenId(userLogin.getTokenId())
                .token(userLogin.getToken())
                .os(userLogin.getOs())
                .browser(userLogin.getBroswer())
                .ipaddr(userLogin.getLoginIp())
                .loginLocation(userLogin.getLoginLocation())
                .loginTime(DateUtils.getTimeStr(userLogin.getLastLoginTime()))
                .roleName(null)
                .username(userLogin.getUsername())
                .expireTime(DateUtils.getTimeStr(DateUtils.addTime(LocalDateTime.now(), expiration, ChronoUnit.SECONDS)))
                .build();
        //从 Redis中获取IP来源
        String jsonResult = redisUtils.get(LoginKey.loginIpSource, userLogin.getLoginIp());
        if (StringUtils.isEmpty(jsonResult)) {
            String addresses = IpUtils.getAddresses(SysConf.IP + "=" + userLogin.getLoginIp());
            if (StringUtils.isNotEmpty(addresses)) {
                onlineAdmin.setLoginLocation(addresses);
                redisUtils.setEx(LoginKey.loginIpSource, userLogin.getLoginIp(), addresses, expiration * 24, TimeUnit.SECONDS);
            }
        } else {
            onlineAdmin.setLoginLocation(jsonResult);
        }
    }

    private void judgeField(Map<String, Object> map, Long id) {
        if (map.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String type = entry.getKey();
            long count = judgeValueCount(type, entry.getValue(), id);
            if (count > 0) {
                switch (type) {
                    case "username":
                        throw new UserException(ResultEnum.USER_USERNAME_EXISTS);
                    case "mobile":
                        throw new UserException(ResultEnum.USER_MOBILE_EXISTS);
                    case "email":
                        throw new UserException(ResultEnum.USER_EMAIL_EXISTS);
                }
                return;
            }
        }
    }

    /**
     * param type
     * param value
     * param id
     * description: 判断字段对应的值在数据库中的数量
     * author: alex
     * return: boolean
     */
    private long judgeValueCount(String type, Object value, Long id) {
        if (StringUtils.isEmpty(type)) {
            return 0;
        }
        LambdaQueryWrapper<TUser> query = Wrappers.<TUser>lambdaQuery().eq(TUser::getIsDelete, 0);
        if (id != null) {
            query.ne(TUser::getId, id);
        }
        switch (type) {
            case "username":
                query.eq(TUser::getUsername, value);
                break;
            case "mobile":
                query.eq(TUser::getMobile, value);
                break;
            case "email":
                query.eq(TUser::getEmail, value);
                break;
            default:
                throw new UserException("400", "请输入正确的字段");
        }
        return this.count(query);
    }

    private void setAvatarUrls(TUserVo userVo) {
        if (userVo == null || userVo.getAvatar() == null) {
            return;
        }
        try {
            Result<List<FileInfoVo>> fileInfo = ossApi.getFileInfo(Lists.newArrayList(userVo.getAvatar()));
            if (fileInfo != null && SysConf.RESULT_SUCCESS.equals(fileInfo.getCode()) && fileInfo.getData() != null && !fileInfo.getData().isEmpty()) {
                FileInfoVo vo = fileInfo.getData().get(0);
                userVo.setAvatarUrl(vo.getPreUrl());
                userVo.setAvatarThumbnailUrl(vo.getPreThumbnailUrl());
            }
        } catch (Exception e) {
            log.error("获取头像文件错误：{}", e.getMessage());
        }
    }

    private void setAvatarUrls(Collection<TUserVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> fileIdList = records.parallelStream()
                .map(TUserVo::getAvatar)
                .filter(Objects::nonNull)
                .toList();
        if (fileIdList.isEmpty()) {
            return;
        }
        try {
            Result<List<FileInfoVo>> result = ossApi.getFileInfo(fileIdList);
            if (result != null && SysConf.RESULT_SUCCESS.equals(result.getCode()) && result.getData() != null && !result.getData().isEmpty()) {
                Map<Long, List<FileInfoVo>> fileMap = result.getData()
                        .parallelStream()
                        .collect(Collectors.groupingBy(FileInfoVo::getId));
                records.forEach(item -> {
                    List<FileInfoVo> fileInfoVos = fileMap.get(item.getAvatar());
                    if (fileInfoVos != null && !fileInfoVos.isEmpty()) {
                        FileInfoVo fileInfoVo = fileInfoVos.get(0);
                        item.setAvatarUrl(fileInfoVo.getPreUrl());
                        item.setAvatarThumbnailUrl(fileInfoVo.getPreThumbnailUrl());
                    }
                });
            }
        } catch (Exception e) {
            log.error("批量获取用户头像失败！", e);
        }
    }

    @Override
    public Boolean authToken(String uuidToken) {
        if (StringUtils.isEmpty(uuidToken) || "undefined".equalsIgnoreCase(uuidToken) || "null".equalsIgnoreCase(uuidToken)) {
            return false;
        }
        String barToken = redisUtils.get(LoginKey.loginUuid, uuidToken);
        if (StringUtils.isEmpty(barToken) || !barToken.startsWith(audience.getTokenHead())) {
            return false;
        }
        // 私钥
        String base64Secret = audience.getBase64Secret();
        String token = barToken.substring(audience.getTokenHead().length());
        //校验 token
        if (StringUtils.isEmpty(token) || jwtTokenUtils.isExpiration(token, base64Secret)) {
            return false;
        }
        // 异步刷新token，不阻塞主流程
        tokenRefreshService.refreshTokenAsync(token, base64Secret, uuidToken, barToken);
        // 获取在线的管理员信息
        String username = jwtTokenUtils.getUsername(token, base64Secret);
        SecurityContextHolder.getContext().getAuthentication();
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 优先从 Redis 缓存中获取已登录的用户 VO 组装 UserDetails
            TUserVo userVo = redisUtils.get(LoginKey.loginToken, barToken, TUserVo.class);
            if (userVo == null) {
                log.info("未从缓存 loginToken 中找到用户 {}, 降级为数据库查询", username);
                userVo = getUserByUsername(username);
            }
            // 通过用户名加载 SpringSecurity用户
            UserDetails userDetails = SecurityUserFactory.create(userVo);
            // 校验 Token的有效性
            if (jwtTokenUtils.validateToken(token, userDetails, base64Secret)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                // 以后可以 security中取得SecurityUser信息
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        return true;
    }



    public AuthRequest getAuthRequest(String appName) {
        return switch (appName) {
            case "wechat_mp" -> new AuthWeChatMpRequest(AuthConfig.builder()
                    .clientId("wxec93a0ddb72c8cff")
                    .clientSecret("1240434ae0be6dc4b0ba979d7c1f9b7a")
                    .redirectUri("https://mjzp.xyz/login")
                    .build());
            case "baidu" -> new AuthBaiduRequest(AuthConfig.builder()
                    .clientId("w7kcpHna8w8irDiMA4tdnnnQ")
                    .clientSecret("8LTOzDkpVv5LkzPR9yyptsq7MMENyCVS")
                    .redirectUri("https://mjzp.xyz/login")
                    .scopes(Arrays.asList(
                            AuthBaiduScope.BASIC.getScope(),
                            AuthBaiduScope.SUPER_MSG.getScope(),
                            AuthBaiduScope.NETDISK.getScope()
                    ))
                    .build());
            default -> null;
        };
    }
}

