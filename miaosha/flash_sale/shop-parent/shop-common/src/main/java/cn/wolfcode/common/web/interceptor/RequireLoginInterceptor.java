package cn.wolfcode.common.web.interceptor;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.common.web.anno.RequireLogin;
import cn.wolfcode.redis.CommonRedisKey;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by lanxw
 */
public class RequireLoginInterceptor implements HandlerInterceptor {
    private StringRedisTemplate redisTemplate;
    public RequireLoginInterceptor(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String feignRequest = request.getHeader(CommonConstants.FEIGN_REQUEST_KEY);
            //如果是feign请求，直接放行
            if(!StringUtils.isEmpty(feignRequest) && CommonConstants.FEIGN_REQUEST_TRUE.equals(feignRequest)){
                return true;
            }
            //如果不是Feign请求，判断是否有贴RequireLogin注解
            if(handlerMethod.getMethodAnnotation(RequireLogin.class)!=null){
                response.setContentType("application/json;charset=utf-8");
                String token = request.getHeader(CommonConstants.TOKEN_NAME);
                if(StringUtils.isEmpty(token)){
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.TOKEN_INVALID)));
                    return false;
                }
                String phone = JSON.parseObject(redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token)),String.class);
                if(phone==null){
                    response.getWriter().write(JSON.toJSONString(Result.error(CommonCodeMsg.TOKEN_INVALID)));
                    return false;
                }
            }
        }
        return true;
    }
}

