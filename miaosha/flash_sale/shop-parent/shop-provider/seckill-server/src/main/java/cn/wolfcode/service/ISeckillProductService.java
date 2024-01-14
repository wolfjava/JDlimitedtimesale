package cn.wolfcode.service;

import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;

import java.util.List;

/**
 * Created by lanxw
 */
public interface ISeckillProductService {
    List<SeckillProductVo> querySeckillProductListByTime(Integer time);

    SeckillProductVo find(String time,Long seckillId);
    String doSeckill(String phone, SeckillProductVo seckillProductVo);

    List<SeckillProductVo> querySeckillProductListByTimeFromCache(Integer time);

    void syncRedisStock(Integer time, Long seckillId);

    void incrStockCount(Long seckillId);
}
