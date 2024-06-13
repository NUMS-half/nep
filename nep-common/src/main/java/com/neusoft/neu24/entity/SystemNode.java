package com.neusoft.neu24.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@TableName("system_node")
public class SystemNode implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 节点名称
     */
    private String title;

    /**
     * 父节点ID
     */
    private Integer pid;

    /**
     * 节点路径
     */
    private String path;

    /**
     * 节点备注
     */
    private String remark;

    /**
     * 节点状态
     */
    private Integer state;

    /**
     * 当前节点的子节点
     */
    @TableField(exist = false)
    private List<SystemNode> children;
}
