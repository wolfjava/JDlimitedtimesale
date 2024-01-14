package cn.wolfcode.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Created by lanxw
 * 支付日志表
 */
@Setter
@Getter
public class PayLog {
    public static final int PAY_TYPE_ONLINE = 0;//在线支付
    public static final int PAY_TYPE_INTERGRAL = 1;//积分支付
    private String orderNo;//订单编号
    private Date payTime;//支付时间
    private Long totalAmount;//交易数值
    private int payType;//支付类型
}
