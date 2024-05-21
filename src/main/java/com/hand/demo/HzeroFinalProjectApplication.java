package com.hand.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import io.choerodon.resource.annoation.EnableChoerodonResourceServer;

@EnableChoerodonResourceServer
@EnableDiscoveryClient
@SpringBootApplication

public class HzeroFinalProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HzeroFinalProjectApplication.class, args);
    }
}


