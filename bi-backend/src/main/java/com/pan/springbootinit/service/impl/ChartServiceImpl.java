package com.pan.springbootinit.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pan.springbootinit.mapper.ChartMapper;
import com.pan.springbootinit.model.entity.Chart;
import com.pan.springbootinit.service.ChartService;

import org.springframework.stereotype.Service;

/**
* @author 1
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2023-10-09 20:14:38
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

}




