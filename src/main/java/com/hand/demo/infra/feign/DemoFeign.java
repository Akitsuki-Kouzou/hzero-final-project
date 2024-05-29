package com.hand.demo.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;


/**
 * FeignDemo
 */
@FeignClient(value = "FINAL.46324.HEADER", path = "/v1/demos")
public interface DemoFeign {
}
