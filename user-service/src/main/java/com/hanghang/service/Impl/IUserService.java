package com.hanghang.service.Impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hanghang.domain.dto.RegisterDTO;
import com.hanghang.domain.entity.User;
import com.hanghang.domain.vo.UserInfoVO;

public interface IUserService extends IService<User> {
    UserInfoVO register(RegisterDTO registerDTO);
}
