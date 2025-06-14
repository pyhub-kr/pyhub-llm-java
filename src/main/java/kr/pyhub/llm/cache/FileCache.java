package kr.pyhub.llm.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import kr.pyhub.llm.types.LLMReply;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 파일 시스템 기반 캐시 구현.
 * 
 * 특징:
 * - JSON 형식으로 디스크에 저장
 * - 프로세스 재시작 후에도 캐시 유지
 * - 대용량 응답 저장 가능
 * - 네트워크 파일 시스템 지원
 */
@Slf4j
public class FileCache extends AbstractCache {
    
    private static final String DEFAULT_CACHE_DIR = ".pyhub-llm-cache";
    private static final String CACHE_FILE_EXTENSION = ".json";
    
    private final Path cacheDir;
    private final ObjectMapper objectMapper;
    private boolean enabled = true;
    
    /**
     * 기본 캐시 디렉토리로 파일 캐시 생성
     */
    public FileCache() {
        this(DEFAULT_CACHE_DIR);
    }
    
    /**
     * 지정된 디렉토리로 파일 캐시 생성
     * 
     * @param cacheDirectory 캐시 디렉토리 경로
     */
    public FileCache(String cacheDirectory) {
        this.cacheDir = Paths.get(cacheDirectory);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // 캐시 디렉토리 생성
        try {
            Files.createDirectories(cacheDir);
            log.info("FileCache initialized at: {}", cacheDir.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to create cache directory: {}", cacheDir, e);
            throw new RuntimeException("Failed to initialize FileCache", e);
        }
    }
    
    @Override
    public Optional<LLMReply> get(String key) {
        if (!enabled) {
            return Optional.empty();
        }
        
        Path cacheFile = getCacheFilePath(key);
        if (!Files.exists(cacheFile)) {
            log.debug("Cache miss for key: {}", key);
            return Optional.empty();
        }
        
        try {
            LLMReply reply = objectMapper.readValue(cacheFile.toFile(), LLMReply.class);
            log.debug("Cache hit for key: {}", key);
            return Optional.of(reply);
        } catch (IOException e) {
            log.warn("Failed to read cache file: {}", cacheFile, e);
            // 손상된 캐시 파일 삭제
            try {
                Files.deleteIfExists(cacheFile);
            } catch (IOException deleteError) {
                log.error("Failed to delete corrupted cache file: {}", cacheFile, deleteError);
            }
            return Optional.empty();
        }
    }
    
    @Override
    public void put(String key, LLMReply value) {
        if (!enabled) {
            return;
        }
        
        Path cacheFile = getCacheFilePath(key);
        try {
            objectMapper.writeValue(cacheFile.toFile(), value);
            log.debug("Cached response to file: {}", cacheFile);
        } catch (IOException e) {
            log.error("Failed to write cache file: {}", cacheFile, e);
        }
    }
    
    @Override
    public void evict(String key) {
        Path cacheFile = getCacheFilePath(key);
        try {
            if (Files.deleteIfExists(cacheFile)) {
                log.debug("Evicted cache file: {}", cacheFile);
            }
        } catch (IOException e) {
            log.error("Failed to delete cache file: {}", cacheFile, e);
        }
    }
    
    @Override
    public void clear() {
        try (Stream<Path> stream = Files.list(cacheDir)) {
            stream.filter(path -> path.toString().endsWith(CACHE_FILE_EXTENSION))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        log.error("Failed to delete cache file: {}", path, e);
                    }
                });
            log.info("Cleared all cache files in: {}", cacheDir);
        } catch (IOException e) {
            log.error("Failed to clear cache directory: {}", cacheDir, e);
        }
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 캐시 활성화/비활성화
     * 
     * @param enabled 활성화 여부
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("FileCache enabled: {}", enabled);
    }
    
    /**
     * 캐시 파일 경로 생성
     * 
     * @param key 캐시 키
     * @return 캐시 파일 경로
     */
    private Path getCacheFilePath(String key) {
        return cacheDir.resolve(key + CACHE_FILE_EXTENSION);
    }
    
    /**
     * 캐시 디렉토리의 전체 크기 (바이트)
     * 
     * @return 캐시 크기
     */
    public long getCacheSize() {
        try (Stream<Path> stream = Files.list(cacheDir)) {
            return stream.filter(path -> path.toString().endsWith(CACHE_FILE_EXTENSION))
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
        } catch (IOException e) {
            log.error("Failed to calculate cache size", e);
            return 0;
        }
    }
    
    /**
     * 캐시 파일 개수
     * 
     * @return 파일 개수
     */
    public long getCacheFileCount() {
        try (Stream<Path> stream = Files.list(cacheDir)) {
            return stream.filter(path -> path.toString().endsWith(CACHE_FILE_EXTENSION))
                .count();
        } catch (IOException e) {
            log.error("Failed to count cache files", e);
            return 0;
        }
    }
}