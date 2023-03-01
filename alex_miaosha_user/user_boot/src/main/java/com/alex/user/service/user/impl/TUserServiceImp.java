package com.alex.user.service.user.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alex.api.user.utils.jwt.Audience;
import com.alex.api.user.utils.jwt.JwtTokenUtils;
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
import com.alex.user.service.user.TUserService;
import com.alex.utils.IpUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
public class TUserServiceImp extends ServiceImpl<TUserMapper, TUser> implements TUserService {

    private final TUserMapper tUserMapper;

    private final RedisUtils redisUtils;

    private final JwtTokenUtils jwtTokenUtils;

    @Value(value = "${isRememberMeExpiresSecond}")
    private int isRememberMeExpiresSecond;

    private final Audience audience;

    @Value(value = "${defaultPassword}")
    private String defaultPassword;

    @Override
    public Page<TUserVo> getPage(Long pageNum, Long pageSize, TUserVo tUserVo) {
        Page<TUserVo> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        return tUserMapper.getPage(page, tUserVo);
    }

    @Override
    public TUserVo queryTUser(String id) {
        return tUserMapper.queryTUser(id);
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
        String roleName = "";
        long expiration = isRemember != null && isRemember ? isRememberMeExpiresSecond : audience.getExpiresSecond();
        String jwtToken = jwtTokenUtils.createJwt(admin.getUsername(), admin.getId(), roleName, audience.getClientId(), audience.getName()
                , expiration * 1000, audience.getBase64Secret());
        String token = audience.getTokenHead() + jwtToken;
        HashMap<String, Object> result = new HashMap<>(RedisConstants.NUM_ONE);
        //保存登录信息
        Map<String, String> map = IpUtils.getOsAndBrowserInfo(request);
        String os = map.get(SysConf.OS);
        String browser = map.get(SysConf.BROWSER);
        String location = map.get(SysConf.LOCATION);
        // TODO: 2023/2/16 添加复杂逻辑
        String uuid = StringUtils.getUUID();
        result.put(SysConf.TOKEN, uuid);
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
        userLogin.insert();
//        admin.setRole(roles.get(0));
        //添加在线用户到redis中，设置过期时间
        this.addOnLineAdmin(userLogin, expiration);
        //不返回密码到前端
        admin.setPassword(null);
        result.put(SysConf.ADMIN, admin);
        return Result.success(result);
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
        return tUserMapper.getUserInfo(tUserVo);
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
                redisUtils.setEx(LoginKey.loginIpSource, userLogin.getLoginIp(), addresses, expiration * 24, TimeUnit.SECONDS);
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
}
