package com.hand.demo.infra.feign.fallback;

import com.hand.demo.domain.entity.InvoiceInfoDTO;
import com.hand.demo.infra.feign.InvoiceInfoFeign;

public class InvoiceInfoFeignFallback implements InvoiceInfoFeign {
    @Override
    public String sendInvoiceInfo(InvoiceInfoDTO invoiceInfoDTO) {
        return null;
    }
}
