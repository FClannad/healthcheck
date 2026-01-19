package com.example.service;

import com.example.config.ZhipuAiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 智谱AI服务类
 * 用于调用智谱AI的GLM模型进行健康咨询
 */
@Service
public class ZhipuAiService {
    
    private static final Logger log = LoggerFactory.getLogger(ZhipuAiService.class);
    
    @Autowired
    private ZhipuAiConfig zhipuAiConfig;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private OkHttpClient httpClient;
    
    /**
     * 获取HTTP客户端（懒加载）
     */
    private OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(zhipuAiConfig.getTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(zhipuAiConfig.getTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(zhipuAiConfig.getTimeout(), TimeUnit.MILLISECONDS)
                    .build();
        }
        return httpClient;
    }
    
    /**
     * 健康咨询系统提示词
     */
    private static final String SYSTEM_PROMPT = """
        你是一位专业的健康咨询AI助手，专门为用户提供体检建议和健康指导。
        
        你的职责：
        1. 根据用户描述的症状，推荐合适的体检项目
        2. 解释各种体检项目的作用和意义
        3. 提供健康生活建议
        4. 在必要时建议用户及时就医
        
        回复要求：
        1. 使用简洁、专业但易懂的语言
        2. 回复中要包含具体的体检项目推荐
        3. 适当使用换行和编号使内容更清晰
        4. 在回复末尾添加温馨提示，说明AI建议仅供参考
        
        常见体检项目包括：
        - 基础检查：血常规、尿常规、血压测量
        - 心血管：心电图、心脏彩超、血脂检查、动态心电图
        - 消化系统：腹部B超、肝功能、肾功能、胃镜、肠镜
        - 呼吸系统：胸片、胸部CT、肺功能检查
        - 内分泌：甲状腺功能、血糖、糖化血红蛋白
        - 肿瘤筛查：肿瘤标志物
        - 骨骼：骨密度检查
        - 妇科：妇科常规、宫颈癌筛查、乳腺检查
        - 男科：前列腺检查
        """;
    
    /**
     * 调用智谱AI进行健康咨询
     * 
     * @param userQuestion 用户问题
     * @param conversationHistory 对话历史（可选）
     * @return AI回复内容
     */
    public String chat(String userQuestion, List<String> conversationHistory) {
        if (!zhipuAiConfig.isEnabled()) {
            log.warn("智谱AI服务未启用，使用本地响应");
            return null;
        }
        
        String apiKey = zhipuAiConfig.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("智谱AI API Key未配置，使用本地响应");
            return null;
        }
        
        try {
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统提示
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT);
            messages.add(systemMessage);
            
            // 添加对话历史
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (int i = 0; i < conversationHistory.size(); i++) {
                    Map<String, String> historyMessage = new HashMap<>();
                    historyMessage.put("role", i % 2 == 0 ? "user" : "assistant");
                    historyMessage.put("content", conversationHistory.get(i));
                    messages.add(historyMessage);
                }
            }
            
            // 添加当前用户问题
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userQuestion);
            messages.add(userMessage);
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", zhipuAiConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1024);
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            log.info("调用智谱AI，问题: {}", userQuestion);
            
            // 发送请求
            Request request = new Request.Builder()
                    .url(zhipuAiConfig.getApiUrl())
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();
            
            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("智谱AI请求失败，状态码: {}, 响应: {}", 
                            response.code(), response.body() != null ? response.body().string() : "无响应体");
                    return null;
                }
                
                String responseBody = response.body() != null ? response.body().string() : "";
                log.debug("智谱AI响应: {}", responseBody);
                
                // 解析响应
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode choices = jsonNode.get("choices");
                
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode firstChoice = choices.get(0);
                    JsonNode message = firstChoice.get("message");
                    if (message != null) {
                        String content = message.get("content").asText();
                        log.info("智谱AI回复成功，内容长度: {}", content.length());
                        return content;
                    }
                }
                
                log.error("智谱AI响应格式异常: {}", responseBody);
                return null;
            }
            
        } catch (IOException e) {
            log.error("调用智谱AI失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 简单调用（不带对话历史）
     */
    public String chat(String userQuestion) {
        return chat(userQuestion, null);
    }
    
    /**
     * 流式调用智谱AI（SSE方式）
     *
     * @param userQuestion 用户问题
     * @param conversationHistory 对话历史
     * @param emitter SSE发射器
     */
    public void chatStream(String userQuestion, List<String> conversationHistory, SseEmitter emitter) {
        if (!zhipuAiConfig.isEnabled()) {
            log.warn("智谱AI服务未启用");
            sendErrorAndComplete(emitter, "AI服务未启用");
            return;
        }
        
        String apiKey = zhipuAiConfig.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("智谱AI API Key未配置");
            sendErrorAndComplete(emitter, "AI服务配置错误");
            return;
        }
        
        try {
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统提示
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT);
            messages.add(systemMessage);
            
            // 添加对话历史
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                for (int i = 0; i < conversationHistory.size(); i++) {
                    Map<String, String> historyMessage = new HashMap<>();
                    historyMessage.put("role", i % 2 == 0 ? "user" : "assistant");
                    historyMessage.put("content", conversationHistory.get(i));
                    messages.add(historyMessage);
                }
            }
            
            // 添加当前用户问题
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userQuestion);
            messages.add(userMessage);
            
            // 构建请求体 - 启用流式输出
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", zhipuAiConfig.getModel());
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1024);
            requestBody.put("stream", true);  // 启用流式输出
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            log.info("流式调用智谱AI，问题: {}", userQuestion);
            
            // 发送请求
            Request request = new Request.Builder()
                    .url(zhipuAiConfig.getApiUrl())
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "text/event-stream")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();
            
            // 异步处理流式响应
            getHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.error("流式调用智谱AI失败: {}", e.getMessage());
                    sendErrorAndComplete(emitter, "AI服务调用失败");
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        log.error("智谱AI流式请求失败，状态码: {}", response.code());
                        sendErrorAndComplete(emitter, "AI服务响应错误");
                        return;
                    }
                    
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        StringBuilder fullContent = new StringBuilder();
                        
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6).trim();
                                
                                // 检查是否结束
                                if ("[DONE]".equals(data)) {
                                    // 发送完成信号
                                    try {
                                        emitter.send(SseEmitter.event()
                                                .name("done")
                                                .data(fullContent.toString()));
                                        emitter.complete();
                                    } catch (IOException e) {
                                        log.warn("发送完成信号失败: {}", e.getMessage());
                                    }
                                    break;
                                }
                                
                                try {
                                    // 解析JSON获取内容片段
                                    JsonNode jsonNode = objectMapper.readTree(data);
                                    JsonNode choices = jsonNode.get("choices");
                                    
                                    if (choices != null && choices.isArray() && choices.size() > 0) {
                                        JsonNode delta = choices.get(0).get("delta");
                                        if (delta != null && delta.has("content")) {
                                            String content = delta.get("content").asText();
                                            if (content != null && !content.isEmpty()) {
                                                fullContent.append(content);
                                                // 发送内容片段
                                                emitter.send(SseEmitter.event()
                                                        .name("message")
                                                        .data(content));
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    log.debug("解析流式数据失败: {}", e.getMessage());
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("处理流式响应失败: {}", e.getMessage());
                        sendErrorAndComplete(emitter, "处理响应失败");
                    }
                }
            });
            
        } catch (Exception e) {
            log.error("流式调用智谱AI失败: {}", e.getMessage(), e);
            sendErrorAndComplete(emitter, "AI服务异常");
        }
    }
    
    /**
     * 发送错误并完成SSE
     */
    private void sendErrorAndComplete(SseEmitter emitter, String errorMessage) {
        try {
            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(errorMessage));
            emitter.complete();
        } catch (IOException e) {
            log.warn("发送错误信息失败: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }
    
    /**
     * 检查服务是否可用
     */
    public boolean isAvailable() {
        return zhipuAiConfig.isEnabled()
                && zhipuAiConfig.getApiKey() != null
                && !zhipuAiConfig.getApiKey().isEmpty();
    }
}
