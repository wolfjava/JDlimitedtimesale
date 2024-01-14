package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.Product;
import cn.wolfcode.domain.SeckillProduct;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.mapper.OrderInfoMapper;
import cn.wolfcode.mapper.SeckillProductMapper;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.IdGenerateUtil;
import cn.wolfcode.web.feign.ProductFeignApi;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by lanxw
 */
@Service
public class SeckillProductServiceImpl implements ISeckillProductService {
    @Autowired
    private SeckillProductMapper seckillProductMapper;
    @Autowired
    private IOrderInfoService orderInfoService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private ProductFeignApi productFeignApi;
    @Override
    public List<SeckillProductVo> querySeckillProductListByTime(Integer time) {
        List<SeckillProduct> seckillProductList = seckillProductMapper.queryCurrentlySeckillProduct(time);
        List<Long> pids = new ArrayList<>();
        for(SeckillProduct seckillProduct:seckillProductList){
            pids.add(seckillProduct.getProductId());
        }
        Result<List<Product>> result = productFeignApi.selectProductListByIds(pids);
        if(result==null || result.hasError()){
            throw new BusinessException(SeckillCodeMsg.REPEAT_SECKILL);
        }
        List<Product> productList = result.getData();
        Map<Long,Product> productMap = new HashMap<>();
        for(Product product:productList){
            productMap.put(product.getId(),product);
        }
        List<SeckillProductVo> seckillProductVoList = new ArrayList<>();
        for(SeckillProduct seckillProduct:seckillProductList){
            SeckillProductVo vo = new SeckillProductVo();
            Product product = productMap.get(seckillProduct.getProductId());
            BeanUtils.copyProperties(product,vo);
            BeanUtils.copyProperties(seckillProduct,vo);
            seckillProductVoList.add(vo);
        }
        return seckillProductVoList;
    }

    @Override
    public SeckillProductVo find(String time,Long seckillId) {
        String key = SeckillRedisKey.SECKILL_PRODUCT_HASH.getRealKey(String.valueOf(time));
        String objStr = (String) redisTemplate.opsForHash().get(key, String.valueOf(seckillId));
        return JSON.parseObject(objStr,SeckillProductVo.class);
    }

    @Override
    @Transactional
    public String doSeckill(String phone, SeckillProductVo seckillProductVo) {
        int count = seckillProductMapper.decrStock(seckillProductVo.getId());
        if(count==0){
            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        String orderNo = orderInfoService.createOrderInfo(phone,seckillProductVo);
        return orderNo;
    }

    @Override
    public List<SeckillProductVo> querySeckillProductListByTimeFromCache(Integer time) {
        String key = SeckillRedisKey.SECKILL_PRODUCT_HASH.getRealKey(String.valueOf(time));
        List<Object> values = redisTemplate.opsForHash().values(key);
        List<SeckillProductVo> seckillProductVoList = new ArrayList<>();
        for(Object object:values){
            seckillProductVoList.add(JSON.parseObject((String)object,SeckillProductVo.class));
        }
        return seckillProductVoList;
    }

    @Override
    public void syncRedisStock(Integer time, Long seckillId) {
        int stockCount = seckillProductMapper.getStockCount(seckillId);
        if(stockCount>0){
            String key = SeckillRedisKey.SECKILL_STOCK_COUNT_HASH.getRealKey(String.valueOf(time));
            redisTemplate.opsForHash().put(key,String.valueOf(seckillId),String.valueOf(stockCount));
        }
    }

    @Override
    public void incrStockCount(Long seckillId) {
        seckillProductMapper.incrStock(seckillId);
    }
}
