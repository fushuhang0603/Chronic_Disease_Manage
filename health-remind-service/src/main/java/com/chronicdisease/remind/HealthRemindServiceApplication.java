package com.chronicdisease.remind;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.chronicdisease.remind"})
@EnableDiscoveryClient
@EnableScheduling
@MapperScan("com.chronicdisease.remind.**.mapper")
public class HealthRemindServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthRemindServiceApplication.class, args);
    }

}
