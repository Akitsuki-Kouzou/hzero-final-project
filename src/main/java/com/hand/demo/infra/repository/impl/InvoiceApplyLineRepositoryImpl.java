package com.hand.demo.infra.repository.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;
import org.apache.commons.collections.CollectionUtils;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.mapper.InvoiceApplyLineMapper;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * (InvoiceApplyLine)资源库
 *
 * @author ariel.peaceo@hand-global.com
 * @since 2024-05-21 14:06:57
 */
@Component
public class InvoiceApplyLineRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyLine> implements InvoiceApplyLineRepository {
    @Resource
    private InvoiceApplyLineMapper invoiceApplyLineMapper;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine) {
        return invoiceApplyLineMapper.selectLine(invoiceApplyLine);
    }

    @Override
    public InvoiceApplyLine selectByPrimary(Long applyLineId) {
        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyLineId(applyLineId);
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineMapper.selectLine(invoiceApplyLine);
        if (invoiceApplyLines.size() == 0) {
            return null;
        }
        return invoiceApplyLines.get(0);
    }

    @Override
    public void updateRedis(Long applyHeaderId) {
        String cache = redisHelper.strGet(Constants.DETAIL_REDIS_KEY + applyHeaderId);
        List<InvoiceApplyHeader> invoiceApplyHeaders = null;
//        If cache Found Update Redis
        if (cache != null) {
            InvoiceApplyHeader findHead = new InvoiceApplyHeader();
            findHead.setApplyHeaderId(applyHeaderId);
            invoiceApplyHeaders = invoiceApplyHeaderRepository.select(findHead);
            if (invoiceApplyHeaders.isEmpty()) {
                return;
            }
            InvoiceApplyLine queryParam = new InvoiceApplyLine();
            queryParam.setApplyHeaderId(applyHeaderId);
            invoiceApplyHeaders.get(0).setLineList(invoiceApplyLineRepository.select(queryParam));
            redisHelper.strSet(Constants.DETAIL_REDIS_KEY + invoiceApplyHeaders.get(0).getApplyHeaderId(), JSON.toJSONString(invoiceApplyHeaders.get(0)), 5, TimeUnit.MINUTES);
        }
    }

    public List<InvoiceApplyLine> selectExport(InvoiceApplyLine invoiceApplyLine) {
        return invoiceApplyLineMapper.selectExport(invoiceApplyLine);
    }
}

