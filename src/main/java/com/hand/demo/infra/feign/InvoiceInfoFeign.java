package com.hand.demo.infra.feign;

import com.hand.demo.domain.entity.InvoiceInfoFeignDTO;
import com.hand.demo.infra.feign.fallback.InvoiceInfoFeignFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "hzero-final-project-20740", fallback = InvoiceInfoFeignFallBack.class)

public interface InvoiceInfoFeign {
    @PostMapping("/v1/example/receive/invoice")
    String sentInvoiceInfo(@RequestBody InvoiceInfoFeignDTO invoiceInfoFeignDTO);
}
