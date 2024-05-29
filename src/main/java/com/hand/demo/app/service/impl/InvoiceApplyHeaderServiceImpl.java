package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.exception.ErrorMessage;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import com.alibaba.fastjson.JSON;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author marsa.ariqi@hand-global.com
 * @since 2024-05-21 14:19:52
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyLineService invoiceApplyLineService;

    @Autowired
    private CodeRuleBuilder codeRuleBuilder;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
    }

    @Override
    @Transactional
    public void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        List<InvoiceApplyHeader> validHeaders = invoiceApplyHeaders.stream()
                .filter(header -> invoiceApplyHeaderRepository.isValidValue(header.getApplyStatus(), "46319_IAH_APPLYSTATUS"))
                .filter(header -> invoiceApplyHeaderRepository.isValidValue(header.getInvoiceColor(), "46319_IAH_INVCOLOR"))
                .filter(header -> invoiceApplyHeaderRepository.isValidValue(header.getInvoiceType(), "46319_IAH_INVTYPE"))
                .collect(Collectors.toList());

        if (validHeaders.size() != invoiceApplyHeaders.size()) {
            throw new CommonException("Some invoice apply headers have invalid values for apply_status, invoice_color, or invoice_type");
        }

        List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null)
                .peek(header -> header.setApplyHeaderNumber(codeRuleBuilder.generateCode("46319.FINAL.AHN", new HashMap<>())))
                .collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null)
                .peek(header -> {
            InvoiceApplyHeader currentVersion = invoiceApplyHeaderRepository.selectByPrimaryKey(header.getApplyHeaderId());
            if (currentVersion != null) {
                header.setObjectVersionNumber(header.getObjectVersionNumber());
            }
        }).collect(Collectors.toList());


        invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);

        Set<Long> headerIds = invoiceApplyHeaders.stream()
                .map(InvoiceApplyHeader::getApplyHeaderId)
                .collect(Collectors.toSet());
        for (Long headerId : headerIds) {
            updateHeaderAmounts(headerId);
            invoiceApplyLineService.updateRedisCache(headerId);
        }


    }

    @Override
    public List<InvoiceApplyHeader> fuzzySearch(InvoiceApplyHeader invoiceApplyHeader) {
        return invoiceApplyHeaderRepository.fuzzySearch(invoiceApplyHeader);
    }


    @Override
    public ResponseEntity<InvoiceApplyHeader> deleteById(Long applyHeaderId) {
        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimaryKey(applyHeaderId);

        if (invoiceApplyHeader != null) {
            invoiceApplyHeader.setDelFlag(1);
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
            deleteRedisCache(invoiceApplyHeader);
        }
        return Results.success(invoiceApplyHeader);
    }

    @Override
    public void deleteRedisCache(InvoiceApplyHeader header){
        String cacheKey = "46319:IAH:" + header.getApplyHeaderId();
        redisHelper.delKey(cacheKey);
    }

    @Override
    @ProcessLovValue
    public ResponseEntity<InvoiceApplyHeader> detailWithLine(Long applyHeaderId) {
        String headerCacheKey = "46319:IAH:" + applyHeaderId;
        String cachedValue = redisHelper.strGet(headerCacheKey);
        InvoiceApplyHeader invoiceApplyHeader;
        List<InvoiceApplyLine> invoiceApplyLines;
        if(StringUtils.isNotBlank(cachedValue)){
            invoiceApplyHeader = JSON.parseObject(cachedValue, InvoiceApplyHeader.class);
        }else{
            invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
            invoiceApplyLines = invoiceApplyLineRepository.selectByHeaderId(applyHeaderId);
            invoiceApplyHeader.setInvoiceApplyLines(invoiceApplyLines);
            redisHelper.strSet(headerCacheKey, JSON.toJSONString(invoiceApplyHeader), 6, TimeUnit.MINUTES);
        }
        return Results.success(invoiceApplyHeader);
    }

    @Override
    @Transactional
    public void updateHeaderAmounts(Long applyHeaderId) {
        List<InvoiceApplyLine> lines = invoiceApplyLineRepository.selectByHeaderId(applyHeaderId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal excludeTaxAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        for (InvoiceApplyLine line : lines) {
            totalAmount = totalAmount.add(line.getTotalAmount());
            excludeTaxAmount = excludeTaxAmount.add(line.getExcludeTaxAmount());
            taxAmount = taxAmount.add(line.getTaxAmount());
        }

        InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        header.setTotalAmount(totalAmount);
        header.setExcludeTaxAmount(excludeTaxAmount);
        header.setTaxAmount(taxAmount);

        invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
    }

}

