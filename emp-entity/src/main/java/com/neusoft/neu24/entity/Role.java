package com.neusoft.neu24.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Role implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色ID
     */
    @TableId(value = "role_id")
    private String roleId;

    /**
     * 角色名称
     */
    private String roleName;
}
