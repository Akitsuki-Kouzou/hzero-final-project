package com.hand.demo.app.service.validator;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;

import com.alibaba.fastjson.JSON;
import java.util.List;

@ImportValidators({@ImportValidator(templateCode = "46319.IAH.IMPORT")})
public class IAHImportValidator extends BatchValidatorHandler {

    @Override
    public boolean validate(List<String> data) {
        boolean isValid = true;
        for (int i = 0; i < data.size(); i++) {
            InvoiceApplyHeader invoiceApplyHeader = JSON.parseObject(data.get(i), InvoiceApplyHeader.class);

            if (isEmpty(invoiceApplyHeader.getApplyStatus())) {
                addErrorMsg(i,"Apply Status is mandatory");
                isValid = false;
            } else if (!isValidApplyStatus(invoiceApplyHeader.getApplyStatus())) {
                addErrorMsg(i,"Invalid Apply Status (allowed values: D, S, F, C) - Case Sensitive");
                isValid = false;
            }

            if (isEmpty(invoiceApplyHeader.getInvoiceColor())) {
                addErrorMsg(i,"Invoice Color is mandatory");
                isValid = false;
            } else if (!isValidInvoiceColor(invoiceApplyHeader.getInvoiceColor())) {
                addErrorMsg(i,"Invalid Invoice Color (allowed values: R, B) - Case Sensitive");
                isValid = false;
            }

            if (isEmpty(invoiceApplyHeader.getInvoiceType())) {
                addErrorMsg(i,"Invoice Type is mandatory");
                isValid = false;

            } else if (!isValidInvoiceType(invoiceApplyHeader.getInvoiceType())) {
                addErrorMsg(i,"Invalid Invoice Type (allowed values: P, E) - Case Sensitive");
                isValid = false;
            }
        }
        return isValid;
    }


    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private boolean isValidApplyStatus(String status) {
        return status.length() == 1 && (status.equals("D") || status.equals("S") || status.equals("F") || status.equals("C"));
    }

    private boolean isValidInvoiceColor(String color) {
        return color.length() == 1 && (color.equals("R") || color.equals("B"));
    }

    private boolean isValidInvoiceType(String type) {
        return type.length() == 1 && (type.equals("P") || type.equals("E"));
    }
}