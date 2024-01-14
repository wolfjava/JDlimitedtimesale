package cn.wolfcode.web.feign.fallback;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.web.feign.SeckillProductFeignApi;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by lanxw
 */
@Component
public class SeckillProductFeignFallback implements SeckillProductFeignApi {
    @Override
    public Result<List<SeckillProductVo>> queryByTimeForJob(Integer time) {
        return null;
    }
}
