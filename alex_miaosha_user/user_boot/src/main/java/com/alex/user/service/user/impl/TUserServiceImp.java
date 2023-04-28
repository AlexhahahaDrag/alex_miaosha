package com.alex.user.service.user.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alex.api.oss.api.OssApi;
import com.alex.api.oss.vo.fileInfo.FileInfoVo;
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
import com.alex.common.redis.key.UserKey;
import com.alex.common.utils.date.DateUtils;
import com.alex.common.utils.redis.RedisUtils;
import com.alex.common.utils.string.StringUtils;
import com.alex.user.entity.tUserLogin.TUserLogin;
import com.alex.user.entity.user.TUser;
import com.alex.user.mapper.user.TUserMapper;
import com.alex.user.utils.security.SecurityUserFactory;
import com.alex.user.service.user.TUserService;
import com.alex.user.utils.jwt.Audience;
import com.alex.user.utils.jwt.JwtTokenUtils;
import com.alex.utils.IpUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * @description: 管理员表服务实现类
 * @author: alex
 * @createDate: 2022-12-26 17:20:38
 * @version: 1.0.0
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

    @Override
    public Page<TUserVo> getPage(Long pageNum, Long pageSize, TUserVo tUserVo) {
        Page<TUserVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        Page<TUserVo> userPage = tUserMapper.getPage(page, tUserVo);
        List<TUserVo> records = userPage.getRecords();
        if (records == null || records.isEmpty()) {
            return userPage;
        }
        List<Long> fileIdList = records.parallelStream()
                .filter(item -> item.getAvatar() != null)
                .map(TUserVo::getAvatar)
                .collect(Collectors.toList());
        try {
            Result<List<FileInfoVo>> result = ossApi.getFileInfo(fileIdList);
            if ("200".equals(result.getCode()) && result.getData() != null && !result.getData().isEmpty()) {
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
        if (user.getAvatar() != null) {
            user.setAvatarUrl(getFileUrl(user.getAvatar()));
        }
        return user;
    }

    @Override
    public TUser addTUser(TUserVo tUserVo) {
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
        //校验username,mobile,email
        judgeField(map, null);
        TUser tUser = new TUser();
        BeanUtil.copyProperties(tUserVo, tUser);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        tUser.setPassword(encoder.encode(tUserVo.getPassword() == null ? defaultPassword : tUserVo.getPassword()));
        tUserMapper.insert(tUser);
        return tUser;
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
        BeanUtil.copyProperties(tUserVo, tUser);
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
        boolean isPassword = encoder.matches(password, admin.getPassword());
        if (!isPassword) {
            //密码错误，返回提示信息
            return Result.error(ResultEnum.USER_LOGIN_ERROR_MORE.getCode(), String.format(MessageConf.LOGIN_ERROR, setLoginCommit(request, username)));
        }
//        //设置角色信息
//        List<String> roleIds = new ArrayList<>();
//        roleIds.add(admin.getRoleId());
//        List<Role> roles = roleService.listByIds(roleIds);
//        if (roles == null || roles.size() <= 0) {
//            return ResultUtil.result(SysConf.ERROR, MessageConf.NO_ROLE);
//        }
//        StringBuilder sb = new StringBuilder();
//        for (Role role : roles) {
//            sb.append(role.getRoleName()).append(Constants.SYMBOL_COMMA);
//        }
//        String roleName = sb.replace(sb.length() - 1, sb.length(), "").toString();
        HashMap<String, Object> result = new HashMap<>(RedisConstants.NUM_ONE);
        String uuid = StringUtils.getUUID();
        result.put(SysConf.TOKEN, uuid);
        //保存登录信息
        saveLoginLog(request, admin, uuid, ip, isRemember);
//        admin.setRole(roles.get(0));
        //不返回密码到前端
        TUserVo tUserVo = new TUserVo();
        BeanUtil.copyProperties(admin, tUserVo, "password");
        if (admin.getAvatar() != null) {
            tUserVo.setAvatarUrl(getFileUrl(admin.getAvatar()));
        }
        result.put(SysConf.ADMIN, tUserVo);
        return Result.success(result);
    }

    private void saveLoginLog(HttpServletRequest request, TUser admin, String uuid, String ip, Boolean isRemember) {
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
            e.printStackTrace();
        }
        String token = audience.getTokenHead() + jwtToken;
        String os = map.get(SysConf.OS);
        String browser = map.get(SysConf.BROWSER);
        TUserLogin userLogin = null;
        try {
            userLogin = TUserLogin.builder()
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
            userLogin.insert();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //添加在线用户到redis中，设置过期时间
        try {
            this.addOnLineAdmin(userLogin, expiration);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        new Thread(() -> {
//
//        });
    }

    /**
     * @param request
     * @param username 登录名称
     * @description: 设置登录限制，返回剩余次数
     * @author: alex
     * @return: java.lang.Integer
     */
    private Integer setLoginCommit(HttpServletRequest request, String username) {
        String ip = IpUtils.getIpAddr(request);
        String loginCountKey = LoginKey.loginLimitCount.getPrefix() + RedisConstants.SEGMENTATION + ip + RedisConstants.SEGMENTATION + username;
        String count = redisUtils.get(loginCountKey);
        int surplusCount = RedisConstants.NUM_FIVE;
        Integer exTime = 30;
        if (StringUtils.isNotEmpty(count)) {
            Integer curCount = Integer.parseInt(count) + 1;
            surplusCount -= curCount;
            redisUtils.setEx(loginCountKey, curCount.toString(), exTime, TimeUnit.MINUTES);
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
        TUserVo userInfo = getUserInfo(tUserVo);
        return userInfo;
    }

    @Override
    public Result<Boolean> logout() {
        ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attribute.getRequest();
        String token = request.getAttribute(SysConf.TOKEN).toString();
        if (StringUtils.isEmpty(token)) {
            return Result.error(ResultEnum.PARAM_ERROR);
        } else {
            // 获取在线用户信息
            String adminJson = redisUtils.get(UserKey.getById, token);
            if (StringUtils.isNotEmpty(adminJson)) {
                OnlineAdmin onlineAdmin = JSONUtil.toBean(adminJson, OnlineAdmin.class);
                String tokenUid = onlineAdmin.getTokenId();
                redisUtils.delete(LoginKey.loginUuid, tokenUid);
            }
            // 移除Redis中的用户
            redisUtils.delete(LoginKey.loginToken, token);
            SecurityContextHolder.clearContext();
            return Result.success();
        }
    }

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
                .username("username")
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

    private boolean judgeField(Map<String, Object> map, Long id) {
        if (map.isEmpty()) {
            return true;
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
                return false;
            }
        }
        return true;
    }

    /**
     * @param type
     * @param value
     * @param id
     * @description: 判断字段对应的值在数据库中的数量
     * @author: alex
     * @return: boolean
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
        // TODO: 2023/2/16 校验token是否正确
        Date expirationDate = jwtTokenUtils.getExpiration(token, base64Secret);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 得到两个日期相差的间隔，秒
        long survivalSecond = DateUtils.diffSecondByTwoDays(DateUtils.getTimeStr(LocalDateTime.now()), sdf.format(expirationDate));
        // 而旧的Token将会在不久之后从Redis中过期,当存活时间小于更新时间，那么将颁发新的Token到客户端，同时重置新的过期时间
        if (survivalSecond < audience.getRefreshSecond()) {
            //生成新的token
            String newToken = audience.getTokenHead() + jwtTokenUtils.refreshToken(token, base64Secret, audience.getExpiresSecond() * 1000);
            redisUtils.setEx(LoginKey.loginUuid, uuidToken, newToken, audience.getExpiresSecond(), TimeUnit.SECONDS);
            String onlineAdminStr = redisUtils.get(LoginKey.loginToken, barToken);
            if (StringUtils.isNotBlank(onlineAdminStr)) {
                OnlineAdmin onlineAdmin = JSONUtil.toBean(onlineAdminStr, OnlineAdmin.class);
                onlineAdmin.setToken(newToken);
                redisUtils.setEx(LoginKey.loginToken, newToken, JSONUtil.toJsonStr(onlineAdmin), audience.getExpiresSecond(), TimeUnit.SECONDS);
            }
        }
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
}
