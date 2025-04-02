package com.bu.dong.fuseki.model.user;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "user")
public class UserPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;
    @Column(name = "password", length = 100, nullable = false)
    private String password;
    @Column(name = "email", length = 100, unique = true)
    private String email;
    @Column(name = "status", nullable = false)
    private Integer status = 1;
    @Column(name = "avatar_url")
    private String avatarURL;
    @Column(name = "role")
    private Integer role;
    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    public User toBO() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setRawPassword(this.password);
        user.setEmail(this.email);
        user.setStatus(this.status);
        user.setAvatarURL(this.avatarURL);
        user.setRole(this.role);
        user.setCreateTime(this.createTime);
        user.setUpdateTime(this.updateTime);
        return user;
    }
}