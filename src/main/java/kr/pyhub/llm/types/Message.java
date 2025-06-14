package kr.pyhub.llm.types;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    public enum Role {
        @JsonProperty("system")
        SYSTEM,
        
        @JsonProperty("user")
        USER,
        
        @JsonProperty("assistant")
        ASSISTANT,
        
        @JsonProperty("tool")
        TOOL
    }
    
    @JsonProperty("role")
    private Role role;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("tool_call_id")
    private String toolCallId;
    
    public static Message system(String content) {
        return Message.builder()
            .role(Role.SYSTEM)
            .content(content)
            .build();
    }
    
    public static Message user(String content) {
        return Message.builder()
            .role(Role.USER)
            .content(content)
            .build();
    }
    
    public static Message assistant(String content) {
        return Message.builder()
            .role(Role.ASSISTANT)
            .content(content)
            .build();
    }
    
    public static Message tool(String content, String toolCallId) {
        return Message.builder()
            .role(Role.TOOL)
            .content(content)
            .toolCallId(toolCallId)
            .build();
    }
}