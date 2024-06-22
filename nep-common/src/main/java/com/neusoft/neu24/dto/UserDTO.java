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

    // 网格员属性
    private String gmProvinceCode;
    private String gmCityCode;
    private String gmTownCode;
    private Integer gmState;

    private String token;

    public UserDTO(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.realName = user.getRealName();
        this.telephone = user.getTelephone();
        this.roleId = user.getRoleId();

        if ( user.getRoleId() == 2 ) {
            this.gmProvinceCode = user.getGmProvinceCode();
            this.gmCityCode = user.getGmCityCode();
            this.gmTownCode = user.getGmTownCode();
            this.gmState = user.getGmState();
        }

        this.token = user.getToken();
    }
}
