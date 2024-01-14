package cn.wolfcode.web.feign.fallback;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.web.feign.AlipayFeignApi;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by lanxw
 */
@Component
public class AlipayFeignFallback implements AlipayFeignApi {
    @Override
    public Result<String> pay(PayVo payVo) {
        return null;
    }

    @Override
    public Result<Boolean> rsaCheckV1(Map<String, String> params) {
        return null;
    }
}
