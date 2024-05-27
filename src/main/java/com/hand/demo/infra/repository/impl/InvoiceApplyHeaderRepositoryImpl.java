package com.hand.demo.infra.repository.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.Constants;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author ariel.peaceo@hand-global.com
 * @since 2024-05-21 14:06:03
 */
@Component
public class InvoiceApplyHeaderRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyHeader> implements InvoiceApplyHeaderRepository {
    @Resource
    private InvoiceApplyHeaderMapper invoiceApplyHeaderMapper;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @ProcessLovValue
    @Override
    public List<InvoiceApplyHeader> selectList(InvoiceApplyHeader invoiceApplyHeader) {
        return invoiceApplyHeaderMapper.selectHead(invoiceApplyHeader);
    }

    @Override
    public InvoiceApplyHeader selectByPrimary(Long applyHeaderId) {
        String cache = redisHelper.strGet(Constants.DETAIL_REDIS_KEY+applyHeaderId);
        List<InvoiceApplyHeader> invoiceApplyHeaders = null;
        if( cache==null ){
            InvoiceApplyHeader invoiceApplyHeader = new InvoiceApplyHeader();
            invoiceApplyHeader.setApplyHeaderId(applyHeaderId);
            invoiceApplyHeaders = invoiceApplyHeaderMapper.selectHead(invoiceApplyHeader);
            if (invoiceApplyHeaders.isEmpty()) {
                return null;
            }
            InvoiceApplyLine queryParam = new InvoiceApplyLine();
            queryParam.setApplyHeaderId(applyHeaderId);
            invoiceApplyHeaders.get(0).setLineList(invoiceApplyLineRepository.select(queryParam));
            redisHelper.strSet(Constants.DETAIL_REDIS_KEY+invoiceApplyHeaders.get(0).getApplyHeaderId(), JSON.toJSONString(invoiceApplyHeaders.get(0)), 5, TimeUnit.MINUTES);
        }else {
            return JSON.parseObject(cache, InvoiceApplyHeader.class);
        }
        return invoiceApplyHeaders.get(0);
    }

    @Override
    public void updateRedis(Long applyHeaderId) {
        String cache = redisHelper.strGet(Constants.DETAIL_REDIS_KEY + applyHeaderId);
        List<InvoiceApplyHeader> invoiceApplyHeaders = null;
//        If cache Found Update Redis
        if (cache != null) {
            InvoiceApplyHeader findHead = new InvoiceApplyHeader();
            findHead.setApplyHeaderId(applyHeaderId);
            invoiceApplyHeaders = invoiceApplyHeaderMapper.selectHead(findHead);
            if (invoiceApplyHeaders.isEmpty()) {
                return;
            }
            InvoiceApplyLine queryParam = new InvoiceApplyLine();
            queryParam.setApplyHeaderId(applyHeaderId);
            invoiceApplyHeaders.get(0).setLineList(invoiceApplyLineRepository.select(queryParam));
            redisHelper.strSet(Constants.DETAIL_REDIS_KEY + invoiceApplyHeaders.get(0).getApplyHeaderId(), JSON.toJSONString(invoiceApplyHeaders.get(0)), 5, TimeUnit.MINUTES);
        }
    }

    @Override
    public List<InvoiceApplyHeader> redisMessagePush() {
        return invoiceApplyHeaderMapper.redisQueuePush();
    }


}

