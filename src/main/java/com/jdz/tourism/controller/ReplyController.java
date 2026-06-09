package com.jdz.tourism.controller;

import com.jdz.tourism.entity.Reply;
import com.jdz.tourism.service.ReplyService;
import com.jdz.tourism.service.CommentService;
import com.jdz.tourism.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jdz.tourism.annotation.LogOperation;

@RestController
@RequestMapping("/api/replies")
@CrossOrigin(origins = "*")
public class ReplyController {
    
    @Autowired
    private ReplyService replyService;

    @Autowired
    private CommentService commentService;

    @Autowired(required = false)
    private JwtUtil jwtUtil;

    @Autowired(required = false)
    private HttpServletRequest request;

    private Long resolveCurrentUserId() {
        try {
            String auth = request != null ? request.getHeader("Authorization") : null;
            if (auth != null && auth.startsWith("Bearer ") && jwtUtil != null) {
                String token = auth.substring(7);
                if (jwtUtil.validateToken(token)) return jwtUtil.extractUserId(token);
            }
        } catch (Exception ignore) {}
        return null;
    }
    
    /**
     * 创建回复（游客回复评论）
     */
    @LogOperation("游客回复评论")
    @PostMapping("/review/{reviewId}/user")
    public ResponseEntity<Map<String, Object>> createUserReply(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String content = body.get("content").toString();
            
            Reply reply = replyService.createUserReply(reviewId, userId, content);
            response.put("success", true);
            response.put("message", "回复成功");
            response.put("data", reply);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 创建回复（商家回复评论）
     */
    @LogOperation("商家回复评论")
    @PostMapping("/review/{reviewId}/merchant")
    public ResponseEntity<Map<String, Object>> createMerchantReply(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long merchantId = Long.valueOf(body.get("merchantId").toString());
            String content = body.get("content").toString();
            
            Reply reply = replyService.createMerchantReply(reviewId, merchantId, content);
            response.put("success", true);
            response.put("message", "回复成功");
            response.put("data", reply);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 创建回复的回复（游客回复回复）
     */
    @LogOperation("游客回复回复")
    @PostMapping("/review/{reviewId}/reply/{parentReplyId}/user")
    public ResponseEntity<Map<String, Object>> createUserReplyToReply(
            @PathVariable Long reviewId,
            @PathVariable Long parentReplyId,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String content = body.get("content").toString();
            
            Reply reply = replyService.createUserReplyToReply(reviewId, parentReplyId, userId, content);
            response.put("success", true);
            response.put("message", "回复成功");
            response.put("data", reply);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 创建回复的回复（商家回复回复）
     */
    @LogOperation("商家回复回复")
    @PostMapping("/review/{reviewId}/reply/{parentReplyId}/merchant")
    public ResponseEntity<Map<String, Object>> createMerchantReplyToReply(
            @PathVariable Long reviewId,
            @PathVariable Long parentReplyId,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long merchantId = Long.valueOf(body.get("merchantId").toString());
            String content = body.get("content").toString();
            
            Reply reply = replyService.createMerchantReplyToReply(reviewId, parentReplyId, merchantId, content);
            response.put("success", true);
            response.put("message", "回复成功");
            response.put("data", reply);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据评论ID获取所有回复（树形结构）
     */
    @GetMapping("/review/{reviewId}")
    public ResponseEntity<Map<String, Object>> getRepliesByReviewId(@PathVariable Long reviewId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Reply> replies = replyService.getAllRepliesByReviewId(reviewId);
            response.put("success", true);
            response.put("data", replies);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据回复ID获取子回复
     */
    @GetMapping("/reply/{parentReplyId}/children")
    public ResponseEntity<Map<String, Object>> getChildReplies(@PathVariable Long parentReplyId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Reply> replies = replyService.getChildRepliesByParentId(parentReplyId);
            response.put("success", true);
            response.put("data", replies);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除回复（通用，无权限校验，仅内部使用）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReply(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            replyService.deleteReply(id);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 商家删除自己的回复（JWT 校验商家身份，只能删自己发的回复）
     */
    @LogOperation("商家删除回复")
    @DeleteMapping("/{id}/merchant")
    public ResponseEntity<Map<String, Object>> deleteMerchantReply(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = resolveCurrentUserId();
            if (userId == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }
            // 校验商家身份并确认该回复属于此商家
            commentService.deleteMerchantReply(userId, id);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(403).body(response);
        }
    }
    
    /**
     * 更新回复
     */
    @LogOperation("更新回复")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateReply(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String content = body.get("content");
            Reply reply = replyService.updateReply(id, content);
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", reply);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

