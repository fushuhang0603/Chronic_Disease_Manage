package com.chronicdisease.remind.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "health-user-service")
public class UserServiceFeign {

}
