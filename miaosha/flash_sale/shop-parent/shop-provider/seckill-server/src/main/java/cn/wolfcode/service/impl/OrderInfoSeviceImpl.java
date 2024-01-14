package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.*;
import cn.wolfcode.mapper.OrderInfoMapper;
import cn.wolfcode.mapper.PayLogMapper;
import cn.wolfcode.mapper.RefundLogMapper;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.IdGenerateUtil;
import cn.wolfcode.web.feign.AlipayFeignApi;
import cn.wolfcode.web.feign.IntergralFeignApi;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Map;

/**
 * Created by wolfcode-lanxw
 */
@Service
public class OrderInfoSeviceImpl implements IOrderInfoService {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PayLogMapper payLogMapper;
    @Autowired
    private RefundLogMapper refundLogMapper;
    @Autowired
    private IntergralFeignApi intergralFeignApi;
    @Autowired
    private AlipayFeignApi alipayFeignApi;
    @Value("${pay.returnUrl}")
    private String returlUrl;
    @Value("${pay.notifyUrl}")
    private String notifyUrl;

    @Override
    public OrderInfo getOrderInfoByPhoneAndSeckillId(String phone, Long seckillId) {
        return orderInfoMapper.getOrderInfoByPhoneAndSeckillId(phone,seckillId);
    }

    @Override
    public String createOrderInfo(String phone, SeckillProductVo seckillProductVo) {
        OrderInfo info = new OrderInfo();
        info.setCreateDate(new Date());
        info.setSeckillDate(seckillProductVo.getStartDate());
        info.setSeckillId(seckillProductVo.getId());
        info.setIntergral(seckillProductVo.getIntergral());
        info.setProductId(seckillProductVo.getProductId());
        info.setProductImg(seckillProductVo.getProductImg());
        info.setProductName(seckillProductVo.getProductName());
        info.setProductPrice(seckillProductVo.getProductPrice());
        info.setSeckillPrice(seckillProductVo.getSeckillPrice());
        info.setSeckillTime(seckillProductVo.getTime());
        info.setUserId(Long.parseLong(phone));
        info.setOrderNo(String.valueOf(IdGenerateUtil.get().nextId()));
        orderInfoMapper.insert(info);
        String key = SeckillRedisKey.SECKILL_ORDER_SET.getRealKey(String.valueOf(seckillProductVo.getId()));
        redisTemplate.opsForSet().add(key,phone);
        return info.getOrderNo();
    }

    @Override
    public OrderInfo getOrderInfoByOrderNo(String orderNo) {
        return orderInfoMapper.find(orderNo);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.find(orderNo);
        if(OrderInfo.STATUS_ARREARAGE.equals(orderInfo.getStatus())){
            orderInfoMapper.updateCancelStatus(orderNo, OrderInfo.STATUS_CANCEL);
            //增加真实库存
            seckillProductService.incrStockCount(orderInfo.getSeckillId());
            //同步预库存
            seckillProductService.syncRedisStock(orderInfo.getSeckillTime(),orderInfo.getSeckillId());
            System.out.println("取消订单成功");
        }
    }

    @Override
    @GlobalTransactional
    public void payIntergral(String orderNo) {
        OrderInfo orderInfo = orderInfoMapper.find(orderNo);
        //1.插入日志,保证幂等性
        PayLog payLog = new PayLog();
        payLog.setOrderNo(orderNo);
        payLog.setPayTime(new Date());
        payLog.setTotalAmount(orderInfo.getIntergral());
        payLog.setPayType(PayLog.PAY_TYPE_INTERGRAL);
        payLogMapper.insert(payLog);
        //2.调用积分远程方法
        OperateIntergralVo vo = new OperateIntergralVo();
        vo.setUserId(orderInfo.getUserId());
        vo.setValue(orderInfo.getIntergral());
        Result<String> result = intergralFeignApi.decrIntergral(vo);
        if(result==null || result.hasError()){
            throw new BusinessException(SeckillCodeMsg.INTERGRAL_SERVER_ERROR);
        }
        //3.更新订单状态
        int count = orderInfoMapper.changePayStatus(orderNo, OrderInfo.STATUS_ACCOUNT_PAID, OrderInfo.PAYTYPE_INTERGRAL);
        if(count==0){
            throw new BusinessException(SeckillCodeMsg.PAY_ERROR);
        }
    }

    @Override
    public OrderInfo find(String orderNo) {
        return orderInfoMapper.find(orderNo);
    }

    @Override
    @Transactional
    public void refundIntergral(OrderInfo orderInfo) {
        //插入日志
        RefundLog refundLog = new RefundLog();
        refundLog.setOrderNo(orderInfo.getOrderNo());
        refundLog.setRefundTime(new Date());
        refundLog.setRefundAmount(orderInfo.getIntergral());
        refundLog.setRefundReason("取消订单");
        refundLogMapper.insert(refundLog);
        //调用远程方法扣减积分
        OperateIntergralVo vo = new OperateIntergralVo();
        vo.setUserId(orderInfo.getUserId());
        vo.setValue(orderInfo.getIntergral());
        Result<String> result = intergralFeignApi.incrIntergral(vo);
        if(result==null || result.hasError()){
            throw new BusinessException(SeckillCodeMsg.INTERGRAL_SERVER_ERROR);
        }
        //更新订单状态
        int count = orderInfoMapper.changeRefundStatus(orderInfo.getOrderNo(), OrderInfo.STATUS_REFUND);
        if(count==0){
            throw new BusinessException(SeckillCodeMsg.REFUND_ERROR);
        }
    }

    @Override
    public String payOnline(String orderNo) {
        OrderInfo orderInfo = this.find(orderNo);
        PayVo vo = new PayVo();
        vo.setOutTradeNo(orderNo);
        vo.setSubject(orderInfo.getProductName());
        vo.setTotalAmount(String.valueOf(orderInfo.getSeckillPrice()));
        vo.setBody(orderInfo.getProductName());
        vo.setReturnUrl(returlUrl);
        vo.setNotifyUrl(notifyUrl);
        Result<String> result = alipayFeignApi.pay(vo);
        if(result==null || result.hasError()){
            throw new BusinessException(SeckillCodeMsg.PAY_SERVER_ERROR);
        }
        return result.getData();
    }

    @Override
    @Transactional
    public void paySuccess(String orderNo) {
        OrderInfo orderInfo = this.find(orderNo);
        //插入支付日志
        PayLog log = new PayLog();
        log.setOrderNo(orderNo);
        log.setPayTime(new Date());
        log.setTotalAmount(orderInfo.getSeckillPrice().longValue());
        log.setPayType(OrderInfo.PAYTYPE_ONLINE);
        payLogMapper.insert(log);
        //更新订单状态
        int count = orderInfoMapper.changePayStatus(orderNo, OrderInfo.STATUS_ACCOUNT_PAID, orderInfo.getPayType());
        if(count==0){
            //记录日志
            throw new BusinessException(SeckillCodeMsg.PAY_ERROR);
        }
    }
}
