package kr.pyhub.llm.cache;

import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 캐시 인터페이스 테스트
 */
class CacheTest {
    
    private Cache cache;
    
    @BeforeEach
    void setUp() {
        cache = new TestCache();
    }
    
    @Test
    @DisplayName("캐시에 값을 저장하고 조회할 수 있어야 함")
    void shouldStoreAndRetrieveValues() {
        // Given
        String key = "test-key";
        LLMReply value = LLMReply.builder()
            .text("Hello from cache")
            .model("test-model")
            .build();
        
        // When
        cache.put(key, value);
        Optional<LLMReply> retrieved = cache.get(key);
        
        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getText()).isEqualTo("Hello from cache");
    }
    
    @Test
    @DisplayName("존재하지 않는 키는 빈 Optional을 반환해야 함")
    void shouldReturnEmptyForNonExistentKey() {
        // When
        Optional<LLMReply> retrieved = cache.get("non-existent");
        
        // Then
        assertThat(retrieved).isEmpty();
    }
    
    @Test
    @DisplayName("메시지 리스트로부터 캐시 키를 생성할 수 있어야 함")
    void shouldGenerateCacheKeyFromMessages() {
        // Given
        List<Message> messages = Arrays.asList(
            Message.system("You are a helpful assistant"),
            Message.user("Hello!")
        );
        String model = "gpt-3.5-turbo";
        Double temperature = 0.7;
        Integer maxTokens = 100;
        
        // When
        String key1 = cache.generateKey(messages, model, temperature, maxTokens);
        String key2 = cache.generateKey(messages, model, temperature, maxTokens);
        
        // Then
        assertThat(key1).isEqualTo(key2);
        assertThat(key1).isNotEmpty();
    }
    
    @Test
    @DisplayName("다른 파라미터는 다른 캐시 키를 생성해야 함")
    void shouldGenerateDifferentKeysForDifferentParameters() {
        // Given
        List<Message> messages = Arrays.asList(
            Message.user("Hello!")
        );
        
        // When
        String key1 = cache.generateKey(messages, "model1", 0.7, 100);
        String key2 = cache.generateKey(messages, "model2", 0.7, 100);
        String key3 = cache.generateKey(messages, "model1", 0.8, 100);
        String key4 = cache.generateKey(messages, "model1", 0.7, 200);
        
        // Then
        assertThat(key1).isNotEqualTo(key2);
        assertThat(key1).isNotEqualTo(key3);
        assertThat(key1).isNotEqualTo(key4);
    }
    
    @Test
    @DisplayName("캐시를 삭제할 수 있어야 함")
    void shouldEvictCache() {
        // Given
        String key = "test-key";
        LLMReply value = LLMReply.builder()
            .text("To be evicted")
            .model("test-model")
            .build();
        cache.put(key, value);
        
        // When
        cache.evict(key);
        Optional<LLMReply> retrieved = cache.get(key);
        
        // Then
        assertThat(retrieved).isEmpty();
    }
    
    @Test
    @DisplayName("전체 캐시를 비울 수 있어야 함")
    void shouldClearAllCache() {
        // Given
        cache.put("key1", LLMReply.builder().text("Value 1").build());
        cache.put("key2", LLMReply.builder().text("Value 2").build());
        cache.put("key3", LLMReply.builder().text("Value 3").build());
        
        // When
        cache.clear();
        
        // Then
        assertThat(cache.get("key1")).isEmpty();
        assertThat(cache.get("key2")).isEmpty();
        assertThat(cache.get("key3")).isEmpty();
    }
    
    /**
     * 테스트용 캐시 구현
     */
    static class TestCache extends AbstractCache {
        private final java.util.Map<String, LLMReply> storage = new java.util.HashMap<>();
        
        @Override
        public Optional<LLMReply> get(String key) {
            return Optional.ofNullable(storage.get(key));
        }
        
        @Override
        public void put(String key, LLMReply value) {
            storage.put(key, value);
        }
        
        @Override
        public void evict(String key) {
            storage.remove(key);
        }
        
        @Override
        public void clear() {
            storage.clear();
        }
    }
}