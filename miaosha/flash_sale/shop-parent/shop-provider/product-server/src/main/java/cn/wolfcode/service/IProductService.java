package cn.wolfcode.service;

import cn.wolfcode.domain.Product;

import java.util.List;

/**
 * Created by lanxw
 */
public interface IProductService {
    List<Product> selectProductListByIds(List<Long> ids);
}
