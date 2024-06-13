package com.neusoft.neu24.dto;

import com.neusoft.neu24.entity.User;
import lombok.Data;


@Data
public class UserDTO {

    private String userId;
    private String username;
    private String realName;
    private String telephone;
    private Integer roleId;
    private String token;

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.realName = user.getRealName();
        this.telephone = user.getTelephone();
        this.roleId = user.getRoleId();
        this.token = user.getToken();
    }
}
