package com.pan.springbootinit.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @ClassName AppProperties
 * @Description TODO
 * @Author Pan
 * @DATE 2023/10/11 15:11
 */
@ConfigurationProperties(prefix = "pan")
@Component
public class AppProperties {
    @Getter
    @Setter
    private BceProvider bceProvider = new BceProvider();
    @Getter
    @Setter
    public static class BceProvider {
        private String apiKey;
        private String secretKey;
    }
}
