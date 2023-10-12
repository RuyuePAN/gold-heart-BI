package com.pan.springbootinit.model.dto.chart;

import lombok.Data;

/**
 * @ClassName BiResponse
 * @Description TODO
 * @Author Pan
 * @DATE 2023/10/11 20:55
 */
@Data
public class BiResponse {
    private String genChart;
    private String genResult;
    private Long chartId;
}
