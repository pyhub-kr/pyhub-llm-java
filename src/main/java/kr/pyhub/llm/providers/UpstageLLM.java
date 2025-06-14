package kr.pyhub.llm.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.pyhub.llm.Config;
import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.exceptions.LLMException;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Upstage Solar LLM implementation.
 * 
 * Supports Korean-optimized models:
 * - solar-1-mini-chat
 * - solar-1-mini-chat-ja
 * 
 * API Endpoint: https://api.upstage.ai/v1/solar/chat/completions
 */
@Slf4j
public class UpstageLLM extends BaseLLM {
    
    private static final String DEFAULT_BASE_URL = "https://api.upstage.ai/";
    private static final String CHAT_ENDPOINT = "v1/solar/chat/completions";
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    
    public UpstageLLM(String model) {
        this(model, Config.fromEnvironment("upstage"));
    }
    
    public UpstageLLM(String model, String apiKey) {
        this(model, Config.builder().apiKey(apiKey).build());
    }
    
    public UpstageLLM(String model, Config config) {
        super(model);
        
        // Config가 null인 경우 처리
        if (config == null) {
            config = Config.builder().build();
        }
        
        String apiKeyFromConfig = config.getApiKey();
        if (apiKeyFromConfig == null || apiKeyFromConfig.trim().isEmpty()) {
            String envKey = System.getenv("UPSTAGE_API_KEY");
            if (envKey == null || envKey.trim().isEmpty()) {
                throw new IllegalArgumentException("API key is required. Set UPSTAGE_API_KEY environment variable or provide it in Config.");
            }
            this.apiKey = envKey;
        } else {
            this.apiKey = apiKeyFromConfig;
        }
        
        this.baseUrl = config.getBaseUrl() != null ? config.getBaseUrl() : DEFAULT_BASE_URL;
        
        // HTTP 클라이언트 설정
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
            
        this.objectMapper = new ObjectMapper();
        
        // Config에서 temperature와 maxTokens 설정
        if (config.getTemperature() != null) {
            withTemperature(config.getTemperature());
        }
        if (config.getMaxTokens() != null) {
            withMaxTokens(config.getMaxTokens());
        }
        
        log.info("Initialized UpstageLLM with model: {}", model);
    }
    
    @Override
    protected LLMReply doAsk(List<Message> messages) {
        try {
            // 요청 본문 생성
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", getModel());
            
            // 메시지 배열 생성
            ArrayNode messagesArray = requestBody.putArray("messages");
            for (Message message : messages) {
                ObjectNode messageNode = messagesArray.addObject();
                messageNode.put("role", message.getRole().toString().toLowerCase());
                messageNode.put("content", message.getContent());
            }
            
            // 선택적 파라미터 추가
            if (getTemperature() != null) {
                requestBody.put("temperature", getTemperature());
            }
            if (getMaxTokens() != null) {
                requestBody.put("max_tokens", getMaxTokens());
            }
            
            // HTTP 요청 생성
            String url = baseUrl.endsWith("/") ? baseUrl + CHAT_ENDPOINT : baseUrl + "/" + CHAT_ENDPOINT;
            RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json")
            );
            
            Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
            
            log.debug("Sending request to Upstage API: {}", url);
            
            // API 호출
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error details";
                    throw new LLMException("Upstage API error: " + response.code() + " - " + errorBody);
                }
                
                String responseBody = response.body().string();
                log.debug("Received response: {}", responseBody);
                
                // 응답 파싱
                ObjectNode responseJson = (ObjectNode) objectMapper.readTree(responseBody);
                
                // 응답에서 필요한 정보 추출
                ObjectNode firstChoice = (ObjectNode) responseJson.get("choices").get(0);
                ObjectNode messageNode = (ObjectNode) firstChoice.get("message");
                String content = messageNode.get("content").asText();
                String finishReason = firstChoice.get("finish_reason").asText();
                
                // 사용량 정보 추출
                LLMReply.Usage usage = null;
                if (responseJson.has("usage")) {
                    ObjectNode usageNode = (ObjectNode) responseJson.get("usage");
                    usage = LLMReply.Usage.builder()
                        .promptTokens(usageNode.get("prompt_tokens").asInt())
                        .completionTokens(usageNode.get("completion_tokens").asInt())
                        .totalTokens(usageNode.get("total_tokens").asInt())
                        .build();
                }
                
                return LLMReply.builder()
                    .text(content)
                    .model(getModel())
                    .finishReason(finishReason)
                    .usage(usage)
                    .build();
            }
            
        } catch (IOException e) {
            throw new LLMException("Failed to communicate with Upstage API", e);
        } catch (Exception e) {
            throw new LLMException("Error processing Upstage API response", e);
        }
    }
}