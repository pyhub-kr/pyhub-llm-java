package kr.pyhub.llm.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import kr.pyhub.llm.types.LLMReply;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Caffeine을 사용한 메모리 기반 캐시 구현.
 * 
 * 특징:
 * - 크기 기반 제거 (LRU)
 * - 시간 기반 만료 (TTL)
 * - 통계 정보 제공
 * - 스레드 안전
 */
@Slf4j
public class MemoryCache extends AbstractCache {
    
    private static final int DEFAULT_MAX_SIZE = 1000;
    private static final long DEFAULT_TTL_MINUTES = 60;
    
    private final Cache<String, LLMReply> cache;
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    
    /**
     * 기본 설정으로 메모리 캐시 생성
     */
    public MemoryCache() {
        this(DEFAULT_MAX_SIZE, DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
    }
    
    /**
     * 커스텀 설정으로 메모리 캐시 생성
     * 
     * @param maxSize 최대 항목 수
     * @param ttl TTL 값
     * @param ttlUnit TTL 시간 단위
     */
    public MemoryCache(long maxSize, long ttl, TimeUnit ttlUnit) {
        this.cache = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(ttl, ttlUnit)
            .recordStats()
            .build();
            
        log.info("MemoryCache initialized with maxSize={}, ttl={} {}", 
            maxSize, ttl, ttlUnit);
    }
    
    @Override
    public Optional<LLMReply> get(String key) {
        if (!enabled.get()) {
            return Optional.empty();
        }
        
        LLMReply value = cache.getIfPresent(key);
        if (value != null) {
            log.debug("Cache hit for key: {}", key);
        } else {
            log.debug("Cache miss for key: {}", key);
        }
        
        return Optional.ofNullable(value);
    }
    
    @Override
    public void put(String key, LLMReply value) {
        if (!enabled.get()) {
            return;
        }
        
        cache.put(key, value);
        log.debug("Cached response for key: {}", key);
    }
    
    @Override
    public void evict(String key) {
        cache.invalidate(key);
        log.debug("Evicted cache for key: {}", key);
    }
    
    @Override
    public void clear() {
        cache.invalidateAll();
        log.info("Cleared all cache entries");
    }
    
    @Override
    public boolean isEnabled() {
        return enabled.get();
    }
    
    /**
     * 캐시 활성화/비활성화
     * 
     * @param enabled 활성화 여부
     */
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        if (!enabled) {
            clear();
        }
        log.info("Cache enabled: {}", enabled);
    }
    
    /**
     * 캐시 통계 정보 반환
     * 
     * @return 캐시 통계
     */
    public kr.pyhub.llm.cache.CacheStats getStats() {
        CacheStats stats = cache.stats();
        return kr.pyhub.llm.cache.CacheStats.builder()
            .hitCount(stats.hitCount())
            .missCount(stats.missCount())
            .size(cache.estimatedSize())
            .build();
    }
    
    /**
     * 캐시 크기 반환
     * 
     * @return 현재 캐시 크기
     */
    public long size() {
        return cache.estimatedSize();
    }
}