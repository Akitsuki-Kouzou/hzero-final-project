package com.hand.demo.infra.repository.impl;


import com.hand.demo.exception.ErrorMessage;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * (InvoiceApplyHeader)资源库
 *
 * @author marsa.ariqi@hand-global.com
 * @since 2024-05-21 14:19:52
 */
@Component
public class InvoiceApplyHeaderRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyHeader> implements InvoiceApplyHeaderRepository {
    @Resource
    private InvoiceApplyHeaderMapper invoiceApplyHeaderMapper;

    @Override
    @ProcessLovValue
    public List<InvoiceApplyHeader> selectList(InvoiceApplyHeader invoiceApplyHeader) {
        return invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
    }

    @Override
    @ProcessLovValue
    public InvoiceApplyHeader selectByPrimary(Long applyHeaderId) {
        InvoiceApplyHeader invoiceApplyHeader = new InvoiceApplyHeader();
        invoiceApplyHeader.setApplyHeaderId(applyHeaderId);
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
        if (invoiceApplyHeaders.isEmpty()) {
            return null;
        }
        return invoiceApplyHeaders.get(0);
    }

    @Override
    @ProcessLovValue
    public List<InvoiceApplyHeader> fuzzySearch(InvoiceApplyHeader invoiceApplyHeader) {
        return invoiceApplyHeaderMapper.fuzzySearch(invoiceApplyHeader);
    }

    @Override
    public List<InvoiceApplyHeader> failHeaders() {
        return invoiceApplyHeaderMapper.failHeaders();
    }

    @Override
    public boolean isValidValue(String value, String lovCode) {
        Map<String, List<String>> lovValues = new HashMap<>();
        lovValues.put("46319_IAH_APPLYSTATUS", Arrays.asList("D", "S", "F", "C"));
        lovValues.put("46319_IAH_INVCOLOR", Arrays.asList("R", "B"));
        lovValues.put("46319_IAH_INVTYPE", Arrays.asList("P", "E"));

        if (!lovValues.containsKey(lovCode)) {
            throw new ErrorMessage("Invalid LOV code: " + lovCode);
        }

        return lovValues.get(lovCode).contains(value);
    }

}

