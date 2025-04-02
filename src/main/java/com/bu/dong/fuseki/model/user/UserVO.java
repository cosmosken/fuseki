package com.bu.dong.fuseki.model.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class UserVO {
    private String id;
    private String username;
    private String password;
    private String email;
    private String status;
    private String avatarURL;
    private String role;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public User toBO() {
        User user = new User();
        user.setId(Long.valueOf(this.id));
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setEmail(this.email);
        user.setStatus(Integer.valueOf(this.status));
        user.setAvatarURL(this.avatarURL);
        user.setRole(Integer.valueOf(this.role));
        user.setCreateTime(this.createTime);
        user.setUpdateTime(this.updateTime);
        return user;
    }
}


