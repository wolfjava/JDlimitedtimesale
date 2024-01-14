package cn.wolfcode.web.feign;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.web.feign.fallback.ProductFeignFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by lanxw
 */
@FeignClient(value = "product-service",fallback = ProductFeignFallBack.class)
public interface ProductFeignApi {
    @RequestMapping("/product/selectProductListByIds")
    Result<List<Product>> selectProductListByIds(@RequestParam("ids") List<Long> ids);
}
