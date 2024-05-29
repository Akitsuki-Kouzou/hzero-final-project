package com.hand.demo.infra.feign;

import com.hand.demo.domain.entity.InvoiceInfoDTO;
import com.hand.demo.infra.feign.fallback.InvoiceInfoFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * FeignDemo
 */
@FeignClient(value = "hzero-final-project-20740", fallback = InvoiceInfoFeignFallback.class)
public interface InvoiceInfoFeign {
    @PostMapping("/v1/example/receive/invoice")
    String sendInvoiceInfo(@RequestBody InvoiceInfoDTO invoiceInfoDTO);
}
