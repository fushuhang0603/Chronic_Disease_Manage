package com.chronicdisease.record;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.chronicdisease.record"})
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.chronicdisease.record.**.mapper")
public class HealthRecordServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthRecordServiceApplication.class, args);
    }

}
