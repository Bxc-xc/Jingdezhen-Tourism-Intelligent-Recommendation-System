package com.jdz.tourism.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataSyncWebSocketHandler implements WebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public DataSyncWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        System.out.println("WebSocket连接建立: " + sessionId);
        
        // 发送欢迎消息
        sendMessage(session, createMessage("connected", "连接成功"));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = (String) message.getPayload();
        System.out.println("收到消息: " + payload);
        
        // 处理客户端消息（如订阅特定数据）
        // 这里可以根据需要实现订阅逻辑
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket传输错误: " + exception.getMessage());
        sessions.remove(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        System.out.println("WebSocket连接关闭: " + sessionId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 广播数据更新消息给所有连接的客户端
     */
    public void broadcastDataUpdate(String dataType, Object data, String operation) {
        String message = createMessage("data_update", Map.of(
            "dataType", dataType,
            "operation", operation,
            "data", data,
            "timestamp", System.currentTimeMillis()
        ));
        
        broadcastMessage(message);
    }

    /**
     * 发送消息给特定用户
     */
    public void sendToUser(String userId, String message) {
        // 这里可以根据用户ID找到对应的session
        // 简化实现，实际项目中需要维护用户ID和session的映射关系
    }

    /**
     * 广播消息给所有连接的客户端
     */
    public void broadcastMessage(String message) {
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    sendMessage(session, message);
                }
            } catch (Exception e) {
                System.err.println("广播消息失败: " + e.getMessage());
            }
        });
    }

    /**
     * 发送消息给指定session
     */
    private void sendMessage(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    /**
     * 创建标准消息格式
     */
    private String createMessage(String type, Object data) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "type", type,
                "data", data,
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            return "{\"type\":\"error\",\"message\":\"消息序列化失败\"}";
        }
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return sessions.size();
    }
}
