package com.chronicdisease.user.controller;

import com.chronicdisease.common.annotation.OperationLog;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.user.domain.dto.LoginDTO;
import com.chronicdisease.user.domain.dto.RegisterDTO;
import com.chronicdisease.user.domain.dto.UserDTO;
import com.chronicdisease.user.domain.entity.User;
import com.chronicdisease.user.domain.query.UserQuery;
import com.chronicdisease.user.domain.vo.LoginVO;
import com.chronicdisease.user.domain.vo.UserInfoVO;
import com.chronicdisease.common.result.Result;
import com.chronicdisease.user.service.IUserService;
import com.chronicdisease.user.service.Impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {
    @Autowired
    private IUserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO){
        LoginVO result = userService.login(loginDTO);
        return Result.success(result);
    }

    @OperationLog(module = "用户管理", description = "用户注册")
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<UserInfoVO> register(@Valid @RequestBody RegisterDTO registerDTO){
        UserInfoVO result = userService.register(registerDTO);
        return Result.success(result);
    }

    @OperationLog(module = "用户管理", description = "分页查询用户")
    @PostMapping("/page")
    @Operation(summary = "用户分页查询")
    public Result<PageResult<User>> page(@RequestBody UserQuery query){
        PageResult<User> result = userService.getUserPage(query);
        return Result.success(result);
    }

    @OperationLog(module = "用户管理", description = "新增用户")
    @PostMapping("/add")
    @Operation(summary = "管理员新增用户")
    public Result<Void> add(@Valid @RequestBody UserDTO userDTO){
        userService.addUser(userDTO);
        return Result.success();
    }

    @OperationLog(module = "用户管理", description = "编辑用户")
    @PostMapping("/edit")
    @Operation(summary = "管理员编辑用户")
    public Result<Void> edit(@Valid @RequestBody UserDTO userDTO){
        userService.editUser(userDTO);
        return Result.success();
    }

    @OperationLog(module = "用户管理", description = "根据ID查询用户")
    @GetMapping("/queryById")
    @Operation(summary = "根据ID查询用户")
    public Result<User> queryById(@RequestParam("id") Long id){
        User result = userService.queryById(id);
        return Result.success(result);
    }

    @OperationLog(module = "用户管理", description = "删除用户")
    @PostMapping("/delete")
    @Operation(summary = "根据ID删除用户")
    public Result<Void> delete(@RequestParam("id") Long id){
        userService.deleteById(id);
        return Result.success();
    }

    @OperationLog(module = "用户管理", description = "更新用户状态")
    @PostMapping("/updateStatus")
    @Operation(summary = "启用/禁用用户")
    public Result<Void> updateStatus(@RequestParam("id") Long id, @RequestParam("status") Integer status){
        userService.updateStatus(id, status);
        return Result.success();
    }

    @OperationLog(module = "用户管理", description = "获取用户角色数量统计")
    @GetMapping("/count")
    @Operation(summary = "获取患者和医生数量统计")
    public Result<Map<String, Long>> count() {
        return Result.success(userService.getUserCount());
    }
}
