package com.hanghang.controller;

import com.hanghang.domain.dto.RegisterDTO;
import com.hanghang.domain.vo.UserInfoVO;
import com.hanghang.result.Result;
import com.hanghang.service.Impl.Impl.UserServiceImpl;
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

    @GetMapping("/login")
    public Result login(){
        log.info("用户请求登录");
        return Result.success("登录成功");
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterDTO registerDTO){
        log.info("用户注册，{}", registerDTO);
        UserInfoVO vo = userService.register(registerDTO);
        return Result.success(vo);
    }
}
