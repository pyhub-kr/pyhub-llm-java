package kr.pyhub.llm.cache;

import kr.pyhub.llm.types.LLMReply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 파일 기반 캐시 테스트
 */
class FileCacheTest {
    
    @TempDir
    Path tempDir;
    
    private FileCache cache;
    
    @BeforeEach
    void setUp() {
        cache = new FileCache(tempDir.toString());
    }
    
    @Test
    @DisplayName("파일에 캐시를 저장하고 읽을 수 있어야 함")
    void shouldStoreAndReadFromFile() {
        // Given
        String key = "test-key";
        LLMReply value = LLMReply.builder()
            .text("File cached response")
            .model("test-model")
            .finishReason("stop")
            .usage(LLMReply.Usage.builder()
                .promptTokens(10)
                .completionTokens(20)
                .totalTokens(30)
                .build())
            .build();
        
        // When
        cache.put(key, value);
        Optional<LLMReply> retrieved = cache.get(key);
        
        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getText()).isEqualTo("File cached response");
        assertThat(retrieved.get().getUsage().getTotalTokens()).isEqualTo(30);
    }
    
    @Test
    @DisplayName("캐시 파일을 삭제할 수 있어야 함")
    void shouldDeleteCacheFile() {
        // Given
        String key = "test-key";
        cache.put(key, LLMReply.builder().text("To be deleted").build());
        
        // When
        cache.evict(key);
        Optional<LLMReply> retrieved = cache.get(key);
        
        // Then
        assertThat(retrieved).isEmpty();
    }
    
    @Test
    @DisplayName("모든 캐시 파일을 삭제할 수 있어야 함")
    void shouldClearAllCacheFiles() {
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
    
    @Test
    @DisplayName("손상된 캐시 파일은 무시해야 함")
    void shouldIgnoreCorruptedCacheFiles() {
        // Given
        String key = "corrupted-key";
        // 직접 손상된 파일 생성
        Path corruptedFile = tempDir.resolve(key + ".json");
        try {
            java.nio.file.Files.write(corruptedFile, "{ invalid json".getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // When
        Optional<LLMReply> retrieved = cache.get(key);
        
        // Then
        assertThat(retrieved).isEmpty();
    }
    
    @Test
    @DisplayName("캐시 디렉토리가 없으면 자동으로 생성해야 함")
    void shouldCreateCacheDirectoryIfNotExists() {
        // Given
        Path nonExistentDir = tempDir.resolve("non-existent-cache");
        FileCache newCache = new FileCache(nonExistentDir.toString());
        
        // When
        newCache.put("key", LLMReply.builder().text("Test").build());
        
        // Then
        assertThat(nonExistentDir).exists();
        assertThat(newCache.get("key")).isPresent();
    }
}