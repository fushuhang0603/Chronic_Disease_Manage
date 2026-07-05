package com.chronicdisease.user.service.Impl.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.user.domain.dto.LoginDTO;
import com.chronicdisease.user.domain.dto.RegisterDTO;
import com.chronicdisease.user.domain.entity.User;
import com.chronicdisease.user.domain.query.UserQuery;
import com.chronicdisease.user.domain.vo.LoginVO;
import com.chronicdisease.user.domain.vo.UserInfoVO;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.user.mapper.UserMapper;
import com.chronicdisease.user.service.Impl.IUserService;
import com.chronicdisease.common.util.JwtTool;
import org.apache.commons.lang3.StringUtils;
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
        String token = JwtTool.createToken(user.getId(), user.getRoleType());
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        UserInfoVO userInfoVO = BeanUtil.copyProperties(user, UserInfoVO.class);
        return LoginVO.builder()
                .token(token)
                .userInfo(userInfoVO)
                .build();
    }

    @Override
    public IPage<User> getUserPage(UserQuery query) {
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getUsername())) {
            wrapper.like(User::getUsername, query.getUsername());
        }
        if (StringUtils.isNotBlank(query.getNickname())) {
            wrapper.like(User::getNickname, query.getNickname());
        }
        if (StringUtils.isNotBlank(query.getRoleType())) {
            wrapper.eq(User::getRoleType, query.getRoleType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        if (StringUtils.isNotBlank(query.getPhone())) {
            wrapper.like(User::getPhone, query.getPhone());
        }
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }
}
