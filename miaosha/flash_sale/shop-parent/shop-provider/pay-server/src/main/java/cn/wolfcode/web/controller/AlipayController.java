package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.config.AlipayConfig;
import cn.wolfcode.config.AlipayProperties;
import cn.wolfcode.domain.PayVo;
import cn.wolfcode.domain.RefundVo;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/alipay")
public class AlipayController {
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private AlipayProperties alipayProperties;
    @RequestMapping("/pay")
    Result<String> pay(@RequestBody PayVo payVo) throws AlipayApiException {
        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(payVo.getReturnUrl());
        alipayRequest.setNotifyUrl(payVo.getNotifyUrl());
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ payVo.getOutTradeNo() +"\","
                + "\"total_amount\":\""+ payVo.getTotalAmount() +"\","
                + "\"subject\":\""+ payVo.getSubject() +"\","
                + "\"body\":\""+ payVo.getBody() +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        return Result.success(result);
    }
    @RequestMapping("/rsaCheckV1")
    public Result<Boolean> rsaCheckV1(@RequestParam Map<String, String> params) throws AlipayApiException {
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(), alipayProperties.getCharset(), alipayProperties.getSignType()); //调用SDK验证签名
        return Result.success(signVerified);
    }
}
