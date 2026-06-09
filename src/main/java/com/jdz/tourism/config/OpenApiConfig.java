package com.jdz.tourism.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("景德镇旅游智能推荐系统 API")
                        .description("基于Spring Boot开发的旅游推荐系统，提供景点推荐、用户管理、预订服务等功能")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("dev@jdztourism.com")
                                .url("https://github.com/your-repo/tourism-recommendation"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8888").description("开发环境"),
                        new Server().url("https://api.jdztourism.com").description("生产环境")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT认证token")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}