package com.hand.demo.app.service.validator;

import com.alibaba.fastjson.JSON;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;

import java.math.BigDecimal;
import java.util.List;

@ImportValidators({@ImportValidator(templateCode = "46319.IAL.IMPORT")})
public class IALImportValidator extends BatchValidatorHandler {
    @Override
    public boolean validate(List<String> data) {
        boolean isValid = true;
        for (int i = 0; i < data.size(); i++) {
            InvoiceApplyLine invoiceApplyLine = JSON.parseObject(data.get(i), InvoiceApplyLine.class);

            if (isEmpty(invoiceApplyLine.getApplyHeaderId())) {
                addErrorMsg(i,"Apply Header Id is mandatory");
                isValid = false;
            }
            if (isEmpty(invoiceApplyLine.getInvoiceName())) {
                addErrorMsg(i,"Invoice Name is mandatory");
                isValid = false;
            }
            if (isEmpty(invoiceApplyLine.getContentName())) {
                addErrorMsg(i,"Content Name is mandatory");
                isValid = false;
            }
            if (isEmpty(invoiceApplyLine.getTaxClassificationNumber())) {
                addErrorMsg(i,"Tax Classification Number is mandatory");
                isValid = false;
            }
            if (isEmpty(invoiceApplyLine.getUnitPrice())) {
                addErrorMsg(i,"Unit Price is mandatory");
                isValid = false;
            }
            if (isEmpty(invoiceApplyLine.getQuantity())) {
                addErrorMsg(i,"Quantity is mandatory");
                isValid = false;
            }
            if (isEmpty(invoiceApplyLine.getTaxRate())) {
                addErrorMsg(i,"Tax Rate is mandatory");
                isValid = false;
            }


        }
        return isValid;
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private boolean isEmpty(Long value) {
        return value == null;
    }

    private boolean isEmpty(BigDecimal value) {
        return value == null;
    }
}
