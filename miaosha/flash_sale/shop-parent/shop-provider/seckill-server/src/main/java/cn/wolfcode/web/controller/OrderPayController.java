package cn.wolfcode.web.controller;


import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.web.feign.AlipayFeignApi;
import com.google.common.collect.Ordering;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/orderPay")
@RefreshScope
public class OrderPayController {
    @Autowired
    private IOrderInfoService orderInfoService;
    @Autowired
    private AlipayFeignApi alipayFeignApi;
    @Value("${pay.errorUrl}")
    private String errorUrl;
    @Value("${pay.frontEndPayUrl}")
    private String frontEndPayUrl;
    @RequestMapping("/pay")
    public Result<String> pay(String orderNo,Integer type){
        if(OrderInfo.PAYTYPE_INTERGRAL.equals(type)){
            orderInfoService.payIntergral(orderNo);
            return Result.success("");
        }else{
            String html = orderInfoService.payOnline(orderNo);
            return Result.success(html);
        }

    }
    @RequestMapping("/refund")
    public Result<String> refund(String orderNo){
        OrderInfo orderInfo = orderInfoService.find(orderNo);
        if(OrderInfo.PAYTYPE_ONLINE.equals(orderInfo.getPayType())){

        }else{
            orderInfoService.refundIntergral(orderInfo);
        }
        return Result.success();
    }
    @RequestMapping("/notify_url")
    public String notifyUrl(@RequestParam Map<String,String> params){
        System.out.println("异步回调");
        Result<Boolean> result = alipayFeignApi.rsaCheckV1(params);
        if(result==null || result.hasError()){
            return "fail";
        }
        boolean signVerified = result.getData();
        if(signVerified){
            String orderNo = params.get("out_trade_no");
            orderInfoService.paySuccess(orderNo);
            return "success";
        }else{
            return "fail";
        }
    }
    @RequestMapping("/return_url")
    public void returnUrl(@RequestParam Map<String,String> params,HttpServletResponse response) throws IOException {
        System.out.println("同步回调");
        Result<Boolean> result = alipayFeignApi.rsaCheckV1(params);
        if(result==null || result.hasError()){
            response.sendRedirect(errorUrl);
        }
        boolean signVerified = result.getData();
        if(signVerified) {
            String orderNo = params.get("out_trade_no");
            response.sendRedirect(frontEndPayUrl+orderNo);
        }else {
            response.sendRedirect(errorUrl);
        }
    }
}
