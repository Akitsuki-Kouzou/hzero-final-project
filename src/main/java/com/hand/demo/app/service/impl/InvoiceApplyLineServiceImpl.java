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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        List<InvoiceApplyLine> insertList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() == null)
                .filter(this::validateDataExistAndNotDeleted)
                .collect(Collectors.toList());

        List<InvoiceApplyLine> updateList = invoiceApplyLines.stream().filter(line -> line.getApplyLineId() != null)
                .filter(this::validateDataExistAndNotDeleted)
                .collect(Collectors.toList());

//        Set Total Amount, Tax Amount, Exclude Tax Amount
        insertList.forEach(
                item -> {
                    item.setTotalAmount(item.getUnitPrice().multiply(item.getQuantity()));
                    item.setTaxAmount(item.getTotalAmount().multiply(item.getTaxRate()));
                    item.setExcludeTaxAmount(item.getTotalAmount().subtract(item.getTaxAmount()));
                }
        );

        updateList.forEach(
                item -> {
                    item.setTotalAmount(item.getUnitPrice().multiply(item.getQuantity()));
                    item.setTaxAmount(item.getTotalAmount().multiply(item.getTaxRate()));
                    item.setExcludeTaxAmount(item.getTotalAmount().subtract(item.getTaxAmount()));
                }
        );

        invoiceApplyLineRepository.batchInsertSelective(insertList);
        invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);

        List<Long> headerIdListSave = insertList.stream().map(x -> x.getApplyHeaderId()).distinct().collect(Collectors.toList());
        List<Long> headerIdListUpdate = updateList.stream().map(x -> x.getApplyHeaderId()).distinct().collect(Collectors.toList());

//        Update Head Amount
        headerIdListSave.forEach(
                item -> {
                    addToHead(item);
                    updateRedis(item);
                }
        );
        headerIdListUpdate.forEach(
                item -> {
                    addToHead(item);
                    updateRedis(item);
                }
        );

    }

    @Override
    public List<InvoiceApplyHeader> deleteData(List<InvoiceApplyLine> invoiceApplyLines) {
//        Input Recalculated HEADER
        List<InvoiceApplyLine> lineList = new ArrayList<>();
        invoiceApplyLines.forEach(item -> lineList.add(invoiceApplyLineRepository.selectByPrimary(item.getApplyLineId())));
        Map<Long, InvoiceApplyHeader> headerMap = new HashMap<>();
//        Get Sum of Header data needs to be substract
        lineList.forEach(
                line -> {
                    if(headerMap.containsKey(line.getApplyHeaderId())){
                        InvoiceApplyHeader getData = headerMap.get(line.getApplyHeaderId());
                        getData.setTotalAmount(getData.getTotalAmount().add(line.getTotalAmount()));
                        getData.setExcludeTaxAmount(getData.getExcludeTaxAmount().add(line.getExcludeTaxAmount()));
                        getData.setTaxAmount(getData.getTaxAmount().add(line.getTaxAmount()));
                        headerMap.put(line.getApplyHeaderId(),getData);
                    }else{
                        InvoiceApplyHeader data = new InvoiceApplyHeader();
                        data.setTotalAmount(line.getTotalAmount());
                        data.setExcludeTaxAmount(line.getExcludeTaxAmount());
                        data.setTaxAmount(line.getTaxAmount());
                        headerMap.put(line.getApplyHeaderId(), data);
                    }
                }
        );

//        Substract the data
        List<InvoiceApplyHeader> listHeaderChanged = new ArrayList<>();
        headerMap.forEach(
                (key, values) -> {
                    InvoiceApplyHeader getHeader = invoiceApplyHeaderRepository.selectByPrimary(key);
                    getHeader.setTotalAmount(getHeader.getTotalAmount().subtract(values.getTotalAmount()));
                    getHeader.setExcludeTaxAmount(getHeader.getExcludeTaxAmount().subtract(values.getExcludeTaxAmount()));
                    getHeader.setTaxAmount(getHeader.getTaxAmount().subtract(values.getTaxAmount()));
                    listHeaderChanged.add(getHeader);
                }
        );

        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(listHeaderChanged);

        return listHeaderChanged;
    }

    public void updateRedis(Long headerId){
        invoiceApplyLineRepository.updateRedis(headerId);
    }

    public boolean validateDataExistAndNotDeleted(InvoiceApplyLine item){
        InvoiceApplyHeader headData = invoiceApplyHeaderRepository.selectByPrimary(item.getApplyHeaderId());
        return headData != null && headData.getDelFlag() != 1;
    }

    public void addToHead(Long headId){
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

