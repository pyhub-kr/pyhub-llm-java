package kr.pyhub.llm.tools;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * LLM이 호출할 수 있는 도구/함수를 정의하는 인터페이스.
 * 
 * OpenAI의 Function Calling, Anthropic의 Tool Use 등을 지원합니다.
 */
public interface Tool {
    
    /**
     * 도구의 고유 이름
     * 
     * @return 도구 이름
     */
    String getName();
    
    /**
     * 도구에 대한 설명
     * 
     * @return 도구 설명
     */
    String getDescription();
    
    /**
     * 도구의 파라미터를 설명하는 JSON Schema
     * 
     * @return JSON Schema
     */
    JsonNode getSchema();
    
    /**
     * 도구 실행
     * 
     * @param args 도구 인자
     * @return 실행 결과
     */
    ToolResult execute(Map<String, Object> args);
    
    /**
     * 도구가 활성화되어 있는지 확인
     * 
     * @return 활성화 여부
     */
    default boolean isEnabled() {
        return true;
    }
}