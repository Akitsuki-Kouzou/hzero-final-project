package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.redis.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author alfredo.frangoulis@hand-global.com
 * @since 2024-05-21 14:23:12
 */
@Service
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    @ProcessLovValue
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
    }

    private boolean isValidHeaderId(Long applyHeaderId) {
        if (applyHeaderId == null) {
            return false;
        }
        InvoiceApplyHeader header = invoiceApplyHeaderRepository.selectByPrimary(applyHeaderId);
        return header != null && header.getDelFlag() == 0;
    }

    @Override
    @Transactional
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        List<InvoiceApplyLine> validLines = invoiceApplyLines.stream()
                .filter(line -> line.getApplyLineId() == null)
                .filter(line -> isValidHeaderId(line.getApplyHeaderId()))
                .collect(Collectors.toList());

        for (InvoiceApplyLine line : validLines) {
            BigDecimal totalAmount = line.getUnitPrice().multiply(line.getQuantity());
            BigDecimal taxAmount = totalAmount.multiply(line.getTaxRate());
            BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);

            line.setTotalAmount(totalAmount);
            line.setTaxAmount(taxAmount);
            line.setExcludeTaxAmount(excludeTaxAmount);
        }

        List<InvoiceApplyLine> insertList = validLines;

        List<InvoiceApplyLine> updateList = invoiceApplyLines.stream()
                .filter(line -> line.getApplyLineId() != null)
                .peek(line -> {
                    InvoiceApplyLine currentVersion = invoiceApplyLineRepository.selectByPrimaryKey(line.getApplyLineId());
                    if (currentVersion != null) {
                        line.setObjectVersionNumber(currentVersion.getObjectVersionNumber());
                    }
                })
                .collect(Collectors.toList());

        invoiceApplyLineRepository.batchInsertSelective(insertList);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);

        Set<Long> headerIds = invoiceApplyLines.stream()
                .map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());

        for (Long headerId : headerIds) {
            invoiceApplyHeaderService.updateHeaderAmounts(headerId);
            updateRedisCache(headerId);
        }
    }

    private void updateRedisCache(Long applyHeaderId) {
        String lineCacheKey = "46324:IAL:" + applyHeaderId;
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineRepository.selectByHeaderId(applyHeaderId);

        String serializedLines = JSON.toJSONString(invoiceApplyLines);
        redisHelper.strSet(lineCacheKey, serializedLines, 600, TimeUnit.SECONDS);
    }

    @Override
    public List<InvoiceApplyLine> fuzzySearch(InvoiceApplyLine invoiceApplyLine) {
        return invoiceApplyLineRepository.fuzzySearch(invoiceApplyLine);
    }
}

