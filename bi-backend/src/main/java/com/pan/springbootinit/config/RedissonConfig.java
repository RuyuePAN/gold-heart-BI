package com.pan.springbootinit.config;

import io.swagger.models.auth.In;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName RedissonConfig
 * @Description TODO
 * @Author Pan
 * @DATE 2023/10/13 20:05
 */
@Configuration
@ConfigurationProperties(prefix="spring.redis")
@Data
public class RedissonConfig {
    private Integer database;
    private String host;
    private Integer port;
    /**
     * 创建客户端并且返回客户端
     * @return redisson
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://" + host +":" + port);
        // Sync and Async API
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
