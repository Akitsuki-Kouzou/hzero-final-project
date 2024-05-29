package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import io.choerodon.core.oauth.DetailsHelper;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.hzero.boot.imported.app.service.IBatchImportService;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ImportService(templateCode = "46324.IAH.IMPORT")
@Slf4j
public class InvoiceApplyHeaderServiceImportImpl implements IBatchImportService {
    @Autowired
    InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    InvoiceApplyHeaderServiceImpl invoiceApplyHeaderService;

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyHeader> invoiceApplyHeaders = new ArrayList<>();

        for (String record : data) {
            InvoiceApplyHeader iah = JSON.parseObject(record, InvoiceApplyHeader.class);
            InvoiceApplyHeader queryParam = new InvoiceApplyHeader();
            queryParam.setApplyHeaderNumber(iah.getApplyHeaderNumber());
            InvoiceApplyHeader existingHeader = null;
            if (StringUtils.isNotBlank(iah.getApplyHeaderNumber())) {
                existingHeader = invoiceApplyHeaderRepository.selectOne(queryParam);
            }
            if(existingHeader != null) {
                iah.setApplyHeaderId(existingHeader.getApplyHeaderId());
                iah.setObjectVersionNumber(existingHeader.getObjectVersionNumber());
                invoiceApplyHeaderRepository.updateByPrimaryKeySelective(iah);
            } else {
                Long tenantId = DetailsHelper.getUserDetails().getTenantId();
                iah.setTenantId(tenantId);
                iah.setDefaultValue();
                invoiceApplyHeaders.add(iah);
            }
        }

        invoiceApplyHeaderService.saveData(invoiceApplyHeaders);
        return true;
    }
}
