package com.bu.dong.fuseki.model.user;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long id;
    private String username;
    private String rawPassword;
    private String password;
    private String email;
    private Integer status;
    private String avatarURL;
    private Integer role;
    private Date createTime;
    private Date updateTime;

    public UserPO toPO() {
        UserPO po = new UserPO();
        po.setId(this.id);
        po.setUsername(this.username);
        po.setPassword(this.password);
        po.setEmail(this.email);
        po.setStatus(this.status);
        po.setAvatarURL(this.avatarURL);
        po.setRole(this.role);
        po.setCreateTime(this.createTime);
        po.setUpdateTime(this.updateTime);
        return po;
    }

    public UserVO toVO() {
        UserVO vo = new UserVO();
        vo.setId(String.valueOf(this.id));
        vo.setUsername(this.username);
        vo.setPassword(this.password);
        vo.setEmail(this.email);
        vo.setStatus(String.valueOf(this.status));
        vo.setAvatarURL(this.avatarURL);
        vo.setRole(String.valueOf(this.role));
        vo.setCreateTime(this.createTime);
        vo.setUpdateTime(this.updateTime);
        return vo;
    }
}