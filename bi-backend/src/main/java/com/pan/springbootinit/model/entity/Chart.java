package com.pan.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 图表信息表
 * @TableName chart
 */
@TableName(value ="chart")
@Data
public class Chart implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 分析目标
     */
    @TableField(value = "goal")
    private String goal;

    /**
     * 图表名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 图表数据
     */
    @TableField(value = "chartData")
    private String chartData;

    /**
     * 图表类型
     */
    @TableField(value = "chartType")
    private String chartType;

    /**
     * 生成的图表数据
     */
    @TableField(value = "genChart")
    private String genChart;

    /**
     * 生成的分析结论
     */
    @TableField(value = "genResult")
    private String genResult;

    /**
     * 创建者的id
     */
    @TableField(value = "userId")
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(value = "createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "updateTime")
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableField(value = "isDelete")
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}