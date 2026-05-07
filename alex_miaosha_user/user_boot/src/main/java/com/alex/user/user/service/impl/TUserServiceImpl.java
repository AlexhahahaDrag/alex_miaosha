package com.alex.user.user.service.impl;

import com.alex.api.oss.fileInfo.api.OssApi;
import com.alex.api.oss.fileInfo.vo.FileInfoVo;
import com.alex.api.user.menuInfo.vo.MenuInfoVo;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

    private final UserPermissionContextService userPermissionContextService;

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
        List<Long> fileIdList = records.parallelStream()
                .map(TUserVo::getAvatar)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        try {
            Result<List<FileInfoVo>> result = ossApi.getFileInfo(fileIdList);
            if (SysConf.RESULT_SUCCESS.equals(result.getCode()) && result.getData() != null && !result.getData().isEmpty()) {
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
            log.error("获取用户头像失败！");
        }
        return userPage;
    }

    @Override
    public TUserVo queryTUser(String id) {
        TUserVo user = tUserMapper.queryTUser(id);
        setAvatarUrls(user);
        return user;
    }

    @Override
    public TUser addTUser(TUserVo tUserVo) {
        Map<String, Object> map = getStringObjectMap(tUserVo);
        //校验username,mobile,email
        judgeField(map, null);
        TUser tUser = new TUser();
        BeanUtils.copyProperties(tUserVo, tUser);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = tUserVo.getPassword() == null ? defaultPassword : tUserVo.getPassword();
        tUser.setPassword(encoder.encode(password + tUserVo.getUsername()));
        tUserMapper.insert(tUser);
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
        return tUser;
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
        stopWatch.start("开始登录");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new LoginException(ResultEnum.USER_USERNAME_OR_PASSWORD_EMPTY);
        }
        String ip = IpUtils.getIpAddr(request);
        String limitCount = redisUtils.get(LoginKey.loginLimitCount.getPrefix() + RedisConstants.SEGMENTATION + ip + RedisConstants.SEGMENTATION + username);
        if (StringUtils.isNotEmpty(limitCount) && Integer.parseInt(limitCount) >= RedisConstants.NUM_FIVE) {
            throw new LoginException(ResultEnum.USER_LOGIN_ERROR_MORE);
        }
        TUserVo redisUser = redisUtils.get(LoginKey.loginAdmin, ip + RedisConstants.SEGMENTATION + username, TUserVo.class);
        Map<String, Object> result = new HashMap<>(RedisConstants.NUM_ONE);
        String headers = request.getHeader(audience.getTokenHeader());
        if (redisUser != null && StringUtils.isNotBlank(headers) && authToken(headers)) {
            // 更新 token过期时间
            long expiration = isRemember != null && isRemember ? isRememberMeExpiresSecond : audience.getExpiresSecond();
            refreshLoginPermissionContext(redisUser);

            // 重新设置 Redis中相关key的值和过期时间
            redisUtils.setEx(LoginKey.loginAdmin, ip + RedisConstants.SEGMENTATION + username, JSONObject.toJSONString(redisUser), expiration, TimeUnit.SECONDS);
            redisUtils.setEx(LoginKey.loginToken, headers, JSONObject.toJSONString(redisUser), expiration, TimeUnit.SECONDS);

            // 获取 token对应的uuid并更新过期时间
            String tokenId = redisUtils.get(LoginKey.loginUuid, headers, String.class);
            if (StringUtils.isNotBlank(tokenId)) {
                redisUtils.setEx(LoginKey.loginUuid, tokenId, headers, expiration, TimeUnit.SECONDS);
            }

            log.info("用户 {} 已登录，更新token过期时间，新过期时间：{} 秒", username, expiration);
            result.put(SysConf.TOKEN, headers);
            result.put(SysConf.ADMIN, redisUser);
            return result;
        }
        LambdaQueryWrapper<TUser> query = Wrappers.<TUser>lambdaQuery()
                .eq(TUser::getStatus, EStatus.ENABLE.getCode())
                .last(SysConf.LIMIT_ONE);
        query.and(qr -> qr.eq(TUser::getEmail, username).or().eq(TUser::getMobile, username).or().eq(TUser::getUsername, username));
        TUser admin = this.getOne(query);
        if (admin == null) {
            //设置错误登录次数
            throw new LoginException(ResultEnum.USER_LOGIN_ERROR_MORE.getCode(), String.format(MessageConf.LOGIN_ERROR, setLoginCommit(request, username)));
        }
        //对密码进行加盐加密验证，采用SHA-256 + 随机盐【动态加盐】 + 密钥对密码进行加密
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean isPassword = encoder.matches(password + admin.getUsername(), admin.getPassword());
        if (!isPassword) {
            //密码错误，返回提示信息
            throw new LoginException(ResultEnum.USER_LOGIN_ERROR_MORE.getCode(), String.format(MessageConf.LOGIN_ERROR, setLoginCommit(request, username)));
        }
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

        // 获取机构信息
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> avatarFuture = CompletableFuture.runAsync(() -> {
            if (admin.getAvatar() != null) {
                setAvatarUrls(tUserVo);
            }
        }).exceptionally(ex -> {
            log.error("异步获取头像信息发生错误", ex);
            return null;
        });
        CompletableFuture<UserPermissionContextVo> permissionContextFuture = CompletableFuture.supplyAsync(() -> {
            try {
                if (attributes != null) {
                    RequestContextHolder.setRequestAttributes(attributes);
                }
                return userPermissionContextService.buildContext(tUserVo.getId());
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });
        completeLoginResponse(tUserVo, avatarFuture, permissionContextFuture);
        redisUtils.setEx(LoginKey.loginAdmin, ip + RedisConstants.SEGMENTATION + username, JSONObject.toJSONString(tUserVo), expiration, TimeUnit.SECONDS);
        // 将登录的管理员存储到在线用户表
        redisUtils.setEx(LoginKey.loginToken, userLogin.getToken(), JSONObject.toJSONString(tUserVo), expiration, TimeUnit.SECONDS);
        result.put(SysConf.ADMIN, tUserVo);
        stopWatch.stop();
        log.info("登录成功，耗时：{}, {} 毫秒", stopWatch.prettyPrint(), stopWatch.getTotalTimeMillis());
        return result;
    }

    public TUserVo refreshLoginPermissionContext(TUserVo userVo) {
        if (userVo == null || userVo.getId() == null) {
            return userVo;
        }
        applyPermissionContext(userVo, userPermissionContextService.buildContext(userVo.getId()));
        return userVo;
    }

    public static void completeLoginResponse(TUserVo userVo, CompletableFuture<Void> avatarFuture,
                                             CompletableFuture<UserPermissionContextVo> permissionContextFuture) {
        try {
            if (avatarFuture != null) {
                avatarFuture.get();
            }
            UserPermissionContextVo context = permissionContextFuture == null ? null : permissionContextFuture.get();
            applyPermissionContext(userVo, context);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UserException(ResultEnum.USER_GET_INFO_ERROR);
        } catch (ExecutionException e) {
            throw new UserException(ResultEnum.USER_GET_INFO_ERROR);
        }
    }

    public static void applyPermissionContext(TUserVo userVo, UserPermissionContextVo context) {
        if (userVo == null || context == null) {
            return;
        }
        List<RoleInfoVo> roleList = context.getRoleList();
        userVo.setPermissionContext(context);
        userVo.setOrgInfoVo(context.getOrgInfo());
        userVo.setRoleInfoVoList(roleList);
        RoleInfoVo firstRole = roleList == null || roleList.isEmpty() ? null : roleList.get(0);
        OrgInfoVo orgInfo = context.getOrgInfo();
        userVo.setRoleInfoVo(firstRole);
        userVo.setOrgName(orgInfo == null ? null : orgInfo.getOrgName());
        userVo.setOrgCode(orgInfo == null ? null : orgInfo.getOrgCode());
        userVo.setRoleName(firstRole == null ? null : firstRole.getRoleName());
        userVo.setRoleCode(firstRole == null ? null : firstRole.getRoleCode());
        userVo.setPermissionCodes(context.getPermissionCodes());
        userVo.setButtonPermissionCodes(context.getButtonPermissionCodes());
        userVo.setMenuInfoVoList(context.getMenuList());
    }

    private TUserLogin saveLoginLog(HttpServletRequest request, TUser admin, String uuid, String ip, Boolean isRemember) {
        String roleName = "";
        long expiration = isRemember != null && isRemember ? isRememberMeExpiresSecond : audience.getExpiresSecond();
        String jwtToken = jwtTokenUtils.createJwt(admin.getUsername(), admin.getId(), roleName, audience.getClientId(), audience.getName()
                , expiration * 1000, audience.getBase64Secret());
        Map<String, String> map = null;
        String location = null;
        try {
            map = IpUtils.getOsAndBrowserInfo(request);
            location = IpUtils.getCityInfo(ip);
            log.info("ip:{},地址：{}", ip, location);
        } catch (Exception e) {
            log.error("获取ip地址和设备信息失败：{}", e.getMessage());
        }
        String token = audience.getTokenHead() + jwtToken;
        assert map != null;
        String os = map.get(SysConf.OS);
        String browser = map.get(SysConf.BROWSER);
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
        new Thread(userLogin::insert);
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
        List<Long> fileIdList = records.parallelStream()
                .map(TUserVo::getAvatar)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!fileIdList.isEmpty()) {
            try {
                Result<List<FileInfoVo>> result = ossApi.getFileInfo(fileIdList);
                if (SysConf.RESULT_SUCCESS.equals(result.getCode()) && result.getData() != null && !result.getData().isEmpty()) {
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
                log.error("获取用户头像列表失败！", e);
            }
        }
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
        if (StringUtils.isEmpty(uuidToken)) {
            return Result.error(ResultEnum.PARAM_ERROR);
        } else {
            // 获取在线用户信息
            String barToken = redisUtils.get(LoginKey.loginUuid, uuidToken);
            redisUtils.delete(LoginKey.loginUuid, uuidToken);
            // 移除Redis中的用户
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
                .expireTime(DateUtils.getTimeStr(DateUtils.addTime(LocalDateTime.now(), expiration, ChronoUnit.MICROS)))
                .build();
        //从 Redis中获取IP来源
        String jsonResult = redisUtils.get(LoginKey.loginIpSource, userLogin.getLoginIp());
        if (StringUtils.isEmpty(jsonResult)) {
            String addresses = IpUtils.getAddresses(SysConf.IP + "=" + userLogin.getLoginIp());
            if (StringUtils.isNotEmpty(addresses)) {
                onlineAdmin.setLoginLocation(addresses);
                redisUtils.setEx(LoginKey.loginIpSource, userLogin.getLoginIp(), addresses, expiration * 24, TimeUnit.MICROSECONDS);
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

    private String getFileUrl(Long fileId) {
        if (fileId == null) {
            return null;
        }
        try {
            Result<List<FileInfoVo>> fileInfo = ossApi.getFileInfo(Lists.newArrayList(fileId));
            return Optional.ofNullable(fileInfo).map(item -> item.getData().get(0).getPreUrl()).orElse("");
        } catch (Exception e) {
            log.error("获取头像文件错误：{}", e.getMessage());
            return null;
        }
    }

    @Override
    public Boolean authToken(String uuidToken) {
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
            // 通过用户名加载 SpringSecurity用户
            UserDetails userDetails = SecurityUserFactory.create(getUserByUsername(username));
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

