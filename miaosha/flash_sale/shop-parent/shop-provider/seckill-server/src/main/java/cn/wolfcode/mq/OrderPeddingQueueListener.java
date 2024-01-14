package cn.wolfcode.mq;

import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * Created by lanxw
 */
@Component
@RocketMQMessageListener(consumerGroup = "OrderPeddingQueueGroup",topic = MQConstant.ORDER_PEDDING_TOPIC)
public class OrderPeddingQueueListener implements RocketMQListener<OrderMessage> {
    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Override
    public void onMessage(OrderMessage message) {
        OrderMQResult result = new OrderMQResult();
        result.setTime(message.getTime());
        result.setSeckillId(message.getSeckillId());
        String tag = null;
        try{
            tag = MQConstant.ORDER_RESULT_SUCCESS_TAG;
            SeckillProductVo vo = seckillProductService.find(String.valueOf(message.getTime()), message.getSeckillId());
            String orderNo = seckillProductService.doSeckill(String.valueOf(message.getUserPhone()), vo);
            result.setOrderNo(orderNo);
            rocketMQTemplate.syncSend(MQConstant.ORDER_PAY_TIMEOUT_TOPIC, MessageBuilder.withPayload(result).build(),3000,MQConstant.ORDER_PAY_TIMEOUT_DELAY_LEVEL);
        }catch(Exception e){
            e.printStackTrace();
            result.setCode(SeckillCodeMsg.SECKILL_ERROR.getCode());
            result.setMsg(SeckillCodeMsg.SECKILL_ERROR.getMsg());
            tag = MQConstant.ORDER_RESULT_FAIL_TAG;
        }
        result.setToken(message.getToken());
        rocketMQTemplate.syncSend(MQConstant.ORDER_RESULT_TOPIC+":"+tag,result);
    }
}
