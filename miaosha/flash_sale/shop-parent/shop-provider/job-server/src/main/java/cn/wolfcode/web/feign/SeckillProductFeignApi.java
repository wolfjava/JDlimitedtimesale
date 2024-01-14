package cn.wolfcode.web.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProductVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by lanxw
 */
@FeignClient("seckill-service")
public interface SeckillProductFeignApi {
    @RequestMapping("/seckillProduct/queryByTimeForJob")
    Result<List<SeckillProductVo>> queryByTimeForJob(@RequestParam("time") Integer time);
}
