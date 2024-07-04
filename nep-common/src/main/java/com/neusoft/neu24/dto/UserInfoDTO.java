package com.neusoft.neu24.dto;

import com.neusoft.neu24.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class UserInfoDTO {
    private String userId;
    private String username;
    private String password;
    private String realName;
    private String telephone;
    private Integer gender;
    private String birthday;
    private Integer roleId;
    private Integer status;
    private String remarks;
    private String headPhotoLoc;

    private String gmProvinceCode;
    private String gmCityCode;
    private String gmTownCode;
    private Integer gmState;

    private String token;
    private List<Integer> permissions;

    public UserInfoDTO() {
    }

    public UserInfoDTO(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.realName = user.getRealName();
        this.telephone = user.getTelephone();
        this.gender = user.getGender();
        this.birthday = user.getBirthday();
        this.roleId = user.getRoleId();
        this.status = user.getStatus();
        this.remarks = user.getRemarks();
        this.headPhotoLoc = user.getHeadPhotoLoc();

        if ( user.getRoleId() == 2 ) {
            this.gmProvinceCode = user.getGmProvinceCode();
            this.gmCityCode = user.getGmCityCode();
            this.gmTownCode = user.getGmTownCode();
            this.gmState = user.getGmState();
        }

        this.token = user.getToken();
    }
}
