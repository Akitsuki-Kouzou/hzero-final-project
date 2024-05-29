package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
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

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author alfredo.frangoulis@hand-global.com
 * @since 2024-05-21 14:21:52
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private CodeRuleBuilder codeRuleBuilder;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
    }

    @Override
    public void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        List<InvoiceApplyHeader> validHeaders = invoiceApplyHeaders.stream()
                .filter(header -> isValidValue(header.getApplyStatus(), "46324_INVAH_APPLYSTATUS"))
                .filter(header -> isValidValue(header.getInvoiceColor(), "46324_INVAH_INVOICECOLOR"))
                .filter(header -> isValidValue(header.getInvoiceType(), "46324_INVAH_INVOICETYPE"))
                .collect(Collectors.toList());

        if (validHeaders.size() != invoiceApplyHeaders.size()) {
            // Handle invalid data (throw exception, log error, etc.)
            throw new CommonException("Some invoice apply headers have invalid values for apply_status, invoice_color, or invoice_type");
        }

        List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null)
                .peek(header -> {
                    header.setApplyHeaderNumber(codeRuleBuilder.generateCode("46324.FINAL.AHN", new HashMap<>()));
                })
                .collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null)
                .peek(header -> {
                    InvoiceApplyHeader currentVersion = invoiceApplyHeaderRepository.selectByPrimaryKey(header.getApplyHeaderId());
                    if (currentVersion != null) {
                        header.setObjectVersionNumber(currentVersion.getObjectVersionNumber());
                    }
                })
                .collect(Collectors.toList());

        invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);

        Set<Long> headerIds = invoiceApplyHeaders.stream()
                .map(InvoiceApplyHeader::getApplyHeaderId)
                .collect(Collectors.toSet());

        for (Long headerId : headerIds) {
            updateRedisCache(headerId);
        }
    }

    private void updateRedisCache(Long applyHeaderId) {
        String lineCacheKey = "46324:IAH:" + applyHeaderId;
        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.selectByHeaderId(applyHeaderId);
        invoiceApplyHeader.setLines(invoiceApplyLines);

        String serializedLines = JSON.toJSONString(invoiceApplyHeader);
        redisHelper.strSet(lineCacheKey, serializedLines, 600, TimeUnit.SECONDS);
    }

    private boolean isValidValue(String value, String lovCode) {
        Map<String, List<String>> lovValues = new HashMap<>();
        lovValues.put("46324_INVAH_APPLYSTATUS", Arrays.asList("D", "S", "F", "C"));
        lovValues.put("46324_INVAH_INVOICECOLOR", Arrays.asList("R", "B"));
        lovValues.put("46324_INVAH_INVOICETYPE", Arrays.asList("P", "E"));

        if (!lovValues.containsKey(lovCode)) {
            throw new IllegalArgumentException("Invalid LOV code: " + lovCode);
        }

        return lovValues.get(lovCode).contains(value);
    }

    @Override
    public List<InvoiceApplyHeader> fuzzySearch(InvoiceApplyHeader invoiceApplyHeader) {
        return invoiceApplyHeaderRepository.fuzzySearch(invoiceApplyHeader);
    }

    @Override
    public ResponseEntity<InvoiceApplyHeader> deleteById(Long applyHeaderId) {
        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);

        if (invoiceApplyHeader != null) {
            invoiceApplyHeader.setDelFlag(1);
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
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

    @Override
    @ProcessLovValue
    public ResponseEntity<InvoiceApplyHeader> getHeaderDetail(Long applyHeaderId) {
        String lineCacheKey = "46324:IAL:" + applyHeaderId;

        InvoiceApplyHeader invoiceApplyHeader = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        List<InvoiceApplyLine> invoiceApplyLines = null;

        if (invoiceApplyHeader != null) {
            String cachedLines = redisTemplate.opsForValue().get(lineCacheKey);
            if (cachedLines != null) {
                invoiceApplyLines = JSON.parseArray(cachedLines, InvoiceApplyLine.class);
            }
            if (invoiceApplyLines == null) {
                invoiceApplyLines = invoiceApplyLineRepository.selectByHeaderId(applyHeaderId);

                String serializedLines = JSON.toJSONString(invoiceApplyLines);
                redisHelper.strSet(lineCacheKey, serializedLines, 600, TimeUnit.SECONDS);
            }
            invoiceApplyHeader.setLines(invoiceApplyLines);
        }
        return Results.success(invoiceApplyHeader);
    }
}

