package com.chronicdisease.remind;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.chronicdisease.remind"})
@EnableDiscoveryClient
@MapperScan("com.chronicdisease.remind.**.mapper")
public class HealthRemindServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthRemindServiceApplication.class, args);
    }

}
