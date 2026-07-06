package com.chronicdisease.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.chronicdisease.user.domain.dto.LoginDTO;
import com.chronicdisease.user.domain.dto.RegisterDTO;
import com.chronicdisease.user.domain.dto.UserDTO;
import com.chronicdisease.user.domain.entity.User;
import com.chronicdisease.user.domain.query.UserQuery;
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

    @GetMapping("/page")
    @Operation(summary = "用户分页查询")
    public Result<IPage<User>> page(UserQuery query){
        log.info("分页查询用户,{}", query);
        IPage<User> result = userService.getUserPage(query);
        return Result.success(result);
    }

    @PostMapping("/add")
    @Operation(summary = "管理员新增用户")
    public Result<Void> add(@Valid @RequestBody UserDTO userDTO){
        log.info("管理员新增用户,{}", userDTO);
        userService.addUser(userDTO);
        return Result.success();
    }

    @PostMapping("/edit")
    @Operation(summary = "管理员编辑用户")
    public Result<Void> edit(@Valid @RequestBody UserDTO userDTO){
        log.info("管理员编辑用户,{}", userDTO);
        userService.editUser(userDTO);
        return Result.success();
    }
}
