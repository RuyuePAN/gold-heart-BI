package com.pan.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisLimiterManagerTest {
    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Test
    void doRateLimit() {
        // 使用userId为限流器
        String userId = "1";

        // 让它每秒执行5次
        for (int i = 0; i < 5; i++) {
            redisLimiterManager.doRateLimiter(userId);
            System.out.println("成功啦！！！！！！！！！");
        }
    }

}