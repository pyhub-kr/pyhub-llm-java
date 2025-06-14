package kr.pyhub.llm.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 캐시 통계 정보.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheStats {
    
    /**
     * 캐시 히트 횟수
     */
    private long hitCount;
    
    /**
     * 캐시 미스 횟수
     */
    private long missCount;
    
    /**
     * 현재 캐시 크기
     */
    private long size;
    
    /**
     * 캐시 히트율 계산
     * 
     * @return 히트율 (0.0 ~ 1.0)
     */
    public double getHitRate() {
        long total = hitCount + missCount;
        return total == 0 ? 0.0 : (double) hitCount / total;
    }
    
    /**
     * 통계 초기화
     */
    public void reset() {
        this.hitCount = 0;
        this.missCount = 0;
    }
}