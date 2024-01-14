package cn.wolfcode.service.impl;

import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.domain.AccountTransaction;
import cn.wolfcode.domain.OperateIntergralVo;
import cn.wolfcode.mapper.AccountTransactionMapper;
import cn.wolfcode.mapper.UsableIntegralMapper;
import cn.wolfcode.service.IUsableIntegralService;
import cn.wolfcode.web.msg.IntergralCodeMsg;
import com.alibaba.fastjson.JSONObject;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by lanxw
 */
@Service
public class UsableIntegralServiceImpl implements IUsableIntegralService {
    @Autowired
    private UsableIntegralMapper usableIntegralMapper;
    @Autowired
    private AccountTransactionMapper accountTransactionMapper;

    @Override
    @Transactional
    public void decrIntergral(OperateIntergralVo vo) {
        usableIntegralMapper.decrIntergral(vo.getUserId(),vo.getValue());
    }

    @Override
    @Transactional
    public void incrIntergral(OperateIntergralVo vo) {
        usableIntegralMapper.incrIntergral(vo.getUserId(),vo.getValue());
    }

    @Override
    @Transactional
    public void decrIntergralTry(OperateIntergralVo operateIntergralVo, BusinessActionContext context) {
        System.out.println("try方法");
        //插入日志
        AccountTransaction transaction = new AccountTransaction();
        transaction.setState(AccountTransaction.STATE_TRY);
        transaction.setTxId(context.getXid());
        transaction.setActionId(context.getBranchId());
        transaction.setAmount(operateIntergralVo.getValue());
        Date now = new Date();
        transaction.setGmtCreated(now);
        transaction.setGmtModified(now);
        transaction.setUserId(operateIntergralVo.getUserId());
        accountTransactionMapper.insert(transaction);
        //冻结金额
        int count = usableIntegralMapper.freezeIntergral(operateIntergralVo.getUserId(), operateIntergralVo.getValue());
        if(count==0){
            throw new BusinessException(IntergralCodeMsg.INTERGRAL_NOT_ENOUGH);
        }
    }

    @Override
    @Transactional
    public void decrIntergralCommit(BusinessActionContext context) {
        System.out.println("commit方法");
        AccountTransaction accountTransaction = accountTransactionMapper.get(context.getXid(), context.getBranchId());
        if(accountTransaction!=null){
            if(AccountTransaction.STATE_TRY==accountTransaction.getState()){
                JSONObject jsonObject = (JSONObject) context.getActionContext("operateIntergralVo");
                OperateIntergralVo vo = jsonObject.toJavaObject(OperateIntergralVo.class);
                usableIntegralMapper.commitChange(vo.getUserId(),vo.getValue());
                accountTransactionMapper.updateAccountTransactionState(context.getXid(), context.getBranchId(),AccountTransaction.STATE_COMMIT,AccountTransaction.STATE_TRY);
            }
        }
    }

    @Override
    public void decrIntergralRollback(BusinessActionContext context) {
        System.out.println("cancel方法");
        JSONObject jsonObject = (JSONObject) context.getActionContext("operateIntergralVo");
        OperateIntergralVo vo = jsonObject.toJavaObject(OperateIntergralVo.class);
        AccountTransaction accountTransaction = accountTransactionMapper.get(context.getXid(), context.getBranchId());
        if(accountTransaction==null){
            //插入日志
            AccountTransaction transaction = new AccountTransaction();
            transaction.setState(AccountTransaction.STATE_CANCEL);
            transaction.setTxId(context.getXid());
            transaction.setActionId(context.getBranchId());
            transaction.setAmount(vo.getValue());
            Date now = new Date();
            transaction.setGmtCreated(now);
            transaction.setGmtModified(now);
            transaction.setUserId(vo.getUserId());
            accountTransactionMapper.insert(transaction);
            return;
        }
        if(AccountTransaction.STATE_TRY==accountTransaction.getState()){
            usableIntegralMapper.unFreezeIntergral(vo.getUserId(),vo.getValue());
            accountTransactionMapper.updateAccountTransactionState(context.getXid(), context.getBranchId(),AccountTransaction.STATE_CANCEL,AccountTransaction.STATE_TRY);
        }
    }
}
