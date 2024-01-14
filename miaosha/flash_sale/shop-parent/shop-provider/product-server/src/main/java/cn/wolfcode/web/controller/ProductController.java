package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.Product;
import cn.wolfcode.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    @Autowired
    private IProductService productService;
    @RequestMapping("/selectProductListByIds")
    public Result<List<Product>> selectProductListByIds(@RequestParam("ids") List<Long> ids){
        if(ids==null || ids.size()==0){
            return Result.success(Collections.emptyList());
        }
        return Result.success(productService.selectProductListByIds(ids));
    }
}
