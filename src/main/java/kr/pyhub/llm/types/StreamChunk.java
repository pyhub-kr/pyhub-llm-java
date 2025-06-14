package kr.pyhub.llm.types;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 스트리밍 응답의 각 청크를 나타내는 클래스.
 * 
 * LLM의 스트리밍 응답에서 각각의 토큰이나 텍스트 조각을 담습니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamChunk {
    
    /**
     * 청크의 텍스트 내용
     */
    private String content;
    
    /**
     * 스트림이 완료되었는지 여부
     */
    @Builder.Default
    private boolean finished = false;
    
    /**
     * 완료 이유 (stop, length, content_filter 등)
     */
    private String finishReason;
    
    /**
     * 청크의 인덱스 (선택사항)
     */
    private Integer index;
    
    /**
     * 델타 타입 (text, function_call 등)
     */
    @Builder.Default
    private String type = "text";
    
    /**
     * 추가 메타데이터
     */
    private Object metadata;
    
    /**
     * 텍스트 청크 생성 헬퍼 메서드
     */
    public static StreamChunk text(String content) {
        return StreamChunk.builder()
            .content(content)
            .type("text")
            .finished(false)
            .build();
    }
    
    /**
     * 완료 청크 생성 헬퍼 메서드
     */
    public static StreamChunk finish(String reason) {
        return StreamChunk.builder()
            .content("")
            .finished(true)
            .finishReason(reason)
            .build();
    }
}