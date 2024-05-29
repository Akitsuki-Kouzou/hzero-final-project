package com.hand.demo.app.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import io.choerodon.core.oauth.DetailsHelper;
import lombok.extern.slf4j.Slf4j;
import org.hzero.boot.imported.app.service.IBatchImportService;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@ImportService(templateCode = "46324.IAL.IMPORT")
@Slf4j
public class InvoiceApplyLineServiceImportImpl implements IBatchImportService {
    @Autowired
    InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    InvoiceApplyLineService invoiceApplyLineService;

    @Autowired
    InvoiceApplyHeaderServiceImpl invoiceApplyHeaderService;

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyLine> invoiceApplyLines = new ArrayList<>();

        for(String record : data) {
            InvoiceApplyLine ial = JSON.parseObject(record, InvoiceApplyLine.class);
            InvoiceApplyLine queryParam = new InvoiceApplyLine();
            queryParam.setApplyLineId(ial.getApplyLineId());
            InvoiceApplyLine existingLine = null;
            if (ial.getApplyLineId() != null) {
                existingLine = invoiceApplyLineRepository.selectOne(queryParam);
            }
            if(existingLine != null) {
                ial.setApplyLineId(existingLine.getApplyLineId());
                ial.setObjectVersionNumber(existingLine.getObjectVersionNumber());
                invoiceApplyLines.add(ial);
            } else {
                Long tenantId = DetailsHelper.getUserDetails().getTenantId();
                ial.setTenantId(tenantId);
                invoiceApplyLines.add(ial);
            }
        }

        if(!invoiceApplyLines.isEmpty()) {
            invoiceApplyLineService.saveData(invoiceApplyLines);
        }

        return true;
    }
}
