package com.hand.demo.app.service.impl;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyLine)应用服务
 *
 * @author ariel.peaceo@hand-global.com
 * @since 2024-05-21 14:06:57
 */
@Service
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
    }

    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        invoiceApplyLines = invoiceApplyLines.stream()
                .filter(this::validateDataExistAndNotDeleted)
                .peek(InvoiceApplyLine::calculateAmount)
                .collect(Collectors.toList());

        List<InvoiceApplyLine> insertList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() == null)
                .collect(Collectors.toList());

        List<InvoiceApplyLine> updateList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() != null)
                .collect(Collectors.toList());


        invoiceApplyLineRepository.batchInsertSelective(insertList);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);

        Set<Long> headerIdSet = invoiceApplyLines.stream().map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());

        headerIdSet.forEach(x -> {
            calculateHeaderAmount(x);
            updateRedis(x);
        });

    }

    @Override
    public void deleteData(List<InvoiceApplyLine> invoiceApplyLines) {
//      Update the head data
        Set<Long> headerIdSet = invoiceApplyLines.stream().map(InvoiceApplyLine::getApplyHeaderId)
                .collect(Collectors.toSet());
        headerIdSet.forEach( x -> {
            calculateHeaderAmount(x);
            updateRedis(x);
        });
    }

    public void updateRedis(Long headerId){
        invoiceApplyLineRepository.updateRedis(headerId);
    }

    public boolean validateDataExistAndNotDeleted(InvoiceApplyLine item){
        InvoiceApplyHeader headData = invoiceApplyHeaderRepository.selectByPrimary(item.getApplyHeaderId());
        return headData != null && headData.getDelFlag() != 1;
    }

    public void calculateHeaderAmount(Long headId){
        InvoiceApplyLine newQuery = new InvoiceApplyLine();
        newQuery.setApplyHeaderId(headId);
        List<InvoiceApplyLine> lineList = invoiceApplyLineRepository.select(newQuery);
        BigDecimal totalAmount = lineList.stream()
                .map(InvoiceApplyLine::getTotalAmount)
                // First, map to BigDecimal
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal excludeTaxAmount = lineList.stream()
                .map(InvoiceApplyLine::getExcludeTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount = lineList.stream()
                .map(InvoiceApplyLine::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        InvoiceApplyHeader updatedHeader = invoiceApplyHeaderRepository.selectByPrimary(headId);
        updatedHeader.setTotalAmount(totalAmount);
        updatedHeader.setExcludeTaxAmount(excludeTaxAmount);
        updatedHeader.setTaxAmount(taxAmount);
        invoiceApplyHeaderRepository.updateByPrimaryKey(updatedHeader);
    }
}

