package com.chronicdisease.user.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chronicdisease.common.constant.BusinessConstant;
import com.chronicdisease.common.result.PageResult;
import com.chronicdisease.user.domain.dto.LoginDTO;
import com.chronicdisease.user.domain.dto.RegisterDTO;
import com.chronicdisease.user.domain.dto.UserDTO;
import com.chronicdisease.user.domain.entity.User;
import com.chronicdisease.user.domain.query.UserQuery;
import com.chronicdisease.user.domain.vo.LoginVO;
import com.chronicdisease.user.domain.vo.UserInfoVO;
import com.chronicdisease.common.exception.BusinessException;
import com.chronicdisease.user.mapper.UserMapper;
import com.chronicdisease.user.service.IUserService;
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
        queryWrapper.and(w -> w.eq(User::getUsername, registerDTO.getUsername())
                        .or()
                        .eq(User::getPhone, registerDTO.getPhone())
                        .or()
                        .eq(User::getRealName, registerDTO.getRealName()))
                .eq(User::getIsDeleted, BusinessConstant.isNotDelete);
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
                    .filter(u -> u.getRealName().equals(registerDTO.getRealName()))
                    .findAny()
                    .ifPresent(u -> { throw new BusinessException("已存在相同姓名用户"); });
        }
        if (!PhoneUtil.isMobile(registerDTO.getPhone())) {
           throw new BusinessException("手机号格式不正确");
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
        wrapper.eq(User::getUsername, loginDTO.getUsername())
                .eq(User::getIsDeleted, BusinessConstant.isNotDelete);
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
    public PageResult<User> getUserPage(UserQuery query) {
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getUsername())) {
            wrapper.like(User::getUsername, query.getUsername());
        }
        if (StringUtils.isNotBlank(query.getRealName())) {
            wrapper.like(User::getRealName, query.getRealName());
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
        wrapper.eq(User::getIsDeleted, BusinessConstant.isNotDelete);
        Page<User> result = userMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal());
    }

    @Override
    public void addUser(UserDTO userDTO) {
        if (StringUtils.isBlank(userDTO.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (!PhoneUtil.isMobile(userDTO.getPhone())) {
            throw new BusinessException("手机号格式不正确");
        }
        // 唯一性校验（仅校验未删除用户）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w.eq(User::getUsername, userDTO.getUsername())
                        .or()
                        .eq(User::getPhone, userDTO.getPhone())
                        .or()
                        .eq(User::getRealName, userDTO.getRealName()))
                .eq(User::getIsDeleted, BusinessConstant.isNotDelete);
        List<User> existList = userMapper.selectList(queryWrapper);
        if (!existList.isEmpty()) {
            existList.stream()
                    .filter(u -> u.getUsername().equals(userDTO.getUsername()))
                    .findAny()
                    .ifPresent(u -> { throw new BusinessException("用户名已存在"); });
            existList.stream()
                    .filter(u -> u.getPhone().equals(userDTO.getPhone()))
                    .findAny()
                    .ifPresent(u -> { throw new BusinessException("手机号已被注册"); });
            existList.stream()
                    .filter(u -> u.getRealName().equals(userDTO.getRealName()))
                    .findAny()
                    .ifPresent(u -> { throw new BusinessException("已存在相同姓名用户"); });
        }
        String password = BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt());
        User user = BeanUtil.copyProperties(userDTO, User.class);
        user.setPassword(password);
        userMapper.insert(user);
    }

    @Override
    public void editUser(UserDTO userDTO) {
        if (userDTO.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        LambdaQueryWrapper<User> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(User::getId, userDTO.getId()).eq(User::getIsDeleted, BusinessConstant.isNotDelete);
        User existUser = userMapper.selectOne(existWrapper);
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userDTO.getId());
        if (StringUtils.isNotBlank(userDTO.getRealName())) {
            wrapper.set(User::getRealName, userDTO.getRealName());
        }
        if (StringUtils.isNotBlank(userDTO.getPhone())) {
            if (!PhoneUtil.isMobile(userDTO.getPhone())) {
                throw new BusinessException("手机号格式不正确");
            }
            wrapper.set(User::getPhone, userDTO.getPhone());
        }
        if (StringUtils.isNotBlank(userDTO.getRoleType())) {
            wrapper.set(User::getRoleType, userDTO.getRoleType());
        }
        if (userDTO.getStatus() != null) {
            wrapper.set(User::getStatus, userDTO.getStatus());
        }
        if (StringUtils.isNotBlank(userDTO.getPassword())) {
            wrapper.set(User::getPassword, BCrypt.hashpw(userDTO.getPassword(), BCrypt.gensalt()));
        }
        userMapper.update(wrapper);
    }

    @Override
    public User queryById(Long id) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, id).eq(User::getIsDeleted, BusinessConstant.isNotDelete);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public void deleteById(Long id) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id).eq(User::getIsDeleted, BusinessConstant.isNotDelete);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id).set(User::getIsDeleted, BusinessConstant.isDelete);
        userMapper.update(wrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, id).eq(User::getIsDeleted, BusinessConstant.isNotDelete);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id).set(User::getStatus, status);
        userMapper.update(wrapper);
    }
}
