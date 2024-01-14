package cn.wolfcode.web.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.web.feign.fallback.AlipayFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Created by lanxw
 */
@FeignClient(value = "pay-service",fallback = AlipayFeignFallback.class )
public interface AlipayFeignApi {
    @RequestMapping("/alipay/pay")
    Result<String> pay(@RequestBody PayVo payVo);
    @RequestMapping("/alipay/rsaCheckV1")
    Result<Boolean> rsaCheckV1(@RequestParam Map<String, String> params);
}
