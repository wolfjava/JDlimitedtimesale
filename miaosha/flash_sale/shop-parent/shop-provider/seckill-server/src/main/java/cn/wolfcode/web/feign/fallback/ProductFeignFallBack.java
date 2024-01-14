package cn.wolfcode.web.feign.fallback;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.web.feign.ProductFeignApi;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by lanxw
 */
@Component
public class ProductFeignFallBack implements ProductFeignApi {
    @Override
    public Result<List<Product>> selectProductListByIds(List<Long> ids) {
        return null;
    }
}
