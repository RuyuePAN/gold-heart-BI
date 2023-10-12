package com.pan.springbootinit.controller;
import java.time.LocalDateTime;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.pan.springbootinit.annotation.AuthCheck;
import com.pan.springbootinit.common.BaseResponse;
import com.pan.springbootinit.common.DeleteRequest;
import com.pan.springbootinit.common.ErrorCode;
import com.pan.springbootinit.common.ResultUtils;
import com.pan.springbootinit.constant.CommonConstant;
import com.pan.springbootinit.constant.UserConstant;
import com.pan.springbootinit.exception.BusinessException;
import com.pan.springbootinit.exception.ThrowUtils;
import com.pan.springbootinit.manager.AiManager;
import com.pan.springbootinit.model.dto.chart.*;
import com.pan.springbootinit.model.entity.Chart;
import com.pan.springbootinit.model.entity.User;
import com.pan.springbootinit.service.ChartService;
import com.pan.springbootinit.service.UserService;
import com.pan.springbootinit.utils.Excel2Csv;
import com.pan.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;

/**
 * 帖子接口

 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;
    @Resource
    private AiManager aiManager;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        chart.setGoal(chartAddRequest.getGoal());
        chart.setChartData(chartAddRequest.getChartData());
        chart.setChartType(chartAddRequest.getChartType());
        chart.setUserId(loginUser.getId());

        chart.setGenChart("");
        chart.setGenResult("");


        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }


    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        Long id = chartQueryRequest.getId();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        String name = chartQueryRequest.getName();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id) && id > 0, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);

        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 智能分析文件上传
     *
     * @param multipartFile
     * @param
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                             GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        User loginUser = userService.getLoginUser(request);

        // 参数校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标不能为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "图表名称过长");

        // 压缩后的数据
        String data = Excel2Csv.excelToCsv(multipartFile);
        String chatMessage = buildChatMessage(goal, data, chartType);
        String res = aiManager.doChat(chatMessage);
        System.out.println("返回的结果是：");
        System.out.println(res);
        BiResponse biResponse = getChatResult(res);

        // 将生成的图表结果存储到数据库中
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setName(name);
        chart.setChartData(data);
        chart.setChartType(chartType);
        chart.setGenChart(biResponse.getGenChart());
        chart.setGenResult(biResponse.getGenResult());
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);

        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
    }

    /**
     * 拆分接口返回的结果 BiResponse
     * @return
     */
    private BiResponse getChatResult(String res) {
        String[] splits = res.split("```");
        BiResponse biResponse = new BiResponse();
        // biResponse.setGenChart("option = " + splits[1].substring(splits[1].indexOf("{")));
        biResponse.setGenChart(splits[1].substring(splits[1].indexOf("{")));
        if (splits[2].contains("}"))   biResponse.setGenResult(splits[2].substring(splits[2].indexOf("}") + 3));
        else biResponse.setGenResult(splits[2]);
        return biResponse;
    }

    private String buildChatMessage(String goal, String data, String chartType) {
        JSONObject json = new JSONObject();

        // 添加"messages"字段，并设置其值为一个JSON数组
        JSONArray messages = new JSONArray();
        json.put("messages", messages);

        // 创建一个JSONObject，表示第一条消息
        JSONObject message1 = new JSONObject();

        // 设置"role"字段为"user"
        message1.put("role", "user");

        // 设置"content"字段为所需的内容字符串
        // String content = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容:\n分析需求:\n{用户数量趋势}\n原始数据:\n{日期,用户数量\\n2023.10.10,10\\n2023.10.11,10\\n2023.10.12,10\\n2023.10.13,50\\n2023.10.14,60\\n2023.10.15,10\\n2023.10.16,5\\n2023.10.17,1010\\n2023.10.18,955\\n2023.10.19,536\\n}\n请根据这两部分内容，帮我按照以下格式生成内容(此外不要输出任何多余的开头、结尾、注释)\n{合理的使用前端 Echarts V5 的option 配置对象js代码将数据绘制成散点图}\n{图表的描述及明确的数据分析结论，不少于500字}";
        // String content = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容:\n分析需求:\n{" + goal + "}\n原始数据:\n{" + data + "}\n请根据这两部分内容，帮我按照以下格式生成内容(此外不要输出任何多余的开头、结尾、注释)\n：【【【\n{将数据绘制成"+ chartType +"的前端 Echarts V5 的option 配置对象js代码}【【【\n{看图说话及明确的数据分析结论，不少于500字}";
        String content = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容:\n分析需求:\n{" + goal + "}\n原始数据:\n{" + data + "}\n请根据这两部分内容，帮我按照以下格式生成内容(此外不要输出任何多余的开头、结尾、注释)：\n{将数据绘制成"+ chartType +"的前端 Echarts V5 的option 配置对象json代码}\n{看图说话及明确的数据分析结论，不少于500字}";
        message1.put("content", content);

        // 将message对象添加到messages数组中
        messages.add(message1);
        // 输出JSON字符串
        System.out.println(json.toStringPretty());
        return json.toString();
    }
}
