package cn.wolfcode.web.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OperateIntergralVo;
import cn.wolfcode.web.feign.fallback.IntergralFeignFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by lanxw
 */
@FeignClient(value = "intergral-service",fallback = IntergralFeignFallBack.class)
public interface IntergralFeignApi {
    @RequestMapping("/intergral/decrIntergral")
    Result<String> decrIntergral(@RequestBody OperateIntergralVo operateIntergralVo);
    @RequestMapping("/intergral/incrIntergral")
    Result<String> incrIntergral(@RequestBody OperateIntergralVo vo);
}
