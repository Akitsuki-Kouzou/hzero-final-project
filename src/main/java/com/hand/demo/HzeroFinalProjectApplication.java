package com.hand.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import io.choerodon.resource.annoation.EnableChoerodonResourceServer;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableChoerodonResourceServer
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("org.hzero.boot.admin.translate.mapper")
@EnableFeignClients
public class HzeroFinalProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HzeroFinalProjectApplication.class, args);
    }
}


