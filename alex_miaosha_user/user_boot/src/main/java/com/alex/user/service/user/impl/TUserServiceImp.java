package com.alex.user.service.user.impl;

import cn.hutool.json.JSONUtil;
import com.alex.api.oss.api.OssApi;
import com.alex.api.oss.vo.fileInfo.FileInfoVo;
import com.alex.api.user.vo.menuInfo.MenuInfoVo;
import com.alex.api.user.vo.orgInfo.OrgInfoVo;
import com.alex.api.user.vo.roleInfo.RoleInfoVo;
import com.alex.api.user.vo.user.OnlineAdmin;
import com.alex.api.user.vo.user.TUserVo;
import com.alex.base.common.Result;
import com.alex.base.constants.SysConf;
import com.alex.base.enums.ResultEnum;
import com.alex.common.constants.message.MessageConf;
import com.alex.common.constants.redis.RedisConstants;
import com.alex.common.enums.EStatus;
import com.alex.common.exception.UserException;
import com.alex.common.redis.key.LoginKey;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.entity.tUserLogin.TUserLogin;
import com.alex.user.entity.user.TUser;
import com.alex.user.mapper.user.TUserMapper;
import com.alex.user.service.menuInfo.MenuInfoService;
import com.alex.user.service.orgUserInfo.OrgUserInfoService;
import com.alex.user.service.roleUserInfo.RoleUserInfoService;
import com.alex.user.service.user.TUserService;
import com.alex.user.utils.jwt.Audience;
import com.alex.user.utils.jwt.JwtTokenUtils;
import com.alex.user.utils.security.SecurityUserFactory;
import com.alex.api.user.user.UserUtils;
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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * description: 管理员表服务实现类
 * author: alex
 * createDate: 2022-12-26 17:20:38
 * version: 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TUserServiceImp extends ServiceImpl<TUserMapper, TUser> implements TUserService {

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

    private final Executor taskExecutor;

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
                        item.setAvatarUrl(fileInfoVos.get(0).getPreUrl());
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
        if (user != null && user.getAvatar() != null) {
            user.setAvatarUrl(getFileUrl(user.getAvatar()));
        }
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
        String password = "1234@com";
        String pass = encoder.encode(password + "mj");
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
    public Result<Object> login(HttpServletRequest request, String username, String password, Boolean isRemember) throws Exception {
        StopWatch stopWatch = new StopWatch();
        // TODO (majf) 2024/2/21 15:02 测试是否可以修改成CompletableFuture
        stopWatch.start("开始登录");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return Result.error(ResultEnum.USER_USERNAME_OR_PASSWORD_EMPTY);
        }
        String ip = IpUtils.getIpAddr(request);
        String limitCount = redisUtils.get(LoginKey.loginLimitCount.getPrefix() + RedisConstants.SEGMENTATION + ip + RedisConstants.SEGMENTATION + username);
        if (StringUtils.isNotEmpty(limitCount) && Integer.parseInt(limitCount) >= RedisConstants.NUM_FIVE) {
            return Result.error(ResultEnum.USER_LOGIN_ERROR_MORE);
        }
        LambdaQueryWrapper<TUser> query = Wrappers.<TUser>lambdaQuery()
                .eq(TUser::getStatus, EStatus.ENABLE.getCode())
                .last(SysConf.LIMIT_ONE);
        query.and(qr -> qr.eq(TUser::getEmail, username).or().eq(TUser::getMobile, username).or().eq(TUser::getUsername, username));
        TUser admin = this.getOne(query);
        if (admin == null) {
            //设置错误登录次数
            return Result.error(ResultEnum.USER_LOGIN_ERROR_MORE.getCode(), String.format(MessageConf.LOGIN_ERROR, setLoginCommit(request, username)));
        }
        //对密码进行加盐加密验证，采用SHA-256 + 随机盐【动态加盐】 + 密钥对密码进行加密
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean isPassword = encoder.matches(password + admin.getUsername(), admin.getPassword());
        if (!isPassword) {
            //密码错误，返回提示信息
            return Result.error(ResultEnum.USER_LOGIN_ERROR_MORE.getCode(), String.format(MessageConf.LOGIN_ERROR, setLoginCommit(request, username)));
        }
        Map<String, Object> result = new HashMap<>(RedisConstants.NUM_ONE);
        String uuid = StringUtils.getUUID();
        result.put(SysConf.TOKEN, uuid);
        //保存登录信息
        TUserLogin userLogin = saveLoginLog(request, admin, uuid, ip, isRemember);
        //不返回密码到前端
        TUserVo tUserVo = new TUserVo();
        BeanUtils.copyProperties(admin, tUserVo, "password");
        // 获取机构信息
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<String> avatarFuture = CompletableFuture.supplyAsync(() -> {
            if (admin.getAvatar() == null) {
                return null;
            }
            log.info("异步获取组织架构信息");
            return getFileUrl(admin.getAvatar());
        }).exceptionally(ex -> {
            log.error("异步获取组织架构信息发生错误", ex);
            return null; // 返回一个空列表或合适的错误处理
        });
        CompletableFuture<List<OrgInfoVo>> orgInfoFuture = CompletableFuture.supplyAsync(() -> {
            log.info("异步获取组织架构信息");
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrgInfoVo> orgInfoList = orgUserInfoService.getOrgInfoList(tUserVo.getId());
            log.info("异步获取组织架构信息{}", JSONObject.toJSONString(orgInfoList));
            return orgInfoList;
        }).exceptionally(ex -> {
            log.error("异步获取组织架构信息发生错误", ex);
            return Collections.emptyList(); // 返回一个空列表或合适的错误处理
        });
        CompletableFuture<List<RoleInfoVo>> rolesFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            return roleUserInfoService.getRoleInfoList(tUserVo.getId(), true);
        });
        MenuInfoVo menuInfoVo = new MenuInfoVo();
        menuInfoVo.setStatus(SysConf.VALID_STATUS);
        CompletableFuture<List<MenuInfoVo>> menuFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            return menuInfoService.getList(menuInfoVo);
        });
        // 等待所有的异步操作完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(orgInfoFuture, rolesFuture, menuFuture);
        // 当所有的异步操作完成后，可以做一些处理
        allFutures.thenRun(() -> {
            try {
                List<OrgInfoVo> orgInfoList = orgInfoFuture.get(); // 获取用户信息结果
                List<RoleInfoVo> roleInfoList = rolesFuture.get();// 获取用户角色信息结果
                List<MenuInfoVo> menuList = menuFuture.get(); // 获取权限信息结果
                // 根据需要处理这些信息
                log.info("用户信息: " + orgInfoList);
                log.info("角色信息: " + roleInfoList);
                log.info("权限信息: " + menuList);
                tUserVo.setAvatarUrl(avatarFuture.get());
                tUserVo.setOrgInfoVo(orgInfoList == null || orgInfoList.isEmpty() ? null : orgInfoList.get(0));
                tUserVo.setRoleInfoVo(roleInfoList == null || roleInfoList.isEmpty() ? null : roleInfoList.get(0));
                tUserVo.setMenuInfoVoList(menuList);
                long expiration = isRemember != null && isRemember ? isRememberMeExpiresSecond : audience.getExpiresSecond();
                redisUtils.setEx(LoginKey.loginAdmin, userLogin.getToken(), JSONObject.toJSONString(tUserVo), expiration, TimeUnit.SECONDS);
                result.put(SysConf.ADMIN, tUserVo);
            } catch (InterruptedException | ExecutionException e) {
                throw new UserException(ResultEnum.USER_GET_INFO_ERROR);
            }
        });
        // 等待最后的处理完成
        allFutures.join();
        
//// 获取机构信息
//        List<OrgInfoVo> orgInfoList = orgUserInfoService.getOrgInfoList(tUserVo.getId());
//        tUserVo.setOrgInfoVo(orgInfoList == null || orgInfoList.isEmpty() ? null : orgInfoList.get(0));
//        // 获取角色信息
//        List<RoleInfoVo> roleInfoList = roleUserInfoService.getRoleInfoList(tUserVo.getId(), true);
//        tUserVo.setRoleInfoVo(roleInfoList == null || roleInfoList.isEmpty() ? null : roleInfoList.get(0));
//        // 获取菜单
//        MenuInfoVo menuInfoVo = new MenuInfoVo();
//        menuInfoVo.setStatus(SysConf.VALID_STATUS);
//        List<MenuInfoVo> menuList = menuInfoService.getList(null);
//        tUserVo.setMenuInfoVoList(menuList);
//        result.put(SysConf.MENU, menuList);
//        long expiration = isRemember != null && isRemember ? isRememberMeExpiresSecond : audience.getExpiresSecond();
//        redisUtils.setEx(LoginKey.loginAdmin, userLogin.getToken(), JSONUtil.toJsonStr(tUserVo), expiration, TimeUnit.SECONDS);
//        result.put(SysConf.ADMIN, tUserVo);

        stopWatch.stop();
        log.info("登录成功，耗时：{}, {} 毫秒", stopWatch.prettyPrint(), stopWatch.getTotalTimeMillis());
        return Result.success(result);
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
            log.info("ip:{}", ip);
            log.info("地址：{}", location);
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
        taskExecutor.execute(() -> {
            // 异步执行的代码
            try {
                //添加在线用户到redis中，设置过期时间
                this.addOnLineAdmin(userLogin, expiration);
            } catch (Exception e) {
                log.error("添加在线用户失败：{}", e.getMessage());
            }
        });
        // 设置认证信息到SecurityContextHolder
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
    private Integer setLoginCommit(HttpServletRequest request, String username) {
        String ip = IpUtils.getIpAddr(request);
        String loginCountKey = LoginKey.loginLimitCount.getPrefix() + RedisConstants.SEGMENTATION + ip + RedisConstants.SEGMENTATION + username;
        String count = redisUtils.get(loginCountKey);
        int surplusCount = RedisConstants.NUM_FIVE;
        int exTime = 30;
        if (StringUtils.isNotEmpty(count)) {
            int curCount = Integer.parseInt(count) + 1;
            surplusCount -= curCount;
            redisUtils.setEx(loginCountKey, Integer.toString(curCount), exTime, TimeUnit.MINUTES);
        } else {
            surplusCount -= 1;
            redisUtils.setEx(loginCountKey, RedisConstants.NUM_ONE + "", exTime, TimeUnit.MINUTES);
        }
        return surplusCount;
    }

    @Override
    public List<TUserVo> getList(TUserVo tUserVo) {
        return tUserMapper.getList(tUserVo);
    }

    public TUserVo getUserInfo(TUserVo tUserVo) {
        TUserVo userInfo = tUserMapper.getUserInfo(tUserVo);
        if (userInfo == null || userInfo.getAvatar() == null) {
            return userInfo;
        }
        tUserVo.setAvatarUrl(getFileUrl(userInfo.getAvatar()));
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
        //从Redis中获取IP来源
        String jsonResult = redisUtils.get(LoginKey.loginIpSource, userLogin.getLoginIp());
        if (StringUtils.isEmpty(jsonResult)) {
            String addresses = IpUtils.getAddresses(SysConf.IP + "=" + userLogin.getLoginIp(), "UTF-8");
            if (StringUtils.isNotEmpty(addresses)) {
                onlineAdmin.setLoginLocation(addresses);
                redisUtils.setEx(LoginKey.loginIpSource, userLogin.getLoginIp(), addresses, expiration * 24, TimeUnit.MICROSECONDS);
            }
        } else {
            onlineAdmin.setLoginLocation(jsonResult);
        }
        // 将登录的管理员存储到在线用户表
        redisUtils.setEx(LoginKey.loginToken, userLogin.getToken(), JSONUtil.toJsonStr(onlineAdmin), expiration, TimeUnit.SECONDS);
        // 在维护一张表，用于 uuid - token 互相转换
        redisUtils.setEx(LoginKey.loginUuid, userLogin.getTokenId(), userLogin.getToken(), expiration, TimeUnit.SECONDS);
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

    private String getFileUrl(Long fileId) {
        if (fileId == null) {
            return null;
        }
        try {
            Result<List<FileInfoVo>> fileInfo = ossApi.getFileInfo(Lists.newArrayList(fileId));
            log.info("");
            return Optional.ofNullable(fileInfo).map(item -> item.getData().get(0).getPreUrl()).orElse("");
        } catch (Exception e) {
            log.error("获取头像文件错误：{}", e.getMessage());
            return null;
        }
        //查询用户图片
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
        //校验token
        if (StringUtils.isEmpty(token) || jwtTokenUtils.isExpiration(token, base64Secret)) {
            return false;
        }
        // TODO (majf) 2024/2/21 15:03 是否可以开一个线程，将刷新token的任务添加到那个中去，不影响
        refreshToken(token, base64Secret, uuidToken, barToken);
        // 获取在线的管理员信息
        String username = jwtTokenUtils.getUsername(token, base64Secret);
        SecurityContextHolder.getContext().getAuthentication();
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 通过用户名加载SpringSecurity用户
            UserDetails userDetails = SecurityUserFactory.create(getUserByUsername(username));
            // 校验Token的有效性
            if (jwtTokenUtils.validateToken(token, userDetails, base64Secret)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                //以后可以security中取得SecurityUser信息
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        return true;
    }

    /**
     * param: token
     * param: base64Secret
     * param: uuidToken
     * param: barToken
     * description: 判断是否需要刷新对应的token数据
     * author:      majf
     * return:      void
    */
    private void refreshToken(String token, String base64Secret, String uuidToken, String barToken) {
        taskExecutor.execute(() -> {
            Date expirationDate = jwtTokenUtils.getExpiration(token, base64Secret);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 得到两个日期相差的间隔，秒
            long survivalSecond = DateUtils.diffSecondByTwoDays(DateUtils.getTimeStr(LocalDateTime.now()), sdf.format(expirationDate));
            // 而旧的Token将会在不久之后从Redis中过期,当存活时间小于更新时间，那么将颁发新的Token到客户端，同时重置新的过期时间
            if (survivalSecond < audience.getRefreshSecond()) {
                //生成新的token
                String newToken = audience.getTokenHead() + jwtTokenUtils.refreshToken(token, base64Secret, audience.getExpiresSecond() * 1000);
                // TODO (majf) 2024/2/21 15:04 多次后是否需要修改uuid信息
                redisUtils.setEx(LoginKey.loginUuid, uuidToken, newToken, audience.getExpiresSecond(), TimeUnit.SECONDS);
                String onlineAdminStr = redisUtils.get(LoginKey.loginToken, barToken);
                if (StringUtils.isNotBlank(onlineAdminStr)) {
                    OnlineAdmin onlineAdmin = JSONObject.parseObject(onlineAdminStr, OnlineAdmin.class);
                    onlineAdmin.setToken(newToken);
                    redisUtils.setEx(LoginKey.loginToken, newToken, JSONObject.toJSONString(onlineAdmin), audience.getExpiresSecond(), TimeUnit.SECONDS);
                }
            }
        });
    }

    public AuthRequest getAuthRequest(String appName) {
        AuthRequest authRequest = null;
        switch (appName) {
            case "wechat_mp":
                authRequest = new AuthWeChatMpRequest(AuthConfig.builder()
                        .clientId("wxec93a0ddb72c8cff")
                        .clientSecret("1240434ae0be6dc4b0ba979d7c1f9b7a")
                        .redirectUri("https://mjzp.xyz/login")
                        .build());
                break;
            case "baidu":
                authRequest = new AuthBaiduRequest(AuthConfig.builder()
                        .clientId("w7kcpHna8w8irDiMA4tdnnnQ")
                        .clientSecret("8LTOzDkpVv5LkzPR9yyptsq7MMENyCVS")
                        .redirectUri("https://mjzp.xyz/login")
                        .scopes(Arrays.asList(
                                AuthBaiduScope.BASIC.getScope(),
                                AuthBaiduScope.SUPER_MSG.getScope(),
                                AuthBaiduScope.NETDISK.getScope()
                        ))
//                        .clientId("")
//                        .clientSecret("")
//                        .redirectUri("http://localhost:9001/oauth/baidu/callback")
                        .build());
        }
        return authRequest;
    }
}
