package com.hanghang.service.Impl.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hanghang.domain.dto.RegisterDTO;
import com.hanghang.domain.entity.User;
import com.hanghang.domain.vo.UserInfoVO;
import com.hanghang.exception.BusinessException;
import com.hanghang.mapper.UserMapper;
import com.hanghang.service.Impl.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserInfoVO register(RegisterDTO registerDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registerDTO.getUsername())
                .or()
                .eq(User::getPhone, registerDTO.getPhone())
                .or()
                .eq(User::getNickname, registerDTO.getNickname());
        List<User> existList = userMapper.selectList(queryWrapper);
        if (!existList.isEmpty()) {
            existList.stream()
                    .filter(u -> u.getUsername().equals(registerDTO.getUsername()))
                    .findAny()
                    .ifPresent(u -> { throw new BusinessException("用户名已存在"); });

            existList.stream()
                    .filter(u -> u.getPhone().equals(registerDTO.getPhone()))
                    .findAny()
                    .ifPresent(u -> { throw new BusinessException("手机号已被注册"); });

            existList.stream()
                    .filter(u -> u.getNickname().equals(registerDTO.getNickname()))
                    .findAny()
                    .ifPresent(u -> { throw new BusinessException("已存在相同昵称用户"); });
        }
        String password = BCrypt.hashpw(registerDTO.getPassword(), BCrypt.gensalt());
        User user = BeanUtil.copyProperties(registerDTO, User.class);
        user.setPassword(password);
        user.setStatus(1);
        userMapper.insert(user);
        return BeanUtil.copyProperties(user, UserInfoVO.class);
    }
}
