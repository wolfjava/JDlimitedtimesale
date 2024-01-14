package cn.wolfcode.web.controller;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.common.web.anno.RequireLogin;
import cn.wolfcode.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by lanxw
 */
@RequestMapping("/test")
@RestController
public class TestController {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @RequestMapping("/testLogin")
    @RequireLogin
    public Result<String> testLogin(HttpServletRequest request){
        String token = request.getHeader(CommonConstants.TOKEN_NAME);
        String phone = UserUtil.getUserPhone(redisTemplate, token);
        System.out.println(phone);
        return Result.success("");
    }
}
