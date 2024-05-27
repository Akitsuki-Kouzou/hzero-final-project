package com.hand.demo.app.service.dataimport;

import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import io.choerodon.core.oauth.DetailsHelper;
import io.seata.common.util.StringUtils;
import org.hzero.boot.imported.app.service.IBatchImportService;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ImportService(templateCode = "HEADER-IMPORT-46321")
public class HeaderImport implements IBatchImportService {

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyHeader> headerList = new LinkedList<>();
        Long tenantId = DetailsHelper.getUserDetails().getTenantId();

        List<InvoiceApplyHeader> getHead = invoiceApplyHeaderRepository.selectAll();
        Map<String, InvoiceApplyHeader> headerMap = getHead.stream()
                .collect(Collectors.toMap(InvoiceApplyHeader::getApplyHeaderNumber, x -> x, (a, b) -> b));

        for (String ln: data){
            InvoiceApplyHeader newHead = JSON.parseObject(ln, InvoiceApplyHeader.class);
            newHead.setTenantId(tenantId);
            if(StringUtils.isNotBlank(newHead.getApplyHeaderNumber())){
                newHead.setApplyHeaderId(headerMap.get(newHead.getApplyHeaderNumber()).getApplyHeaderId());
                newHead.setObjectVersionNumber(headerMap.get(newHead.getApplyHeaderNumber()).getObjectVersionNumber());
            }
            headerList.add(newHead);
        }
        invoiceApplyHeaderService.saveData(headerList);
        return true;
    }
}
