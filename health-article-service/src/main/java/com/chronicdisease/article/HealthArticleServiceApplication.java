package com.chronicdisease.article;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.chronicdisease.article"})
@EnableDiscoveryClient
@MapperScan("com.chronicdisease.article.**.mapper")
public class HealthArticleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HealthArticleServiceApplication.class, args);
    }
}
