package com.aiwps.ai.service;

import com.aiwps.ai.dto.AiConfig;
import com.aiwps.ai.dto.AiGeneratedQuestion;
import com.aiwps.ai.dto.AiQuestionRequest;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LlmService {
    
    @Autowired
    private AiConfig aiConfig;
    
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(60))
            .readTimeout(Duration.ofSeconds(120))
            .writeTimeout(Duration.ofSeconds(60))
            .build();
    
    /**
     * 生成题目
     */
    public List<AiGeneratedQuestion> generateQuestions(AiQuestionRequest request) {
        String prompt = buildQuestionPrompt(request);
        
        try {
            String response = callLlm(prompt);
            return parseQuestionResponse(response, request.getCount());
        } catch (Exception e) {
            log.error("AI生成题目失败: {}", e.getMessage());
            throw new RuntimeException("AI生成题目失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成变形题
     */
    public List<AiGeneratedQuestion> generateTransformQuestions(AiQuestionRequest request, String baseQuestionContent) {
        String prompt = buildTransformPrompt(request, baseQuestionContent);
        
        try {
            String response = callLlm(prompt);
            return parseQuestionResponse(response, request.getCount());
        } catch (Exception e) {
            log.error("AI生成变形题失败: {}", e.getMessage());
            throw new RuntimeException("AI生成变形题失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建出题提示词
     */
    private String buildQuestionPrompt(AiQuestionRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(aiConfig.getSystemPrompt()).append("\n\n");
        
        prompt.append("请根据以下要求生成").append(request.getCount()).append("道题目：\n");
        
        if (request.getQuestionType() != null) {
            prompt.append("题型：").append(getQuestionTypeName(request.getQuestionType())).append("\n");
        }
        
        if (request.getDifficulty() != null) {
            prompt.append("难度：").append(getDifficultyName(request.getDifficulty())).append("\n");
        }
        
        prompt.append("\n请严格按照以下JSON格式返回，不要有其他内容：\n");
        prompt.append("[{\"content\":\"题目内容\",\"optionA\":\"选项A\",\"optionB\":\"选项B\",\"optionC\":\"选项C\",\"optionD\":\"选项D\",\"answer\":\"正确答案\",\"analysis\":\"答案解析\",\"difficulty\":难度,\"questionType\":\"题型\",\"tags\":\"标签\"}]\n");
        
        return prompt.toString();
    }
    
    /**
     * 构建变形题提示词
     */
    private String buildTransformPrompt(AiQuestionRequest request, String baseQuestionContent) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(aiConfig.getSystemPrompt()).append("\n\n");
        
        prompt.append("请基于以下题目生成").append(request.getCount()).append("道变形题：\n");
        prompt.append("变形类型：").append(getTransformTypeName(request.getTransformType())).append("\n");
        prompt.append("原题：").append(baseQuestionContent).append("\n");
        
        prompt.append("\n请严格按照以下JSON格式返回，不要有其他内容：\n");
        prompt.append("[{\"content\":\"题目内容\",\"optionA\":\"选项A\",\"optionB\":\"选项B\",\"optionC\":\"选项C\",\"optionD\":\"选项D\",\"answer\":\"正确答案\",\"analysis\":\"答案解析\",\"difficulty\":难度,\"questionType\":\"题型\",\"tags\":\"标签\"}]\n");
        
        return prompt.toString();
    }
    
    /**
     * 调用大模型API
     */
    private String callLlm(String prompt) {
        String provider = aiConfig.getProvider().toLowerCase();
        
        return switch (provider) {
            case "openai" -> callOpenAI(prompt);
            case "spark" -> callSpark(prompt);
            case "wenxin" -> callWenxin(prompt);
            case "zhipu" -> callZhipu(prompt);
            default -> callOpenAI(prompt);
        };
    }
    
    /**
     * 调用OpenAI API
     */
    private String callOpenAI(String prompt) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", aiConfig.getModel());
            requestBody.put("messages", new JSONArray().add(new JSONObject()
                    .put("role", "user")
                    .put("content", prompt)));
            requestBody.put("max_tokens", aiConfig.getMaxTokens());
            requestBody.put("temperature", aiConfig.getTemperature());
            
            RequestBody body = RequestBody.create(
                    requestBody.toJSONString(),
                    MediaType.get("application/json; charset=utf-8"));
            
            String url = aiConfig.getBaseUrl() != null ? 
                    aiConfig.getBaseUrl() + "/v1/chat/completions" : 
                    "https://api.openai.com/v1/chat/completions";
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                    .post(body)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("OpenAI API error: " + response);
                }
                
                JSONObject responseBody = JSON.parseObject(response.body().string());
                JSONArray choices = responseBody.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    return choices.getJSONObject(0).getJSONObject("message").getString("content");
                }
                throw new IOException("Invalid OpenAI response");
            }
        } catch (Exception e) {
            log.error("OpenAI API调用失败: {}", e.getMessage());
            throw new RuntimeException("OpenAI API调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 调用讯飞星火大模型API
     */
    private String callSpark(String prompt) {
        // 讯飞星火API调用需要鉴权，此处为简化实现
        log.warn("Spark API调用需要进一步实现鉴权机制");
        throw new RuntimeException("讯飞星火API暂未实现，请配置OpenAI或其他支持API");
    }
    
    /**
     * 调用百度文心大模型API
     */
    private String callWenxin(String prompt) {
        // 百度文心API调用需要鉴权，此处为简化实现
        log.warn("Wenxin API调用需要进一步实现鉴权机制");
        throw new RuntimeException("百度文心API暂未实现，请配置OpenAI或其他支持API");
    }
    
    /**
     * 调用智谱GLM大模型API
     */
    private String callZhipu(String prompt) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", aiConfig.getModel());
            requestBody.put("messages", new JSONArray().add(new JSONObject()
                    .put("role", "user")
                    .put("content", prompt)));
            requestBody.put("max_tokens", aiConfig.getMaxTokens());
            requestBody.put("temperature", aiConfig.getTemperature());
            
            RequestBody body = RequestBody.create(
                    requestBody.toJSONString(),
                    MediaType.get("application/json; charset=utf-8"));
            
            String url = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                    .post(body)
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Zhipu API error: " + response);
                }
                
                JSONObject responseBody = JSON.parseObject(response.body().string());
                JSONArray choices = responseBody.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    return choices.getJSONObject(0).getJSONObject("message").getString("content");
                }
                throw new IOException("Invalid Zhipu response");
            }
        } catch (Exception e) {
            log.error("智谱GLM API调用失败: {}", e.getMessage());
            throw new RuntimeException("智谱GLM API调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 解析题目响应
     */
    private List<AiGeneratedQuestion> parseQuestionResponse(String response, int expectedCount) {
        List<AiGeneratedQuestion> questions = new ArrayList<>();
        
        try {
            // 尝试提取JSON数组
            String jsonStr = extractJsonArray(response);
            JSONArray array = JSON.parseArray(jsonStr);
            
            for (int i = 0; i < array.size() && i < expectedCount; i++) {
                JSONObject obj = array.getJSONObject(i);
                AiGeneratedQuestion question = new AiGeneratedQuestion();
                question.setContent(obj.getString("content"));
                question.setOptionA(obj.getString("optionA"));
                question.setOptionB(obj.getString("optionB"));
                question.setOptionC(obj.getString("optionC"));
                question.setOptionD(obj.getString("optionD"));
                question.setAnswer(obj.getString("answer"));
                question.setAnalysis(obj.getString("analysis"));
                question.setDifficulty(obj.getInteger("difficulty"));
                question.setQuestionType(obj.getString("questionType"));
                question.setTags(obj.getString("tags"));
                question.setSource("AI生成");
                questions.add(question);
            }
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());
            throw new RuntimeException("解析AI响应失败: " + e.getMessage());
        }
        
        return questions;
    }
    
    /**
     * 从响应中提取JSON数组
     */
    private String extractJsonArray(String response) {
        // 尝试找到JSON数组的开始和结束
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        
        // 如果没有找到数组，尝试解析整个响应
        return response;
    }
    
    private String getQuestionTypeName(String type) {
        return switch (type) {
            case "choice" -> "选择题";
            case "fill_blank" -> "填空题";
            case "essay" -> "简答题";
            case "calculation" -> "计算题";
            default -> type;
        };
    }
    
    private String getDifficultyName(Integer difficulty) {
        return switch (difficulty) {
            case 1 -> "简单";
            case 2 -> "较简单";
            case 3 -> "中等";
            case 4 -> "较难";
            case 5 -> "困难";
            default -> "中等";
        };
    }
    
    private String getTransformTypeName(String type) {
        return switch (type) {
            case "number" -> "数字变化";
            case "condition" -> "条件变化";
            case "reverse" -> "逆向";
            case "extend" -> "拓展";
            case "comprehensive" -> "综合";
            default -> "数字变化";
        };
    }
}
