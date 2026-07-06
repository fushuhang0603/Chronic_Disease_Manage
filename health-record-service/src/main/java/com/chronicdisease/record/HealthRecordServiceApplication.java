package com.chronicdisease.record;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.chronicdisease.record"})
@EnableDiscoveryClient
@MapperScan("com.chronicdisease.record.**.mapper")
public class HealthRecordServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthRecordServiceApplication.class, args);
    }

}
