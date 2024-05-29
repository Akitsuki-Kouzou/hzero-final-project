package com.hand.demo.infra.feign.fallback;

import com.hand.demo.domain.entity.InvoiceInfoFeignDTO;
import com.hand.demo.infra.feign.InvoiceInfoFeign;

public class InvoiceInfoFeignFallBack implements InvoiceInfoFeign {
    @Override
    public String sentInvoiceInfo(InvoiceInfoFeignDTO invoiceInfoFeignDTO) {
        return null;
    }
}
