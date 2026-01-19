package com.example.controller;

import com.example.common.Result;
import com.example.entity.MedicalLiterature;
import com.example.service.MedicalLiteratureService;
import com.example.crawler.core.Orchestrator;
import com.example.crawler.core.model.CrawlRequest;
import com.example.crawler.core.model.CrawlResult;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * 简化的医疗文献控制器
 * 只保留核心功能：文献查看、爬虫
 */
@RestController
@RequestMapping("/medical-literature")
public class MedicalLiteratureController {

    private static final Logger logger = LoggerFactory.getLogger(MedicalLiteratureController.class);

    @Autowired
    private MedicalLiteratureService medicalLiteratureService;

    @Autowired
    private Orchestrator orchestrator;

    /**
     * 获取文献总数
     */
    @GetMapping("/count")
    public Result getCount() {
        try {
            int count = medicalLiteratureService.getTotalCount();
            return Result.success(count);
        } catch (Exception e) {
            logger.error("获取文献总数失败", e);
            return Result.error("获取文献总数失败");
        }
    }

    /**
     * 搜索文献
     */
    @GetMapping("/search")
    public Result searchLiterature(@RequestParam String keyword,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        try {
            PageInfo<MedicalLiterature> pageInfo = medicalLiteratureService.searchLiterature(
                keyword, null, null, null, null, page, size);
            return Result.success(pageInfo);
        } catch (Exception e) {
            logger.error("搜索文献失败", e);
            return Result.error("搜索文献失败");
        }
    }

    /**
     * 获取文献详情
     */
    @GetMapping("/{id}")
    public Result getLiterature(@PathVariable Integer id) {
        try {
            MedicalLiterature literature = medicalLiteratureService.selectById(id);
            if (literature != null) {
                return Result.success(literature);
            } else {
                return Result.error("404", "文献不存在");
            }
        } catch (Exception e) {
            logger.error("Get literature failed", e);
            return Result.error("500", "获取文献失败: " + e.getMessage());
        }
    }

    /**
     * 获取文献详情 - 兼容detail路径
     */
    @GetMapping("/detail/{id}")
    public Result getLiteratureDetail(@PathVariable Integer id) {
        return getLiterature(id);
    }

    /**
     * 获取分类统计
     */
    @GetMapping("/categories")
    public Result getCategoryStatistics() {
        try {
            List<Map<String, Object>> stats = medicalLiteratureService.getCategoryStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取分类统计失败", e);
            return Result.error("获取分类统计失败");
        }
    }

    /**
     * List - Get paginated literature list
     */
    @GetMapping("/list")
    public Result getList(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) String source,
                         @RequestParam(required = false) String journal,
                         @RequestParam(defaultValue = "1") Integer pageNum,
                         @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            MedicalLiterature query = new MedicalLiterature();

            // 关键词搜索 - 在标题中搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                query.setTitle(keyword.trim());
            }

            // 来源筛选
            if (source != null && !source.trim().isEmpty()) {
                query.setCrawlSource(source.trim());
            }

            // 期刊筛选
            if (journal != null && !journal.trim().isEmpty()) {
                query.setJournal(journal.trim());
            }

            PageInfo<MedicalLiterature> pageInfo = medicalLiteratureService.selectPage(query, pageNum, pageSize);
            return Result.success(pageInfo);
        } catch (Exception e) {
            logger.error("Get literature list failed", e);
            return Result.error("500", "获取文献列表失败: " + e.getMessage());
        }
    }

    /**
     * 文献爬虫 - 使用新版简化爬虫系统
     */
    @PostMapping("/crawl")
    public Result crawlLiterature(@RequestBody Map<String, Object> request) {
        try {
            String keyword = (String) request.get("keyword");
            Integer count = request.get("count") != null ? (Integer) request.get("count") : 10;

            if (keyword == null || keyword.trim().isEmpty()) {
                return Result.error("400", "关键词不能为空");
            }

            logger.info("Starting literature crawl - Keyword: {}, Count: {}", keyword, count);

            CrawlRequest crawlRequest = new CrawlRequest(keyword, count);
            CrawlResult crawlResult = orchestrator.crawl(crawlRequest);

            Map<String, Object> result = new HashMap<>();
            result.put("keyword", crawlResult.getKeyword());
            result.put("found", crawlResult.getFound());
            result.put("saved", crawlResult.getSaved());
            result.put("duration", crawlResult.getDurationMs());
            result.put("message", crawlResult.getMessage());

            return Result.success(result);

        } catch (Exception e) {
            logger.error("Literature crawl failed", e);
            return Result.error("500", "文献爬取失败: " + e.getMessage());
        }
    }



    /**
     * 管理员获取文献列表
     */
    @GetMapping("/admin/list")
    public Result getAdminList(@RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            PageInfo<MedicalLiterature> pageInfo = medicalLiteratureService.adminSelectPage(
                keyword, null, null, null, pageNum, pageSize);
            return Result.success(pageInfo);
        } catch (Exception e) {
            logger.error("Admin get literature list failed", e);
            return Result.error("500", "获取文献列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除文献
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            medicalLiteratureService.deleteById(id);
            return Result.success("文献删除成功");
        } catch (Exception e) {
            logger.error("Delete literature failed", e);
            return Result.error("500", "删除文献失败: " + e.getMessage());
        }
    }

    /**
     * 更新文献状态
     */
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null || status.trim().isEmpty()) {
                return Result.error("400", "状态不能为空");
            }

            medicalLiteratureService.updateStatus(id, status);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            logger.error("Update literature status failed", e);
            return Result.error("500", "状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新文献信息
     */
    @PutMapping("/update")
    public Result updateLiterature(@RequestBody MedicalLiterature literature) {
        try {
            if (literature.getId() == null) {
                return Result.error("400", "文献ID不能为空");
            }

            medicalLiteratureService.updateById(literature);
            return Result.success("文献更新成功");
        } catch (Exception e) {
            logger.error("Update literature failed", e);
            return Result.error("500", "文献更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取管理员统计数据
     */
    @GetMapping("/admin/stats")
    public Result getAdminStats() {
        try {
            Map<String, Object> stats = medicalLiteratureService.getAdminStats();
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("Get admin stats failed", e);
            return Result.error("500", "获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日爬取数量
     */
    @GetMapping("/count-today")
    public Result getTodayCount() {
        try {
            int count = medicalLiteratureService.getTodayCount();
            return Result.success(count);
        } catch (Exception e) {
            logger.error("获取今日爬取数量失败", e);
            return Result.error("获取今日爬取数量失败");
        }
    }

    /**
     * 获取最近几天的文献数量
     */
    @GetMapping("/count-recent")
    public Result getRecentCount(@RequestParam(defaultValue = "7") int days) {
        try {
            int count = medicalLiteratureService.getRecentCount(days);
            return Result.success(count);
        } catch (Exception e) {
            logger.error("获取最近文献数量失败", e);
            return Result.error("获取最近文献数量失败");
        }
    }
}