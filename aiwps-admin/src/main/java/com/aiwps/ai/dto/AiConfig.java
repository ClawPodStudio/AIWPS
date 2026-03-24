package com.aiwps.ai.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.llm")
public class AiConfig {
    /**
     * .provider: openai, spark, wenxin, zhipu
     */
    private String provider = "openai";
    
    /**
     * API Key
     */
    private String apiKey;
    
    /**
     * API Secret (for Spark/Wenxin)
     */
    private String apiSecret;
    
    /**
     * Base URL (optional, for proxy)
     */
    private String baseUrl;
    
    /**
     * Model name
     */
    private String model = "gpt-3.5-turbo";
    
    /**
     * Max tokens
     */
    private Integer maxTokens = 2048;
    
    /**
     * Temperature
     */
    private Double temperature = 0.7;
    
    /**
     * System prompt for question generation
     */
    private String systemPrompt = "你是一位专业的中学辅导老师，擅长根据知识点出题。";
}
