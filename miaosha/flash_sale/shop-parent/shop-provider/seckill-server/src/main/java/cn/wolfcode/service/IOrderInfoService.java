package cn.wolfcode.service;


import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.SeckillProductVo;

/**
 * Created by wolfcode-lanxw
 */
public interface IOrderInfoService {
    OrderInfo getOrderInfoByPhoneAndSeckillId(String phone, Long seckillId);

    String createOrderInfo(String phone, SeckillProductVo seckillProductVo);

    OrderInfo getOrderInfoByOrderNo(String orderNo);

    void cancelOrder(String orderNo);

    void payIntergral(String orderNo);

    OrderInfo find(String orderNo);

    void refundIntergral(OrderInfo orderInfo);

    String payOnline(String orderNo);

    void paySuccess(String orderNo);
}
