package cn.wolfcode.util;

import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.redis.CommonRedisKey;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Created by lanxw
 */
public class UserUtil {
    /**
     * 从Redis中根据用户token信息获取用户手机号码
     * @param redisTemplate
     * @param token
     * @return
     */
    public static String getUserPhone(StringRedisTemplate redisTemplate, String token){
        String strObj = redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token));
        String phone = JSON.parseObject(strObj,String.class);
        return phone;
    }
}
