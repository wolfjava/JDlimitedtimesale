package cn.wolfcode.web.controller;

import cn.wolfcode.common.web.Result;
import cn.wolfcode.domain.OperateIntergralVo;
import cn.wolfcode.service.IUsableIntegralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lanxw
 */
@RestController
@RequestMapping("/intergral")
public class IntegralController {
    @Autowired
    private IUsableIntegralService usableIntegralService;
    @RequestMapping("/decrIntergral")
    public Result<String> decrIntergral(@RequestBody OperateIntergralVo vo){
        usableIntegralService.decrIntergralTry(vo,null);
        return Result.success("");
    }
    @RequestMapping("/incrIntergral")
    public Result<String> incrIntergral(@RequestBody OperateIntergralVo vo){
        usableIntegralService.incrIntergral(vo);
        return Result.success("");
    }

}
