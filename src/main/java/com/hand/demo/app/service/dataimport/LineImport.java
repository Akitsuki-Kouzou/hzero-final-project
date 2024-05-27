package com.hand.demo.app.service.dataimport;

import com.alibaba.fastjson.JSON;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import io.choerodon.core.oauth.DetailsHelper;
import io.seata.common.util.StringUtils;
import org.hzero.boot.imported.app.service.IBatchImportService;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@ImportService(templateCode = "LINE-IMPORT-46321")
public class LineImport implements IBatchImportService {

    @Autowired
    private InvoiceApplyLineService invoiceApplyLineService;

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;


    @Override
    public Boolean doImport(List<String> data) {
        List<InvoiceApplyLine> lineList = new ArrayList<>();
        Long tenantId = DetailsHelper.getUserDetails().getTenantId();

        List<InvoiceApplyLine> getLine = invoiceApplyLineRepository.selectAll();
        Map<Long, InvoiceApplyLine> lineMap = getLine.stream()
                .collect(Collectors.toMap(InvoiceApplyLine::getApplyLineId , x->x, (a, b)-> b));

        for (String ln: data){
            InvoiceApplyLine newLine = JSON.parseObject(ln, InvoiceApplyLine.class);
            newLine.setTenantId(tenantId);
            if(newLine.getApplyLineId()!=null){
                newLine.setObjectVersionNumber(lineMap.get(newLine.getApplyLineId()).getObjectVersionNumber());
            }
            lineList.add(newLine);
        }
        invoiceApplyLineService.saveData(lineList);
        return true;
    }
}
