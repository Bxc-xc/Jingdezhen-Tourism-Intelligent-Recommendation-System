package com.jdz.tourism;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TourismRecommendationApplication {

    public static void main(String[] args) {
        // 从 application.yml 配置的端口（8888）开始尝试，保持与 Vite 代理一致
        int startPort = 8888;
        int maxRetries = 10;

        for (int i = 0; i < maxRetries; i++) {
            int port = startPort + i;
            try {
                SpringApplication app = new SpringApplication(TourismRecommendationApplication.class);
                app.setDefaultProperties(java.util.Collections.singletonMap("server.port", String.valueOf(port)));
                app.run(args);
                return; // 启动成功，退出循环
            } catch (org.springframework.boot.web.server.PortInUseException e) {
                System.out.printf("端口 %d 已被占用，尝试端口 %d...%n", port, port + 1);
            }
        }
        System.err.printf("无法在端口 %d-%d 范围内启动，请手动指定端口。%n", startPort, startPort + maxRetries - 1);
    }

}
