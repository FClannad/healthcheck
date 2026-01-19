package com.example.controller;

import com.example.common.Result;
import com.example.entity.AiConsultation;
import com.example.entity.dto.AiConsultationRequest;
import com.example.entity.dto.AiConsultationResponse;
import com.example.service.AiConsultationService;
import com.example.service.ZhipuAiService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * AI咨询控制器
 *
 * 【未使用接口说明】
 * 此控制器的所有接口目前前端未调用，前端AiConsultation.vue使用本地模拟响应。
 * 保留此控制器以便后续接入真实AI服务时使用。
 */
@Tag(name = "AI咨询管理")
@RestController
@RequestMapping("/ai-consultation")
public class AiConsultationController {
    
    @Autowired
    private AiConsultationService aiConsultationService;
    
    @Autowired
    private ZhipuAiService zhipuAiService;
    
    /**
     * AI健康咨询
     */
    @Operation(summary = "AI健康咨询")
    @PostMapping("/consult")
    public Result consult(@RequestBody AiConsultationRequest request) {
        try {
            // 从Token中获取当前用户ID
            Integer currentUserId = TokenUtils.getCurrentUser().getId();
            request.setUserId(currentUserId);
            
            AiConsultationResponse response = aiConsultationService.consultWithAi(request);
            return Result.success(response);
            
        } catch (Exception e) {
            return Result.error("AI咨询失败: " + e.getMessage());
        }
    }
    
    /**
     * AI健康咨询 - 流式输出
     * 注意：此接口通过URL参数传递token进行验证，因为EventSource不支持自定义Header
     */
    @Operation(summary = "AI健康咨询-流式输出")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter consultStream(@RequestParam String question,
                                    @RequestParam(required = false) String sessionId,
                                    @RequestParam(required = false) String token) {
        // 创建SSE发射器，超时时间60秒
        SseEmitter emitter = new SseEmitter(60000L);
        
        // 设置超时和错误处理
        emitter.onTimeout(() -> {
            emitter.complete();
        });
        emitter.onError((e) -> {
            emitter.complete();
        });
        
        try {
            // 通过token验证用户身份
            Integer userId = null;
            if (token != null && !token.isEmpty()) {
                try {
                    userId = TokenUtils.getUserIdFromToken(token);
                } catch (Exception e) {
                    // token验证失败，继续执行但不获取历史记录
                }
            }
            
            // 获取对话历史
            List<String> conversationHistory = new ArrayList<>();
            if (userId != null && sessionId != null && !sessionId.isEmpty()) {
                try {
                    List<AiConsultation> history = aiConsultationService.getUserConsultationHistory(userId);
                    // 只取最近5轮对话作为上下文
                    int start = Math.max(0, history.size() - 10);
                    for (int i = start; i < history.size(); i++) {
                        AiConsultation consultation = history.get(i);
                        conversationHistory.add(consultation.getUserQuestion());
                        conversationHistory.add(consultation.getAiResponse());
                    }
                } catch (Exception e) {
                    // 忽略历史记录获取失败
                }
            }
            
            // 调用流式AI服务
            zhipuAiService.chatStream(question, conversationHistory, emitter);
            
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data("服务异常: " + e.getMessage()));
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        }
        
        return emitter;
    }
    
    /**
     * 保存流式对话记录
     * 在流式输出完成后由前端调用保存
     */
    @Operation(summary = "保存流式对话记录")
    @PostMapping("/save-stream")
    public Result saveStreamConsultation(@RequestBody AiConsultationRequest request) {
        try {
            // 从Token中获取当前用户ID
            Integer currentUserId = TokenUtils.getCurrentUser().getId();
            
            // 保存咨询记录
            aiConsultationService.saveConsultation(
                currentUserId,
                request.getQuestion(),
                request.getResponse(),
                request.getSessionId()
            );
            
            return Result.success();
        } catch (Exception e) {
            return Result.error("保存对话记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户咨询历史
     */
    @Operation(summary = "获取用户咨询历史")
    @GetMapping("/history")
    public Result getUserHistory() {
        try {
            Integer currentUserId = TokenUtils.getCurrentUser().getId();
            List<AiConsultation> history = aiConsultationService.getUserConsultationHistory(currentUserId);
            return Result.success(history);
            
        } catch (Exception e) {
            return Result.error("获取咨询历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取咨询记录详情
     */
    @Operation(summary = "获取咨询记录详情")
    @GetMapping("/{id}")
    public Result getConsultationDetail(@PathVariable Integer id) {
        try {
            AiConsultation consultation = aiConsultationService.getConsultationById(id);
            
            // 检查权限：只能查看自己的咨询记录
            Integer currentUserId = TokenUtils.getCurrentUser().getId();
            if (!consultation.getUserId().equals(currentUserId)) {
                return Result.error("无权访问此咨询记录");
            }
            
            return Result.success(consultation);
            
        } catch (Exception e) {
            return Result.error("获取咨询详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除咨询记录
     */
    @Operation(summary = "删除咨询记录")
    @DeleteMapping("/{id}")
    public Result deleteConsultation(@PathVariable Integer id) {
        try {
            // 检查权限：只能删除自己的咨询记录
            AiConsultation consultation = aiConsultationService.getConsultationById(id);
            Integer currentUserId = TokenUtils.getCurrentUser().getId();
            
            if (!consultation.getUserId().equals(currentUserId)) {
                return Result.error("无权删除此咨询记录");
            }
            
            aiConsultationService.deleteConsultation(id);
            return Result.success();
            
        } catch (Exception e) {
            return Result.error("删除咨询记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户咨询统计
     */
    @Operation(summary = "获取用户咨询统计")
    @GetMapping("/stats")
    public Result getUserStats() {
        try {
            Integer currentUserId = TokenUtils.getCurrentUser().getId();
            Integer count = aiConsultationService.getUserConsultationCount(currentUserId);
            
            return Result.success(count);
            
        } catch (Exception e) {
            return Result.error("获取咨询统计失败: " + e.getMessage());
        }
    }
    
    // ========== 管理员接口 ==========
    
    /**
     * 分页查询所有咨询记录（管理员）
     */
    @Operation(summary = "分页查询所有咨询记录")
    @GetMapping("/admin/page")
    public Result getConsultationPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 检查管理员权限
            String role = TokenUtils.getCurrentUser().getRole();
            if (!"ADMIN".equals(role)) {
                return Result.error("权限不足");
            }
            
            PageInfo<AiConsultation> pageInfo = aiConsultationService.selectPage(pageNum, pageSize);
            return Result.success(pageInfo);
            
        } catch (Exception e) {
            return Result.error("查询咨询记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员删除咨询记录
     */
    @Operation(summary = "管理员删除咨询记录")
    @DeleteMapping("/admin/{id}")
    public Result adminDeleteConsultation(@PathVariable Integer id) {
        try {
            // 检查管理员权限
            String role = TokenUtils.getCurrentUser().getRole();
            if (!"ADMIN".equals(role)) {
                return Result.error("权限不足");
            }
            
            aiConsultationService.deleteConsultation(id);
            return Result.success();
            
        } catch (Exception e) {
            return Result.error("删除咨询记录失败: " + e.getMessage());
        }
    }
}
