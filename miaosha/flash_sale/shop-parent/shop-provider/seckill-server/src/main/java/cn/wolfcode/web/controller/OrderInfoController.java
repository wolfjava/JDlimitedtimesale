package cn.wolfcode.web.controller;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.web.CommonCodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.common.web.anno.RequireLogin;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.UserUtil;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderInfoController {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private IOrderInfoService orderInfoService;
    @RequestMapping("/doSeckill")
    @RequireLogin
    public Result<String> doSeckill(String time,Long seckillId, HttpServletRequest request){
        Date now = new Date();
        SeckillProductVo seckillProductVo = seckillProductService.find(time,seckillId);
        //判断时间
        if(now.getTime() < seckillProductVo.getStartDate().getTime()){
            return Result.error(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        String token = request.getHeader(CommonConstants.TOKEN_NAME);
        String phone = UserUtil.getUserPhone(redisTemplate,token);
        //判断是否重复下单
        String orderSetKey = SeckillRedisKey.SECKILL_ORDER_SET.getRealKey(String.valueOf(seckillId));
        if(redisTemplate.opsForSet().isMember(orderSetKey,phone)){
            return Result.error(SeckillCodeMsg.REPEAT_SECKILL);
        }
        //判断库存
        String countKey = SeckillRedisKey.SECKILL_STOCK_COUNT_HASH.getRealKey(time);
        Long remainCount = redisTemplate.opsForHash().increment(countKey, String.valueOf(seckillId), -1);
        if(remainCount<0){
            return Result.error(SeckillCodeMsg.SECKILL_STOCK_OVER);
        }
        OrderMessage message = new OrderMessage(Integer.parseInt(time),seckillId,token,Long.parseLong(phone));
        rocketMQTemplate.syncSend(MQConstant.ORDER_PEDDING_TOPIC,message);
        return Result.success("进入抢购队列,请等待结果");
    }
    @RequestMapping("/find")
    @RequireLogin
    public Result<OrderInfo> find(String orderNo,HttpServletRequest request){
        String token = request.getHeader(CommonConstants.TOKEN_NAME);
        String phone = UserUtil.getUserPhone(redisTemplate,token);
        OrderInfo orderInfo = orderInfoService.getOrderInfoByOrderNo(orderNo);
        if(orderInfo==null){
            return Result.error(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        if(!phone.equals(String.valueOf(orderInfo.getUserId()))){
            return Result.error(CommonCodeMsg.ILLEGAL_OPERATION);
        }
        return Result.success(orderInfo);
    }
}
