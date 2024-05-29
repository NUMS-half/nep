package com.neusoft.neu24.user.entity;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;

/**
 * <p>
 * 
 * </p>
 *
 * @author Team-NEU-NanHu
 * @since 2024-05-21
 */
@Data
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识UUID
     */
    @TableId(value = "user_id")
    private String userId;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户登录密码
     */
    private String password;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 用户电话号码
     */
    private String telephone;

    /**
     * 用户性别	0：女	1：男
     */
    private Integer gender;

    /**
     * 用户出生日期
     */
    private String birthday;

    /**
     * 用户角色身份：	0：系统管理员	1：公众监督员	2：网格员	
     */
    private Integer roleId;

    /**
     * 用户头像地址
     */
    private String headPhotoLoc;

    /**
     * 网格员所属网格区域的省ID
     */
    private String gmProvinceId;

    /**
     * 网格员所属网格区域的市ID
     */
    private String gmCityId;

    /**
     * 网格员所属网格区域的区ID
     */
    private String gmTownId;

    /**
     * 网格员当前状态：	0：空闲	1：指派工作中	2：休假	3：其他
     */
    private Integer gmState;

    /**
     * 用户备注
     */
    private String remarks;


}
