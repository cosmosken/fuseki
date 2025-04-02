package com.bu.dong.fuseki.service;

import com.bu.dong.fuseki.model.user.User;
import com.bu.dong.fuseki.model.user.UserPO;
import com.bu.dong.fuseki.model.user.UserVO;
import com.bu.dong.fuseki.repository.UserRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Transactional
public class UserService {
    @Resource
    private UserRepository userRepository;

    // 创建用户
    public UserVO createUser(User user) {
        UserPO po = user.toPO();
        po.setCreateTime(new Date());
        userRepository.save(po);
        return user.toVO();
    }

    // 查询用户
    public UserVO getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserPO::toBO)
                .map(User::toVO)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
    }

    // 更新用户
    public boolean updateUser(Long id, User user) {
        UserPO po = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        userRepository.save(po);
        return true;
    }

    // 删除用户
    public boolean deleteUser(Long id) {
        userRepository.deleteById(id);
        return true;
    }
}