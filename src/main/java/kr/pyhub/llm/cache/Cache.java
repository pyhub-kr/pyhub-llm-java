package kr.pyhub.llm.cache;

import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;

import java.util.List;
import java.util.Optional;

/**
 * LLM 응답 캐싱을 위한 인터페이스.
 * 
 * 동일한 입력에 대해 LLM API 호출을 줄이고 응답 속도를 향상시킵니다.
 */
public interface Cache {
    
    /**
     * 캐시에서 값을 조회합니다.
     * 
     * @param key 캐시 키
     * @return 캐시된 값 또는 빈 Optional
     */
    Optional<LLMReply> get(String key);
    
    /**
     * 캐시에 값을 저장합니다.
     * 
     * @param key 캐시 키
     * @param value 저장할 값
     */
    void put(String key, LLMReply value);
    
    /**
     * 특정 키의 캐시를 삭제합니다.
     * 
     * @param key 삭제할 캐시 키
     */
    void evict(String key);
    
    /**
     * 모든 캐시를 삭제합니다.
     */
    void clear();
    
    /**
     * 메시지와 파라미터로부터 캐시 키를 생성합니다.
     * 
     * @param messages 메시지 리스트
     * @param model 모델명
     * @param temperature 온도 파라미터
     * @param maxTokens 최대 토큰 수
     * @return 생성된 캐시 키
     */
    String generateKey(List<Message> messages, String model, Double temperature, Integer maxTokens);
    
    /**
     * 캐시가 활성화되어 있는지 확인합니다.
     * 
     * @return 활성화 여부
     */
    default boolean isEnabled() {
        return true;
    }
}