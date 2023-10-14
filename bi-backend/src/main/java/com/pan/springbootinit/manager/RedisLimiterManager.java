package com.pan.springbootinit.manager;

import com.pan.springbootinit.common.ErrorCode;
import com.pan.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ClassName RedisLimiterManager
 * @Description 使用 Redisson 的 RateLimiter，专门提供 RedisLimiter 的基础服务的，提供的是通用的能力，业务无关
 * @Author Pan
 * @DATE 2023/10/13 20:17
 */
@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;

    /**
     * @param key 用于区分不同的限流器，比如不同的用户 id 应该分别统计：粒度为针对xxx用户，调用xxx方法
     *            比如：genChartByAi_userId
     */
    public void doRateLimiter(String key) {
        // 创建一个名为 key 的限流器
        RRateLimiter reteLimiter = redissonClient.getRateLimiter(key);
        // 设置限流器的限流规则
        // reteLimiter.trySetRate(RateType.OVERALL, 5, 1, RateIntervalUnit.DAYS);
        reteLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS); // 测试用

        // 每当一个操作来了之后，就去请求 1 个令牌（使用几个令牌可以根据用户等级来定，会员消耗的比较少，这样次数就多）
        boolean canOp = reteLimiter.tryAcquire(1);
        if (!canOp) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
