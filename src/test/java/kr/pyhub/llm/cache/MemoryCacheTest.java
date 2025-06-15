package kr.pyhub.llm.cache;

import kr.pyhub.llm.types.LLMReply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 메모리 캐시 테스트
 */
class MemoryCacheTest {
    
    private MemoryCache cache;
    
    @BeforeEach
    void setUp() {
        cache = new MemoryCache();
    }
    
    @Test
    @DisplayName("기본 설정으로 캐시가 동작해야 함")
    void shouldWorkWithDefaultSettings() {
        // Given
        String key = "test-key";
        LLMReply value = LLMReply.builder()
            .text("Cached response")
            .model("test-model")
            .build();
        
        // When
        cache.put(key, value);
        Optional<LLMReply> retrieved = cache.get(key);
        
        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getText()).isEqualTo("Cached response");
    }
    
    @Test
    @DisplayName("최대 크기 제한이 동작해야 함")
    void shouldRespectMaxSize() throws InterruptedException {
        // Given
        cache = new MemoryCache(2, 1, TimeUnit.HOURS); // 최대 2개 항목
        
        // When
        cache.put("key1", LLMReply.builder().text("Value 1").build());
        cache.put("key2", LLMReply.builder().text("Value 2").build());
        cache.put("key3", LLMReply.builder().text("Value 3").build()); // 이것이 key1을 제거해야 함
        
        // Caffeine은 비동기로 eviction을 수행하므로 약간의 시간이 필요
        Thread.sleep(100);
        
        // Then
        assertThat(cache.get("key1")).isEmpty(); // 제거됨
        assertThat(cache.get("key2")).isPresent();
        assertThat(cache.get("key3")).isPresent();
    }
    
    @Test
    @DisplayName("TTL이 만료되면 캐시가 제거되어야 함")
    void shouldExpireAfterTTL() throws InterruptedException {
        // Given
        cache = new MemoryCache(100, 100, TimeUnit.MILLISECONDS); // 100ms TTL
        String key = "test-key";
        LLMReply value = LLMReply.builder().text("Will expire").build();
        
        // When
        cache.put(key, value);
        assertThat(cache.get(key)).isPresent(); // 즉시 조회 시 존재
        
        Thread.sleep(150); // TTL 만료 대기
        
        // Then
        assertThat(cache.get(key)).isEmpty(); // 만료됨
    }
    
    @Test
    @DisplayName("캐시 통계를 제공해야 함")
    void shouldProvideStatistics() {
        // Given
        cache.put("key1", LLMReply.builder().text("Value 1").build());
        cache.put("key2", LLMReply.builder().text("Value 2").build());
        
        // When
        cache.get("key1"); // hit
        cache.get("key1"); // hit
        cache.get("key3"); // miss
        
        CacheStats stats = cache.getStats();
        
        // Then
        assertThat(stats.getHitCount()).isEqualTo(2);
        assertThat(stats.getMissCount()).isEqualTo(1);
        assertThat(stats.getHitRate()).isEqualTo(2.0 / 3.0);
        assertThat(stats.getSize()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("캐시를 비활성화할 수 있어야 함")
    void shouldDisableCache() {
        // Given
        cache = new MemoryCache(100, 1, TimeUnit.HOURS);
        cache.setEnabled(false);
        
        // When
        cache.put("key", LLMReply.builder().text("Should not be cached").build());
        Optional<LLMReply> retrieved = cache.get("key");
        
        // Then
        assertThat(retrieved).isEmpty(); // 캐시가 비활성화되어 저장되지 않음
        assertThat(cache.isEnabled()).isFalse();
    }
}