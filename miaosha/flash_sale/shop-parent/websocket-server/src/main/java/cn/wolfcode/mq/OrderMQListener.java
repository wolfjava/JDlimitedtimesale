package cn.wolfcode.mq;

import cn.wolfcode.ws.OrderWebSocketServer;
import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.concurrent.TimeUnit;

/**
 * Created by wolfcode-lanxw
 */
@Component
@RocketMQMessageListener(consumerGroup = "websocket-server-group",topic = MQConstants.ORDER_RESULT_TOPIC)
public class OrderMQListener implements RocketMQListener<OrderMQResult> {
    @Override
    public void onMessage(OrderMQResult message) {
        try{
            System.out.println("消息同事......"+message);
            String token = (String) message.getToken();
            int flag = 3;
            Session session = null;
            while(flag>0){
                session = OrderWebSocketServer.clients.get(token);
                if(session!=null){
                    //可以通过这个uuid获取到客户端对象.
                    session.getBasicRemote().sendText(JSON.toJSONString(message));
                    //发送消息
                    return;
                }
                flag--;
                TimeUnit.MILLISECONDS.sleep(100);//睡眠0.1s
            }
        }catch(Exception e){
            e.printStackTrace();
            //通知不到用户,对整体的业务没有影响.用户还可以在《我的订单》看到是否创建了订单.
        }
    }
}
