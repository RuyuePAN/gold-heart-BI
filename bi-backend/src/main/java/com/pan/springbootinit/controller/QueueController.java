package com.pan.springbootinit.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pan.springbootinit.config.ThreadPoolExecutorConfig;
import com.pan.springbootinit.manager.AiManager;
import com.pan.springbootinit.manager.RedisLimiterManager;
import com.pan.springbootinit.service.ChartService;
import com.pan.springbootinit.service.UserService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName QueueController
 * @Description 队列测试用
 * @Author Pan
 * @DATE 2023/10/14 10:52
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"dev", "local"})      // 只对开发环境和本地环境生效
public class QueueController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 添加任务的接口
     * 提交任务到线程池
     */
    @PostMapping("/add")
    public void add(String name) {
        CompletableFuture.runAsync(() -> {
            log.info("任务执行中：" + name + ", 执行人：" + Thread.currentThread().getName());
            try {
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, threadPoolExecutor);
    }

    /**
     * 获取线程池状态
     */
    @GetMapping("/get")
    public String get() {
        Map<String, Object> map = new HashMap<>();
        int size = threadPoolExecutor.getQueue().size();

        map.put("队列长度：", size);
        long taskCount = threadPoolExecutor.getTaskCount();
        map.put("任务总数：", taskCount);

        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        map.put("已完成的线程数：", completedTaskCount);
        long activeCount = threadPoolExecutor.getActiveCount();
        map.put("正在工作的线程数：", activeCount);

        return JSONUtil.toJsonStr(map);
    }


}
