package com.example.service;

import com.example.entity.MedicalLiterature;
import com.example.mapper.MedicalLiteratureMapper;
import com.example.utils.SimilarityUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医疗文献服务层
 */
@Service
public class MedicalLiteratureService {

    private static final Logger logger = LoggerFactory.getLogger(MedicalLiteratureService.class);

    @Resource
    private MedicalLiteratureMapper medicalLiteratureMapper;

    @Resource
    private SimilarityUtil similarityUtil;



    /**
     * 添加医疗文献
     */
    public void add(MedicalLiterature literature) {
        literature.setCreateTime(new Date());
        // 确保状态为active
        if (literature.getStatus() == null || literature.getStatus().isEmpty()) {
            literature.setStatus("active");
        }

        medicalLiteratureMapper.insert(literature);
        logger.info("新增医疗文献: {}", literature.getTitle());
    }

    /**
     * 批量添加文献（爬虫使用）
     */
    public int batchAdd(List<MedicalLiterature> literatures) {
        if (literatures == null || literatures.isEmpty()) {
            logger.warn("批量添加文献：列表为空");
            return 0;
        }

        logger.info("开始批量添加文献，总数: {}", literatures.size());
        
        Date now = new Date();
        int addedCount = 0;
        int duplicateCount = 0;
        int errorCount = 0;
        
        for (int i = 0; i < literatures.size(); i++) {
            MedicalLiterature literature = literatures.get(i);
            try {
                logger.debug("处理文献 [{}/{}]: {}", i + 1, literatures.size(), 
                    literature.getTitle() != null ? literature.getTitle().substring(0, Math.min(50, literature.getTitle().length())) + "..." : "No Title");
                
                // 检查是否已存在
                if (!isDuplicate(literature)) {
                    // 设置必要字段
                    literature.setCreateTime(now);
                    // 确保状态为active
                    if (literature.getStatus() == null || literature.getStatus().isEmpty()) {
                        literature.setStatus("active");
                    }
                    
                    // 验证必要字段
                    if (literature.getTitle() == null || literature.getTitle().trim().isEmpty()) {
                        logger.warn("跳过无标题文献: {}", literature);
                        errorCount++;
                        continue;
                    }
                    
                    medicalLiteratureMapper.insert(literature);
                    addedCount++;
                    logger.debug("成功添加文献: {} (ID: {})", literature.getTitle(), literature.getId());
                } else {
                    duplicateCount++;
                    logger.debug("跳过重复文献: {}", literature.getTitle());
                }
            } catch (Exception e) {
                errorCount++;
                logger.error("添加文献失败: {}", literature.getTitle(), e);
            }
        }
        
        logger.info("批量添加文献完成 - 成功: {}, 重复: {}, 错误: {}, 总数: {}", 
            addedCount, duplicateCount, errorCount, literatures.size());
        
        return addedCount;
    }

    /**
     * 检查文献是否重复（使用智能相似度算法）
     */
    private boolean isDuplicate(MedicalLiterature literature) {
        try {
            // 检查标题（精确匹配）
            if (literature.getTitle() != null && !literature.getTitle().trim().isEmpty()) {
                String title = literature.getTitle().trim();
                MedicalLiterature existing = medicalLiteratureMapper.selectByTitle(title);
                if (existing != null) {
                    logger.debug("发现重复标题: {}", title);
                    return true;
                }
            }

            // 使用相似度算法检查近似重复
            if (isSimilarToExisting(literature)) {
                return true;
            }

            logger.debug("文献不重复: {}", literature.getTitle());
            return false;

        } catch (Exception e) {
            logger.error("检查重复失败，允许添加: {}", literature.getTitle(), e);
            return false; // 如果检查失败，允许添加
        }
    }

    /**
     * 检查文献是否与现有文献相似
     */
    private boolean isSimilarToExisting(MedicalLiterature newLiterature) {
        try {
            // 获取最近的文献进行相似度比较（限制数量以提升性能）
            List<MedicalLiterature> recentLiteratures = medicalLiteratureMapper.selectRecentLiteratures(100);

            for (MedicalLiterature existing : recentLiteratures) {
                // 快速预检查
                if (similarityUtil.quickSimilarityCheck(newLiterature.getTitle(), existing.getTitle())) {
                    // 详细相似度检查
                    if (similarityUtil.isSimilar(newLiterature.getTitle(), existing.getTitle(), 0.85)) {
                        logger.debug("发现相似文献: {} <-> {}", newLiterature.getTitle(), existing.getTitle());
                        return true;
                    }
                }
            }

            return false;
        } catch (Exception e) {
            logger.error("相似度检查失败", e);
            return false;
        }
    }

    /**
     * 根据ID查询文献
     */
    public MedicalLiterature selectById(Integer id) {
        return medicalLiteratureMapper.selectById(id);
    }

    /**
     * 查看文献详情
     */
    public MedicalLiterature viewLiterature(Integer id) {
        return medicalLiteratureMapper.selectById(id);
    }


    /**
     * 更新文献信息
     */
    public void updateById(MedicalLiterature literature) {
        medicalLiteratureMapper.updateById(literature);
        logger.info("更新医疗文献: {}", literature.getId());
    }

    /**
     * 删除文献
     */
    public void deleteById(Integer id) {
        medicalLiteratureMapper.deleteById(id);
        logger.info("删除医疗文献: {}", id);
    }

    /**
     * 更新文献状态
     */
    public void updateStatus(Integer id, String status) {
        medicalLiteratureMapper.updateStatus(id, status);
        logger.info("更新医疗文献状态: id={}, status={}", id, status);
    }

    /**
     * 分页查询文献
     */
    public PageInfo<MedicalLiterature> selectPage(MedicalLiterature literature, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalLiterature> list = medicalLiteratureMapper.selectAll(literature);
        return PageInfo.of(list);
    }

    /**
     * 高级搜索文献
     */
    public PageInfo<MedicalLiterature> searchLiterature(String keyword, String category, 
                                                       String language, String startDate, 
                                                       String endDate, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalLiterature> list = medicalLiteratureMapper.searchLiterature(
            keyword, category, language, startDate, endDate);
        
        return PageInfo.of(list);
    }


    /**
     * 获取热门文献（改为最新文献）
     */
    @Cacheable(value = "literatureStats", key = "'popular_' + #limit")
    public List<MedicalLiterature> getPopularLiterature(int limit) {
        return medicalLiteratureMapper.getLatestLiterature(limit);
    }

    /**
     * 获取最新文献
     */
    @Cacheable(value = "literatureStats", key = "'latest_' + #limit")
    public List<MedicalLiterature> getLatestLiterature(int limit) {
        return medicalLiteratureMapper.getLatestLiterature(limit);
    }

    /**
     * 获取推荐文献
     */
    public List<MedicalLiterature> getRecommendedLiterature(String keyword, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getPopularLiterature(limit);
        }
        return medicalLiteratureMapper.getRecommendedLiterature(keyword.trim(), limit);
    }

    /**
     * 获取分类统计
     */
    public List<Map<String, Object>> getCategoryStatistics() {
        return medicalLiteratureMapper.getCategoryStatistics();
    }

    /**
     * 获取热门搜索关键词
     */
    public List<String> getHotSearchKeywords(int limit) {
        // 这里简化实现，实际可以使用Redis的ZREVRANGE命令
        // 返回热门搜索关键词列表
        return List.of("心血管疾病", "肿瘤治疗", "神经科学", "内分泌", "消化系统");
    }

    /**
     * 清理缓存
     */
    @CacheEvict(value = {"medicalLiterature", "literaturePage", "literatureStats"}, allEntries = true)
    public void clearCache() {
        logger.info("清理医疗文献缓存");
    }

    /**
     * 管理员分页查询文献（包含已删除）
     */
    public PageInfo<MedicalLiterature> adminSelectPage(String keyword, String category,
                                                       String source, String status,
                                                       Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        // 构建查询条件
        MedicalLiterature query = new MedicalLiterature();
        if (source != null && !source.trim().isEmpty()) {
            query.setCrawlSource(source);
        }

        List<MedicalLiterature> list = medicalLiteratureMapper.selectAll(query);

        // 关键词过滤
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.toLowerCase();
            list = list.stream()
                    .filter(lit ->
                        (lit.getTitle() != null && lit.getTitle().toLowerCase().contains(searchKeyword)) ||
                        (lit.getAuthors() != null && lit.getAuthors().toLowerCase().contains(searchKeyword)) ||
                        (lit.getKeywords() != null && lit.getKeywords().toLowerCase().contains(searchKeyword))
                    )
                    .collect(java.util.stream.Collectors.toList());
        }

        return PageInfo.of(list);
    }

    /**
     * 获取管理员统计数据
     */
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new java.util.HashMap<>();

        try {
            // 获取总文献数
            MedicalLiterature allQuery = new MedicalLiterature();
            List<MedicalLiterature> allLiteratures = medicalLiteratureMapper.selectAll(allQuery);
            stats.put("totalCount", allLiteratures.size());

            // 获取今日爬取数量
            long todayCount = allLiteratures.stream()
                    .filter(lit -> {
                        if (lit.getCreateTime() == null) return false;
                        long diff = System.currentTimeMillis() - lit.getCreateTime().getTime();
                        return diff < 24 * 60 * 60 * 1000; // 24小时内
                    })
                    .count();
            stats.put("todayCrawled", (int) todayCount);

        } catch (Exception e) {
            logger.error("获取管理员统计数据失败", e);
            stats.put("totalCount", 0);
            stats.put("todayCrawled", 0);
        }

        return stats;
    }



    /**
     * 批量删除文献
     */
    @CacheEvict(value = {"medicalLiterature", "literaturePage", "literatureStats"}, allEntries = true)
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        for (Integer id : ids) {
            deleteById(id);
        }

        logger.info("批量删除文献: ids={}", ids);
    }
    

    /**
     * 获取文献总数
     */
    public int getTotalCount() {
        try {
            return medicalLiteratureMapper.count();
        } catch (Exception e) {
            logger.error("获取文献总数失败", e);
            return 0;
        }
    }
    
    /**
     * 获取来源统计
     */
    public Map<String, Integer> getSourceStatistics() {
        Map<String, Integer> sourceStats = new HashMap<>();

        try {
            MedicalLiterature query = new MedicalLiterature();
            List<MedicalLiterature> allLiteratures = medicalLiteratureMapper.selectAll(query);

            // 统计各来源的文献数量
            for (MedicalLiterature literature : allLiteratures) {
                String source = literature.getCrawlSource() != null ? literature.getCrawlSource() : "未知";
                sourceStats.put(source, sourceStats.getOrDefault(source, 0) + 1);
            }

        } catch (Exception e) {
            logger.error("获取来源统计失败", e);
        }

        return sourceStats;
    }

    /**
     * 获取今日爬取数量
     */
    public int getTodayCount() {
        try {
            return medicalLiteratureMapper.countTodayLiteratures();
        } catch (Exception e) {
            logger.error("获取今日爬取数量失败", e);
            return 0;
        }
    }

    /**
     * 获取最近几天的文献数量
     */
    public int getRecentCount(int days) {
        try {
            MedicalLiterature query = new MedicalLiterature();
            List<MedicalLiterature> allLiteratures = medicalLiteratureMapper.selectAll(query);

            long cutoffTime = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000);

            return (int) allLiteratures.stream()
                    .filter(lit -> lit.getCreateTime() != null && lit.getCreateTime().getTime() >= cutoffTime)
                    .count();
        } catch (Exception e) {
            logger.error("获取最近{}天文献数量失败", days, e);
            return 0;
        }
    }

    /**
     * 搜索文献（优化版本）
     */
    public List<MedicalLiterature> searchLiteratures(String keyword, String source) {
        MedicalLiterature query = new MedicalLiterature();
        if (source != null && !source.trim().isEmpty()) {
            query.setCrawlSource(source);
        }

        List<MedicalLiterature> list = medicalLiteratureMapper.selectAll(query);

        // 关键词过滤和相关性排序
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.toLowerCase();
            list = list.stream()
                    .filter(literature -> isRelevant(literature, searchKeyword))
                    .sorted((a, b) -> Double.compare(calculateRelevanceScore(b, searchKeyword), calculateRelevanceScore(a, searchKeyword)))
                    .collect(java.util.stream.Collectors.toList());
        }

        return list;
    }

    /**
     * 判断文献是否与关键词相关
     */
    private boolean isRelevant(MedicalLiterature literature, String keyword) {
        String lowerKeyword = keyword.toLowerCase();

        // 检查标题
        if (literature.getTitle() != null &&
            literature.getTitle().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        // 检查摘要
        if (literature.getAbstractContent() != null &&
            literature.getAbstractContent().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        // 检查关键词
        if (literature.getKeywords() != null &&
            literature.getKeywords().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        // 检查作者
        if (literature.getAuthors() != null &&
            literature.getAuthors().toLowerCase().contains(lowerKeyword)) {
            return true;
        }

        return false;
    }

    /**
     * 计算相关性评分
     */
    private double calculateRelevanceScore(MedicalLiterature literature, String keyword) {
        double score = 0.0;
        String lowerKeyword = keyword.toLowerCase();

        // 标题匹配权重最高
        if (literature.getTitle() != null) {
            String title = literature.getTitle().toLowerCase();
            if (title.contains(lowerKeyword)) {
                score += 10.0;
                // 完全匹配额外加分
                if (title.equals(lowerKeyword)) {
                    score += 20.0;
                }
                // 开头匹配加分
                if (title.startsWith(lowerKeyword)) {
                    score += 5.0;
                }
            }
        }

        // 关键词匹配
        if (literature.getKeywords() != null &&
            literature.getKeywords().toLowerCase().contains(lowerKeyword)) {
            score += 5.0;
        }

        // 摘要匹配
        if (literature.getAbstractContent() != null &&
            literature.getAbstractContent().toLowerCase().contains(lowerKeyword)) {
            score += 3.0;
        }

        // 作者匹配
        if (literature.getAuthors() != null &&
            literature.getAuthors().toLowerCase().contains(lowerKeyword)) {
            score += 2.0;
        }

        return score;
    }

}