package com.jdz.tourism.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 时区配置类
 * 统一设置应用时区为Asia/Shanghai，确保时间准确
 */
@Configuration
public class TimezoneConfig {
    
    @PostConstruct
    public void init() {
        // 设置JVM默认时区为Asia/Shanghai
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        System.out.println("时区已设置为: " + TimeZone.getDefault().getID());
    }
}

