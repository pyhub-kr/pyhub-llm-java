package kr.pyhub.llm.types;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMReply {
    
    @JsonProperty("text")
    private String text;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("usage")
    private Usage usage;
    
    @JsonProperty("finish_reason")
    private String finishReason;
    
    @JsonProperty("tools_called")
    private List<ToolCall> toolsCalled;
    
    @JsonProperty("raw_response")
    private Map<String, Object> rawResponse;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        
        @JsonProperty("completion_tokens")
        private int completionTokens;
        
        @JsonProperty("total_tokens")
        private int totalTokens;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ToolCall {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("type")
        private String type;
        
        @JsonProperty("function")
        private FunctionCall function;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class FunctionCall {
            @JsonProperty("name")
            private String name;
            
            @JsonProperty("arguments")
            private String arguments;
        }
    }
}