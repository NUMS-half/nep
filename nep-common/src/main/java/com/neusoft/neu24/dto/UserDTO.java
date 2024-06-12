package com.neusoft.neu24.dto;

import com.neusoft.neu24.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class UserDTO {

    private String userId;
    private String userName;
    private String realName;
    private String telephone;
    private Integer roleId;
    private String token;
    private List<Object> messageList;

    public UserDTO(User user, List<Object> messageList) {
        this.userId = user.getUserId();
        this.userName = user.getUsername();
        this.realName = user.getRealName();
        this.telephone = user.getTelephone();
        this.roleId = user.getRoleId();
        this.token = user.getToken();
        this.messageList = messageList;
    }
}
