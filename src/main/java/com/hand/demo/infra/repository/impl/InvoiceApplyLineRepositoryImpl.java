package com.hand.demo.infra.repository.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.mapper.InvoiceApplyLineMapper;

import java.math.BigDecimal;
import java.util.Collections;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * (InvoiceApplyLine)资源库
 *
 * @author marsa.ariqi@hand-global.com
 * @since 2024-05-21 14:20:34
 */
@Component
public class InvoiceApplyLineRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyLine> implements InvoiceApplyLineRepository {
    @Resource
    private InvoiceApplyLineMapper invoiceApplyLineMapper;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine) {
        return invoiceApplyLineMapper.selectList(invoiceApplyLine);
    }

    @Override
    public List<InvoiceApplyLine> selectListHN(InvoiceApplyLine invoiceApplyLine) {
        return invoiceApplyLineMapper.exportWithHeaderNumber(invoiceApplyLine);
    }

    @Override
    public InvoiceApplyLine selectByPrimary(Long applyLineId) {
        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyLineId(applyLineId);
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineMapper.selectList(invoiceApplyLine);
        if (invoiceApplyLines.isEmpty()) {
            return null;
        }
        return invoiceApplyLines.get(0);
    }

    @Override
    public List<InvoiceApplyLine> fuzzySearch(InvoiceApplyLine invoiceApplyLine) {
        return invoiceApplyLineMapper.fuzzySearch(invoiceApplyLine);
    }

    @Override
    public List<InvoiceApplyLine> selectByHeaderId(Long applyHeaderId) {
        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyHeaderId(applyHeaderId);
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineMapper.selectList(invoiceApplyLine);
        if (invoiceApplyLines.isEmpty()) {
            return Collections.emptyList();
        }
        return invoiceApplyLines;
    }

    @Override
    public boolean isValidHeaderId(Long applyHeaderId) {
        if (applyHeaderId == null) {
            return false;
        }
        InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        return header != null && header.getDelFlag() == 0;
    }

}

