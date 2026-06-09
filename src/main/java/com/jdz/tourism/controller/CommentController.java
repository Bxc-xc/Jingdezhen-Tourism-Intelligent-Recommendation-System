package com.jdz.tourism.controller;

import com.jdz.tourism.annotation.LogOperation;
import com.jdz.tourism.entity.Reply;
import com.jdz.tourism.service.CommentService;
import com.jdz.tourism.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论统一接口（含嵌套回复）。列表公开；商家回复需登录且 JWT 对应账号须为商家。
 */
@RestController
@RequestMapping("/api/comment")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired(required = false)
    private com.jdz.tourism.service.RealtimeDataService realtimeDataService;

    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired(required = false)
    private JwtUtil jwtUtil;

    private Long resolveCurrentUserId() {
        try {
            String authorization = request != null ? request.getHeader("Authorization") : null;
            if (authorization != null && authorization.startsWith("Bearer ") && jwtUtil != null) {
                String token = authorization.substring(7);
                if (jwtUtil.validateToken(token)) {
                    return jwtUtil.extractUserId(token);
                }
            }
            String v = request != null ? request.getHeader("X-User-Id") : null;
            if (v != null && !v.isBlank()) {
                return Long.valueOf(v.trim());
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
     * 获取某景点/店铺下的全部用户评论及嵌套回复（商家 + 用户）。
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list(@RequestParam("scenicId") Long scenicId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> threads = commentService.buildCommentThreads(scenicId);
            response.put("success", true);
            response.put("data", threads);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 商家回复某条用户评论（或回复某条回复）。请求体：reviewId、content、可选 parentReplyId。
     */
    @LogOperation("商家回复评论")
    @PostMapping("/reply")
    public ResponseEntity<Map<String, Object>> merchantReply(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = resolveCurrentUserId();
            if (userId == null) {
                response.put("success", false);
                response.put("message", "请先登录");
                return ResponseEntity.status(401).body(response);
            }
            Object rid = body.get("reviewId");
            if (rid == null) {
                response.put("success", false);
                response.put("message", "缺少 reviewId");
                return ResponseEntity.badRequest().body(response);
            }
            Long reviewId = Long.valueOf(rid.toString());
            String content = body.get("content") != null ? body.get("content").toString() : "";
            Long parentReplyId = null;
            if (body.get("parentReplyId") != null && !body.get("parentReplyId").toString().isBlank()) {
                parentReplyId = Long.valueOf(body.get("parentReplyId").toString());
            }
            Reply saved = commentService.merchantReply(userId, reviewId, parentReplyId, content);
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", saved.getId());
            dto.put("reviewId", saved.getReview() != null ? saved.getReview().getId() : reviewId);
            dto.put("content", saved.getContent());
            dto.put("createTime", saved.getCreateTime());
            dto.put("role", "merchant");
            dto.put("parentReplyId", saved.getParentReply() != null ? saved.getParentReply().getId() : null);
            response.put("success", true);
            response.put("message", "回复成功");
            response.put("data", dto);
            // 推送 review 事件，通知商家端和游客端实时刷新
            if (realtimeDataService != null) {
                realtimeDataService.pushReviewUpdate(java.util.Map.of(
                    "reviewId", reviewId,
                    "action", "reply_added",
                    "merchantId", saved.getMerchant() != null ? saved.getMerchant().getId() : null
                ), "update");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
