package com.hand.demo.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (InvoiceApplyHeader)应用服务
 *
 * @author ariel.peaceo@hand-global.com
 * @since 2024-05-21 14:06:03
 */
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private CodeRuleBuilder codeRuleBuilder;

    @Override
    public Page<InvoiceApplyHeader> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeader) {
        return PageHelper.doPageAndSort(pageRequest, () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
    }

    @Override
    public void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        List<String> applyStatusList = Arrays.asList("D", "F", "S", "C");
        List<String> invoiceColorList = Arrays.asList("R", "B");
        List<String> invoiceTypeList = Arrays.asList("P", "E");
        invoiceApplyHeaders.forEach(InvoiceApplyHeader::initAmount);
        invoiceApplyHeaders = invoiceApplyHeaders.stream()
                .filter(line -> applyStatusList.contains(line.getApplyStatus()))
                .filter(line -> invoiceColorList.contains(line.getInvoiceColor()))
                .filter(line -> invoiceTypeList.contains(line.getInvoiceType()))
                .collect(Collectors.toList());
        List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() == null)
                .collect(Collectors.toList());
        List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream().filter(line -> line.getApplyHeaderId() != null)
                .collect(Collectors.toList());
        //        Generate Code Rule Builder
        insertList.forEach(item -> item.setApplyHeaderNumber(codeRuleBuilder.generateCode("AHN-CODE-46321", new HashMap<>())));

        invoiceApplyHeaderRepository.batchInsertSelective(insertList);
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(updateList);

//      Update Data in Redis
        if (!updateList.isEmpty()) {
            updateList.forEach(this::updateRedis);
        }
    }

    public void updateRedis(InvoiceApplyHeader invoiceApplyHeader) {
        invoiceApplyHeaderRepository.updateRedis(invoiceApplyHeader.getApplyHeaderId());
    }
}

