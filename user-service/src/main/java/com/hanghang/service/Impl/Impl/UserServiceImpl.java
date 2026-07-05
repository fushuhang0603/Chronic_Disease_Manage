package com.hanghang.service.Impl.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hanghang.domain.dto.LoginDTO;
import com.hanghang.domain.dto.RegisterDTO;
import com.hanghang.domain.entity.User;
import com.hanghang.domain.vo.LoginVO;
import com.hanghang.domain.vo.UserInfoVO;
import com.hanghang.exception.BusinessException;
import com.hanghang.mapper.UserMapper;
import com.hanghang.service.Impl.IUserService;
import com.hanghang.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户注册
     * @param registerDTO
     * @return
     */
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

    /**
     * 用户登录
     * @param loginDTO
     * @return
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        String token = JwtUtil.createToken(user.getId(), user.getRoleType());
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        UserInfoVO userInfoVO = BeanUtil.copyProperties(user, UserInfoVO.class);
        return LoginVO.builder()
                .token(token)
                .userInfo(userInfoVO)
                .build();
    }
}
