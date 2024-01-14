package cn.wolfcode.web.feign.fallback;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OperateIntergralVo;
import cn.wolfcode.web.feign.IntergralFeignApi;
import org.springframework.stereotype.Component;

/**
 * Created by lanxw
 */
@Component
public class IntergralFeignFallBack implements IntergralFeignApi {
    @Override
    public Result<String> decrIntergral(OperateIntergralVo operateIntergralVo) {
        return null;
    }

    @Override
    public Result<String> incrIntergral(OperateIntergralVo vo) {
        return null;
    }
}
