package com.chronicdisease.user.controller;

import com.chronicdisease.common.util.UserInfoContext;
import com.chronicdisease.user.domain.dto.LoginDTO;
import com.chronicdisease.user.domain.dto.RegisterDTO;
import com.chronicdisease.user.domain.vo.LoginVO;
import com.chronicdisease.user.domain.vo.UserInfoVO;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.user.service.Impl.Impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {
    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO){
        Long userId = UserInfoContext.getUserId();
        System.out.println("User ID: " + userId);
        log.info("用户请求登录,{}", loginDTO);
        LoginVO result = userService.login(loginDTO);
        return Result.success(result);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterDTO registerDTO){
        log.info("用户注册，{}", registerDTO);
        UserInfoVO result = userService.register(registerDTO);
        return Result.success(result);
    }
}
