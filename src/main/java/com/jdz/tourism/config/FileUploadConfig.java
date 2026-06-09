package com.jdz.tourism.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {
    
    // 文件上传路径
    private static final String UPLOAD_DIR = "uploads/";
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(Paths.get(UPLOAD_DIR).toAbsolutePath().toUri().toString())
                .addResourceLocations("classpath:/static/uploads/");
    }
}