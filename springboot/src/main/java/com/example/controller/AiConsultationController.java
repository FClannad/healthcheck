package com.example.controller;

import com.example.common.Result;
import com.example.entity.AiConsultation;
import com.example.entity.dto.AiConsultationRequest;
import com.example.entity.dto.AiConsultationResponse;
import com.example.service.AiConsultationService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI咨询控制器
 */
@Tag(name = "AI咨询管理")
@RestController
@RequestMapping("/ai-consultation")
public class AiConsultationController {
    
    @Autowired
    private AiConsultationService aiConsultationService;
    
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
