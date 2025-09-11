package com.example.service;

import com.example.entity.MedicalLiterature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 医疗文献智能分类服务
 * 基于关键词和内容自动分类文献
 */
@Service
public class LiteratureClassifierService {

    private static final Logger logger = LoggerFactory.getLogger(LiteratureClassifierService.class);

    // 分类关键词映射
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new HashMap<>();
    
    // 分类权重映射
    private static final Map<String, Integer> CATEGORY_WEIGHTS = new HashMap<>();

    static {
        // 心血管疾病
        CATEGORY_KEYWORDS.put("心血管疾病", Arrays.asList(
            "heart", "cardiac", "cardiovascular", "coronary", "artery", "hypertension", 
            "血压", "心脏", "心血管", "冠心病", "心律", "心肌", "动脉", "静脉"
        ));
        
        // 肿瘤学
        CATEGORY_KEYWORDS.put("肿瘤学", Arrays.asList(
            "cancer", "tumor", "oncology", "carcinoma", "malignant", "chemotherapy", 
            "radiation", "肿瘤", "癌症", "恶性", "化疗", "放疗", "免疫治疗"
        ));
        
        // 神经科学
        CATEGORY_KEYWORDS.put("神经科学", Arrays.asList(
            "brain", "neural", "neurology", "neurological", "alzheimer", "parkinson", 
            "stroke", "大脑", "神经", "阿尔茨海默", "帕金森", "中风", "脑卒中"
        ));
        
        // 内分泌学
        CATEGORY_KEYWORDS.put("内分泌学", Arrays.asList(
            "diabetes", "insulin", "hormone", "endocrine", "thyroid", "glucose", 
            "糖尿病", "胰岛素", "激素", "内分泌", "甲状腺", "血糖"
        ));
        
        // 消化系统
        CATEGORY_KEYWORDS.put("消化系统", Arrays.asList(
            "gastro", "liver", "stomach", "intestine", "digestive", "hepatitis", 
            "胃", "肝", "肠", "消化", "肝炎", "胃炎", "肠炎"
        ));
        
        // 呼吸系统
        CATEGORY_KEYWORDS.put("呼吸系统", Arrays.asList(
            "lung", "respiratory", "pneumonia", "asthma", "copd", "bronchial", 
            "肺", "呼吸", "肺炎", "哮喘", "支气管", "呼吸道"
        ));
        
        // 免疫学
        CATEGORY_KEYWORDS.put("免疫学", Arrays.asList(
            "immune", "immunology", "antibody", "vaccine", "autoimmune", "allergy", 
            "免疫", "抗体", "疫苗", "自身免疫", "过敏", "免疫系统"
        ));
        
        // 感染科
        CATEGORY_KEYWORDS.put("感染科", Arrays.asList(
            "infection", "virus", "bacteria", "pathogen", "antimicrobial", "antibiotic", 
            "感染", "病毒", "细菌", "病原体", "抗菌", "抗生素", "传染病"
        ));
        
        // 精神病学
        CATEGORY_KEYWORDS.put("精神病学", Arrays.asList(
            "mental", "psychiatric", "depression", "anxiety", "schizophrenia", "bipolar", 
            "精神", "抑郁", "焦虑", "精神分裂", "双相", "心理健康"
        ));
        
        // 人工智能医疗
        CATEGORY_KEYWORDS.put("人工智能医疗", Arrays.asList(
            "artificial intelligence", "machine learning", "deep learning", "ai", "ml", 
            "neural network", "computer vision", "natural language processing", 
            "人工智能", "机器学习", "深度学习", "神经网络", "计算机视觉", "自然语言处理"
        ));
        
        // 医疗设备
        CATEGORY_KEYWORDS.put("医疗设备", Arrays.asList(
            "medical device", "imaging", "mri", "ct", "ultrasound", "x-ray", 
            "医疗设备", "影像", "核磁共振", "CT", "超声", "X射线", "医疗器械"
        ));
        
        // 药理学
        CATEGORY_KEYWORDS.put("药理学", Arrays.asList(
            "drug", "pharmaceutical", "pharmacology", "medication", "therapy", "treatment", 
            "药物", "制药", "药理", "药品", "治疗", "疗法"
        ));

        // 设置分类权重（用于处理多个匹配的情况）
        CATEGORY_WEIGHTS.put("心血管疾病", 10);
        CATEGORY_WEIGHTS.put("肿瘤学", 10);
        CATEGORY_WEIGHTS.put("神经科学", 9);
        CATEGORY_WEIGHTS.put("人工智能医疗", 8);
        CATEGORY_WEIGHTS.put("内分泌学", 8);
        CATEGORY_WEIGHTS.put("免疫学", 7);
        CATEGORY_WEIGHTS.put("感染科", 7);
        CATEGORY_WEIGHTS.put("消化系统", 6);
        CATEGORY_WEIGHTS.put("呼吸系统", 6);
        CATEGORY_WEIGHTS.put("精神病学", 6);
        CATEGORY_WEIGHTS.put("医疗设备", 5);
        CATEGORY_WEIGHTS.put("药理学", 5);
    }

    /**
     * 自动分类文献
     */
    public String classifyLiterature(MedicalLiterature literature) {
        try {
            logger.debug("开始分类文献: {}", literature.getTitle());
            
            // 收集所有文本内容
            StringBuilder allText = new StringBuilder();
            
            if (literature.getTitle() != null) {
                allText.append(literature.getTitle()).append(" ");
            }
            if (literature.getAbstractContent() != null) {
                allText.append(literature.getAbstractContent()).append(" ");
            }
            if (literature.getKeywords() != null) {
                allText.append(literature.getKeywords()).append(" ");
            }
            if (literature.getJournal() != null) {
                allText.append(literature.getJournal()).append(" ");
            }
            
            String content = allText.toString().toLowerCase();
            
            // 计算每个分类的匹配分数
            Map<String, Double> categoryScores = new HashMap<>();
            
            for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
                String category = entry.getKey();
                List<String> keywords = entry.getValue();
                
                double score = calculateCategoryScore(content, keywords);
                if (score > 0) {
                    // 应用权重
                    score *= CATEGORY_WEIGHTS.getOrDefault(category, 1);
                    categoryScores.put(category, score);
                }
            }
            
            // 选择得分最高的分类
            String bestCategory = categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("其他");
            
            logger.debug("文献分类结果: {} -> {}", literature.getTitle(), bestCategory);
            
            return bestCategory;
            
        } catch (Exception e) {
            logger.error("文献分类失败", e);
            return "其他";
        }
    }

    /**
     * 计算分类匹配分数
     */
    private double calculateCategoryScore(String content, List<String> keywords) {
        double score = 0.0;
        int totalKeywords = keywords.size();
        int matchedKeywords = 0;
        
        for (String keyword : keywords) {
            if (content.contains(keyword.toLowerCase())) {
                matchedKeywords++;
                
                // 标题中的关键词权重更高
                if (content.indexOf(keyword.toLowerCase()) < 200) { // 假设标题在前200个字符内
                    score += 2.0;
                } else {
                    score += 1.0;
                }
                
                // 完整词匹配权重更高
                if (Pattern.compile("\\b" + Pattern.quote(keyword.toLowerCase()) + "\\b").matcher(content).find()) {
                    score += 0.5;
                }
            }
        }
        
        // 计算匹配率加成
        double matchRate = (double) matchedKeywords / totalKeywords;
        score *= (1 + matchRate);
        
        return score;
    }

    /**
     * 批量分类文献
     */
    public void classifyLiteratures(List<MedicalLiterature> literatures) {
        logger.info("开始批量分类 {} 篇文献", literatures.size());
        
        for (MedicalLiterature literature : literatures) {
            try {
                String category = classifyLiterature(literature);
                // 移除category字段，分类信息可以通过keywords体现
                // literature.setCategory(category);
                logger.debug("文献 '{}' 分类为: {}", literature.getTitle(), category);
            } catch (Exception e) {
                logger.warn("分类文献失败: {}", literature.getTitle(), e);
                // literature.setCategory("其他");
            }
        }
        
        logger.info("批量分类完成");
    }

    /**
     * 获取所有支持的分类
     */
    public Set<String> getSupportedCategories() {
        return CATEGORY_KEYWORDS.keySet();
    }

    /**
     * 获取分类的关键词
     */
    public List<String> getCategoryKeywords(String category) {
        return CATEGORY_KEYWORDS.getOrDefault(category, new ArrayList<>());
    }

    /**
     * 添加自定义分类规则
     */
    public void addCategoryRule(String category, List<String> keywords, int weight) {
        CATEGORY_KEYWORDS.put(category, keywords);
        CATEGORY_WEIGHTS.put(category, weight);
        logger.info("添加自定义分类规则: {} (权重: {})", category, weight);
    }

    /**
     * 获取分类统计信息
     */
    public Map<String, Integer> getCategoryStatistics(List<MedicalLiterature> literatures) {
        Map<String, Integer> stats = new HashMap<>();
        
        for (MedicalLiterature literature : literatures) {
            // 移除category字段，使用crawl_source作为分类统计
            String category = literature.getCrawlSource();
            if (category == null || category.isEmpty()) {
                category = "未知来源";
            }
            stats.put(category, stats.getOrDefault(category, 0) + 1);
        }
        
        return stats;
    }
}
