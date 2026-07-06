package com.alex.utils.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 *description:  秒杀限流拦截器
 *author:       majf
 *createDate:   2022/7/15 9:53
 *version:      1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillInterceptor {
//        implements HandlerInterceptor {
//
//    private final RedisUtils redisUtils;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        if (handler instanceof HandlerMethod) {
//            HandlerMethod handlerMethod = (HandlerMethod) handler;
//            SeckillLimit seckillLimit = handlerMethod.getMethodAnnotation(SeckillLimit.class);
//            if (seckillLimit == null) {
//                return true;
//            }
//            int seconds = seckillLimit.seconds();
//            int maxCount = seckillLimit.maxCount();
//            String requestURI = request.getRequestURI();
//            AccessKey accessKey = AccessKey.withExpire;
//            //当前获取指定url的访问次数
//            Integer count = redisUtils.get(accessKey, requestURI, Integer.class);
//            if (count == null) {
//                redisUtils.set(accessKey, requestURI, 1, seconds);
//            } else if (count < maxCount) {
//                redisUtils.increase(accessKey, requestURI);
//            } else {
//                log.info("访问太频繁");
//                render(response, ResultEnum.ACCESS_LIMIT_REACHED.getCode(), ResultEnum.ACCESS_LIMIT_REACHED.getValue());
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private void render(HttpServletResponse response, String code, String message) {
//        response.setContentType("application/json;charset=UTF-8");
//        ServletOutputStream outputStream = null;
//        try {
//            outputStream = response.getOutputStream();
//            String str = JSONObject.toJSONString(Result.error(code, message));
//            outputStream.write(str.getBytes("UTF-8"));
//            outputStream.flush();
//            outputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
