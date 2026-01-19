package com.example.service;

import com.example.entity.AiConsultation;
import com.example.entity.ExaminationPackage;
import com.example.entity.dto.AiConsultationRequest;
import com.example.entity.dto.AiConsultationResponse;
import com.example.mapper.AiConsultationMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI咨询业务服务类
 */
@Service
public class AiConsultationService {
    
    private static final Logger log = LoggerFactory.getLogger(AiConsultationService.class);
    
    @Autowired
    private AiConsultationMapper aiConsultationMapper;
    
    @Autowired
    private ZhipuAiService zhipuAiService;  // 智谱AI服务

    @Autowired
    private ExaminationPackageService examinationPackageService;
    
    /**
     * 处理AI咨询请求
     */
    public AiConsultationResponse consultWithAi(AiConsultationRequest request) {
        try {
            // 获取对话历史（如果有sessionId）
            List<String> conversationHistory = new ArrayList<>();
            if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
                try {
                    List<AiConsultation> history = aiConsultationMapper.selectBySessionId(request.getSessionId());
                    for (AiConsultation consultation : history) {
                        conversationHistory.add(consultation.getUserQuestion());
                        conversationHistory.add(consultation.getAiResponse());
                    }
                } catch (Exception e) {
                    log.warn("获取对话历史失败（可能表不存在）: {}", e.getMessage());
                    // 继续执行，不影响AI响应
                }
            }
            
            // 优先使用智谱AI服务，如果不可用则降级到本地响应
            String aiResponse = null;
            if (zhipuAiService.isAvailable()) {
                log.info("使用智谱AI服务处理咨询请求");
                aiResponse = zhipuAiService.chat(request.getQuestion(), conversationHistory);
            }
            
            // 如果AI服务调用失败，使用本地响应
            if (aiResponse == null || aiResponse.isEmpty()) {
                log.info("智谱AI服务不可用或调用失败，使用本地响应");
                aiResponse = generateLocalAiResponse(request.getQuestion());
            }
            
            // 解析推荐的体检项目
            List<String> recommendedExams = extractRecommendedExams(aiResponse);

            // 根据推荐的体检项目匹配体检套餐
            List<ExaminationPackage> recommendedPackages = recommendPackagesByExams(recommendedExams);

            // 生成或使用现有的sessionId
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = generateSessionId();
            }

            // 尝试保存咨询记录（如果表不存在则跳过）
            try {
                AiConsultation consultation = new AiConsultation();
                consultation.setUserId(request.getUserId());
                consultation.setUserQuestion(request.getQuestion());
                consultation.setAiResponse(aiResponse);
                consultation.setRecommendedExams(String.join(",", recommendedExams));
                consultation.setSessionId(sessionId);
                consultation.setStatus("active");
                consultation.setCreateTime(new Date());

                aiConsultationMapper.insert(consultation);
            } catch (Exception e) {
                log.warn("保存AI咨询记录失败（可能表不存在）: {}", e.getMessage());
                // 继续执行，不影响AI响应返回
            }

            // 构建响应
            AiConsultationResponse response = new AiConsultationResponse();
            response.setResponse(aiResponse);
            response.setRecommendedExams(recommendedExams);
            response.setRecommendedPackages(recommendedPackages);
            response.setSessionId(sessionId);
            response.setNeedMoreInfo(checkIfNeedMoreInfo(aiResponse));
            response.setFollowUpQuestion(generateFollowUpQuestion(aiResponse));
            
            return response;
            
        } catch (Exception e) {
            log.error("AI咨询处理失败: {}", e.getMessage(), e);
            
            // 返回默认响应
            AiConsultationResponse errorResponse = new AiConsultationResponse();
            errorResponse.setResponse("抱歉，AI咨询服务暂时不可用，建议您直接预约医生进行咨询。");
            errorResponse.setRecommendedExams(Arrays.asList("基础体检套餐"));
            errorResponse.setSessionId(request.getSessionId());
            
            return errorResponse;
        }
    }
    
    /**
     * 从AI响应中提取推荐的体检项目
     */
    private List<String> extractRecommendedExams(String aiResponse) {
        List<String> exams = new ArrayList<>();
        
        // 定义体检项目关键词
        String[] examKeywords = {
            "血常规", "尿常规", "心电图", "胸片", "胸部CT", "腹部B超",
            "心脏彩超", "动态心电图", "血脂检查", "肝功能", "肾功能",
            "甲状腺功能", "血糖", "糖化血红蛋白", "肿瘤标志物",
            "胃镜", "肠镜", "肺功能检查", "骨密度检查",
            "妇科常规", "宫颈癌筛查", "乳腺检查", "前列腺检查",
            "基础体检套餐", "心血管检查", "消化系统检查", "呼吸系统检查"
        };
        
        // 在AI响应中查找体检项目
        for (String keyword : examKeywords) {
            if (aiResponse.contains(keyword)) {
                exams.add(keyword);
            }
        }
        
        // 如果没有找到具体项目，添加基础体检
        if (exams.isEmpty()) {
            exams.add("基础体检套餐");
        }
        
        return exams;
    }
    
    /**
     * 检查是否需要更多信息
     */
    private Boolean checkIfNeedMoreInfo(String aiResponse) {
        String[] needMoreInfoKeywords = {
            "需要更多信息", "请详细描述", "具体症状", "多长时间了", "还有其他症状吗"
        };
        
        for (String keyword : needMoreInfoKeywords) {
            if (aiResponse.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 生成后续问题建议
     */
    private String generateFollowUpQuestion(String aiResponse) {
        if (checkIfNeedMoreInfo(aiResponse)) {
            return "请详细描述您的症状，包括持续时间、严重程度等信息。";
        }
        return null;
    }
    
    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 获取用户咨询历史
     */
    public List<AiConsultation> getUserConsultationHistory(Integer userId) {
        return aiConsultationMapper.selectByUserId(userId);
    }
    
    /**
     * 获取咨询记录详情
     */
    public AiConsultation getConsultationById(Integer id) {
        return aiConsultationMapper.selectById(id);
    }
    
    /**
     * 分页查询所有咨询记录（管理员用）
     */
    public PageInfo<AiConsultation> selectPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AiConsultation> list = aiConsultationMapper.selectAll();
        return PageInfo.of(list);
    }
    
    /**
     * 删除咨询记录
     */
    public void deleteConsultation(Integer id) {
        aiConsultationMapper.deleteById(id);
    }
    
    /**
     * 获取用户咨询统计
     */
    public Integer getUserConsultationCount(Integer userId) {
        return aiConsultationMapper.countByUserId(userId);
    }
    
    /**
     * 保存流式对话记录
     */
    public void saveConsultation(Integer userId, String question, String response, String sessionId) {
        try {
            // 提取推荐的体检项目
            List<String> recommendedExams = extractRecommendedExams(response);
            
            // 生成或使用现有的sessionId
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = generateSessionId();
            }
            
            AiConsultation consultation = new AiConsultation();
            consultation.setUserId(userId);
            consultation.setUserQuestion(question);
            consultation.setAiResponse(response);
            consultation.setRecommendedExams(String.join(",", recommendedExams));
            consultation.setSessionId(sessionId);
            consultation.setStatus("active");
            consultation.setCreateTime(new Date());
            
            aiConsultationMapper.insert(consultation);
            log.info("保存流式对话记录成功，用户ID: {}, sessionId: {}", userId, sessionId);
        } catch (Exception e) {
            log.error("保存流式对话记录失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据推荐的体检项目匹配体检套餐
     */
    private List<ExaminationPackage> recommendPackagesByExams(List<String> recommendedExams) {
        try {
            // 获取所有体检套餐
            List<ExaminationPackage> allPackages = examinationPackageService.selectAll(new ExaminationPackage());
            List<ExaminationPackage> matchedPackages = new ArrayList<>();

            // 定义症状到套餐的映射关系
            Map<String, List<String>> symptomToPackageMap = buildSymptomToPackageMapping();

            // 根据推荐的体检项目匹配套餐
            for (ExaminationPackage pkg : allPackages) {
                int matchScore = calculatePackageMatchScore(pkg, recommendedExams, symptomToPackageMap);
                if (matchScore > 0) {
                    matchedPackages.add(pkg);
                }
            }

            // 按匹配度排序，返回前3个最匹配的套餐
            matchedPackages.sort((p1, p2) -> {
                int score1 = calculatePackageMatchScore(p1, recommendedExams, symptomToPackageMap);
                int score2 = calculatePackageMatchScore(p2, recommendedExams, symptomToPackageMap);
                return Integer.compare(score2, score1);
            });

            return matchedPackages.stream().limit(3).toList();

        } catch (Exception e) {
            log.error("推荐体检套餐失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建症状到套餐的映射关系
     */
    private Map<String, List<String>> buildSymptomToPackageMapping() {
        Map<String, List<String>> mapping = new HashMap<>();

        // 心血管相关
        mapping.put("心电图", Arrays.asList("心血管体检套餐", "全身体检套餐", "中老年体检套餐"));
        mapping.put("心脏彩超", Arrays.asList("心血管体检套餐", "全身体检套餐"));
        mapping.put("血脂检查", Arrays.asList("心血管体检套餐", "代谢综合征套餐", "全身体检套餐"));

        // 消化系统相关
        mapping.put("胃镜", Arrays.asList("消化系统套餐", "肿瘤筛查套餐", "全身体检套餐"));
        mapping.put("肠镜", Arrays.asList("消化系统套餐", "肿瘤筛查套餐"));
        mapping.put("肝功能", Arrays.asList("消化系统套餐", "基础体检套餐", "全身体检套餐"));
        mapping.put("腹部B超", Arrays.asList("消化系统套餐", "基础体检套餐", "全身体检套餐"));

        // 呼吸系统相关
        mapping.put("胸部CT", Arrays.asList("呼吸系统套餐", "肿瘤筛查套餐", "全身体检套餐"));
        mapping.put("肺功能检查", Arrays.asList("呼吸系统套餐", "职业病筛查套餐"));
        mapping.put("胸片", Arrays.asList("基础体检套餐", "呼吸系统套餐"));

        // 基础检查
        mapping.put("血常规", Arrays.asList("基础体检套餐", "全身体检套餐", "入职体检套餐"));
        mapping.put("尿常规", Arrays.asList("基础体检套餐", "全身体检套餐", "入职体检套餐"));
        mapping.put("血压测量", Arrays.asList("基础体检套餐", "心血管体检套餐", "全身体检套餐"));

        // 内分泌相关
        mapping.put("甲状腺功能", Arrays.asList("内分泌套餐", "全身体检套餐", "女性专项套餐"));
        mapping.put("血糖", Arrays.asList("代谢综合征套餐", "内分泌套餐", "基础体检套餐"));
        mapping.put("糖化血红蛋白", Arrays.asList("糖尿病专项套餐", "代谢综合征套餐"));

        // 肿瘤筛查
        mapping.put("肿瘤标志物", Arrays.asList("肿瘤筛查套餐", "全身体检套餐", "中老年体检套餐"));

        // 妇科相关
        mapping.put("妇科常规", Arrays.asList("女性专项套餐", "妇科体检套餐"));
        mapping.put("宫颈癌筛查", Arrays.asList("女性专项套餐", "妇科体检套餐"));
        mapping.put("乳腺检查", Arrays.asList("女性专项套餐", "妇科体检套餐"));

        // 男科相关
        mapping.put("前列腺检查", Arrays.asList("男性专项套餐", "中老年体检套餐"));

        return mapping;
    }

    /**
     * 计算套餐匹配分数
     */
    private int calculatePackageMatchScore(ExaminationPackage pkg, List<String> recommendedExams,
                                         Map<String, List<String>> symptomToPackageMap) {
        int score = 0;
        String packageName = pkg.getName();

        for (String exam : recommendedExams) {
            List<String> matchingPackages = symptomToPackageMap.get(exam);
            if (matchingPackages != null) {
                for (String matchingPackage : matchingPackages) {
                    if (packageName.contains(matchingPackage) || matchingPackage.contains(packageName)) {
                        score += 10; // 精确匹配得分更高
                    } else if (packageName.contains("全身") || packageName.contains("综合")) {
                        score += 5; // 全身套餐通用性较高
                    } else if (containsKeywords(packageName, exam)) {
                        score += 3; // 关键词匹配
                    }
                }
            }
        }

        return score;
    }

    /**
     * 检查套餐名称是否包含相关关键词
     */
    private boolean containsKeywords(String packageName, String exam) {
        String[] keywords = {"心血管", "消化", "呼吸", "内分泌", "妇科", "男科", "肿瘤", "基础"};
        for (String keyword : keywords) {
            if (packageName.contains(keyword) && exam.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成本地AI响应
     */
    private String generateLocalAiResponse(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "请描述您的症状，我会为您提供相应的体检建议。";
        }

        String lowerQuestion = question.toLowerCase();

        if (lowerQuestion.contains("头痛") || lowerQuestion.contains("头疼")) {
            return "根据您的头痛症状，建议进行以下检查：\n" +
                   "1. 血压测量\n" +
                   "2. 血常规检查\n" +
                   "3. 头部CT扫描\n" +
                   "4. 颈椎X光检查\n\n" +
                   "推荐体检套餐：神经系统专项体检";
        }

        if (lowerQuestion.contains("胸闷") || lowerQuestion.contains("胸痛")) {
            return "根据您的胸闷症状，建议进行以下检查：\n" +
                   "1. 心电图检查\n" +
                   "2. 心脏彩超\n" +
                   "3. 胸部CT\n" +
                   "4. 血脂检查\n\n" +
                   "推荐体检套餐：心血管专项体检";
        }

        if (lowerQuestion.contains("腹痛") || lowerQuestion.contains("肚子疼")) {
            return "根据您的腹痛症状，建议进行以下检查：\n" +
                   "1. 腹部B超\n" +
                   "2. 血常规检查\n" +
                   "3. 肝功能检查\n" +
                   "4. 胃镜检查\n\n" +
                   "推荐体检套餐：消化系统专项体检";
        }

        // 默认建议
        return "感谢您的咨询。建议您：\n" +
               "1. 定期进行健康体检\n" +
               "2. 保持良好的生活习惯\n" +
               "3. 如有不适及时就医\n\n" +
               "推荐体检套餐：全面健康体检套餐\n\n" +
               "温馨提示：以上建议仅供参考，具体诊断请咨询专业医生。";
    }
}
