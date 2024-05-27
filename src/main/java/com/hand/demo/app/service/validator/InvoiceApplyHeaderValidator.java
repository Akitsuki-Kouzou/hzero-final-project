package com.hand.demo.app.service.validator;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ImportValidators({@ImportValidator(templateCode = "HEADER-IMPORT-46321")})
public class InvoiceApplyHeaderValidator extends BatchValidatorHandler {

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public boolean validate(List<String> data) {
        boolean flag = true;

        List<InvoiceApplyHeader> headerList = invoiceApplyHeaderRepository.selectAll();
        Map<String, InvoiceApplyHeader> headerMap = headerList.stream()
                .collect(Collectors.toMap(InvoiceApplyHeader::getApplyHeaderNumber, x -> x, (a,b) -> b));

        List<String> applyStatusList = Arrays.asList("D", "F", "S", "C");
        List<String> invoiceColorList = Arrays.asList("R", "B");
        List<String> invoiceTypeList = Arrays.asList("P", "E");

        for(int i=0; i< data.size(); i++) {
            InvoiceApplyHeader header = JSON.parseObject(data.get(i), InvoiceApplyHeader.class);
            if(header.getApplyHeaderNumber() != null){
//                If Header Number Input Exist
                if(!headerMap.containsKey(header.getApplyHeaderNumber())){
//                    If Header Number not exist
                    addErrorMsg(i, "Apply Status Number "+header.getApplyHeaderNumber()+" is not exist");
                    flag = false;
                }
            }

//            Check Create Header Data
//                Validate Apply Status, Invoice Color, and Invoice Type

            if(!applyStatusList.contains(header.getApplyStatus())){
                addErrorMsg(i, "Apply Status "+ header.getApplyStatus()+ Constants.ERR_MESS);
                flag = false;
            }else if (!invoiceColorList.contains(header.getInvoiceColor())){
                addErrorMsg(i, "Invoice Color "+header.getInvoiceColor()+Constants.ERR_MESS);
                flag = false;
            }else if (!invoiceTypeList.contains(header.getInvoiceType())){
                addErrorMsg(i, "Invoice Type "+header.getInvoiceType()+Constants.ERR_MESS);
                flag = false;
            }
        }

        return flag;
    }
}
