package com.pan.springbootinit.model.dto.chart;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 更新请求

 */
@Data
public class ChartUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;
    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图表名称
     */
    private String name;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;

}