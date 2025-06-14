package kr.pyhub.llm.tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 도구 실행 결과.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolResult {
    
    /**
     * 실행 성공 여부
     */
    private boolean success;
    
    /**
     * 출력 결과 (성공 시)
     */
    private String output;
    
    /**
     * 에러 메시지 (실패 시)
     */
    private String error;
    
    /**
     * 추가 메타데이터
     */
    private Object metadata;
    
    /**
     * 성공 결과 생성
     * 
     * @param output 출력 내용
     * @return 성공 결과
     */
    public static ToolResult success(String output) {
        return ToolResult.builder()
            .success(true)
            .output(output)
            .build();
    }
    
    /**
     * 성공 결과 생성 (메타데이터 포함)
     * 
     * @param output 출력 내용
     * @param metadata 메타데이터
     * @return 성공 결과
     */
    public static ToolResult success(String output, Object metadata) {
        return ToolResult.builder()
            .success(true)
            .output(output)
            .metadata(metadata)
            .build();
    }
    
    /**
     * 실패 결과 생성
     * 
     * @param error 에러 메시지
     * @return 실패 결과
     */
    public static ToolResult error(String error) {
        return ToolResult.builder()
            .success(false)
            .error(error)
            .build();
    }
}