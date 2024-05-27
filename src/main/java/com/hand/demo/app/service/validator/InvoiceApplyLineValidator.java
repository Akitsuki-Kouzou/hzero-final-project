package com.hand.demo.app.service.validator;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ImportValidators({@ImportValidator(templateCode = "LINE-IMPORT-46321")})
public class InvoiceApplyLineValidator extends BatchValidatorHandler {

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public boolean validate(List<String> data) {
        boolean flag = true;

        List<InvoiceApplyHeader> headerList = invoiceApplyHeaderRepository.selectAll();
//        Map For checking Header ID Exist
        Map<Long, InvoiceApplyHeader> headerMap = headerList.stream()
                .collect(Collectors.toMap(InvoiceApplyHeader::getApplyHeaderId, x -> x, (a, b) -> b));
//        Map for checking line ID correct if line ID inputted
        List<InvoiceApplyLine> lineList = invoiceApplyLineRepository.selectAll();
        Map<Long, InvoiceApplyLine> lineMap = lineList.stream()
                .collect(Collectors.toMap(InvoiceApplyLine::getApplyLineId , x->x, (a,b)-> b));

        for(int i=0; i< data.size(); i++ ){
            InvoiceApplyLine line = JSON.parseObject(data.get(i), InvoiceApplyLine.class);

//            Check Whether line have ID inputted
            if(line.getApplyLineId()!=null){
//                Check Line Exist in Database
                if(!lineMap.containsKey(line.getApplyLineId())){
                    addErrorMsg(i, "Line ID "+line.getApplyLineId()+" is not exist");
                    flag = false;
                }
            }

//            Check header ID exist
            if(!headerMap.containsKey(line.getApplyHeaderId())){
                addErrorMsg(i, "Header ID "+line.getApplyHeaderId()+" is not exist");
                flag = false;
            }

        }
        return flag;
    }
}
