package com.bu.dong.fuseki.controller;

import com.bu.dong.fuseki.model.user.UserVO;
import com.bu.dong.fuseki.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping
    public UserVO createUser(@RequestBody @Valid UserVO userVO) {
        return userService.createUser(userVO.toBO());
    }

    @GetMapping("/{id}")
    public UserVO getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public Boolean updateUser(@PathVariable Long id, @RequestBody UserVO userVO) {
        return userService.updateUser(id, userVO.toBO());
    }

    @DeleteMapping("/{id}")
    public Boolean deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }
}