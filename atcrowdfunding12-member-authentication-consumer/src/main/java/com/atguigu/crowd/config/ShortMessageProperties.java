package com.atguigu.crowd.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component                                          // 需要注入IOC容器
@ConfigurationProperties(prefix = "short.message") // 在yml配置文件中对应的前缀
public class ShortMessageProperties {
    private String host;
    private String path;
    private String appcode;
    private String sign;
    private String skin;
}
