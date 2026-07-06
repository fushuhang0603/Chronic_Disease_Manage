package com.chronicdisease.user.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.user.domain.dto.LoginDTO;
import com.chronicdisease.user.domain.dto.RegisterDTO;
import com.chronicdisease.user.domain.dto.UserDTO;
import com.chronicdisease.user.domain.entity.User;
import com.chronicdisease.user.domain.query.UserQuery;
import com.chronicdisease.user.domain.vo.LoginVO;
import com.chronicdisease.user.domain.vo.UserInfoVO;

public interface IUserService extends IService<User> {

    UserInfoVO register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);

    IPage<User> getUserPage(UserQuery query);

    void addUser(UserDTO userDTO);

    void editUser(UserDTO userDTO);

    User queryById(Long id);

    void deleteById(Long id);

    void updateStatus(Long id, Integer status);
}
