package kr.pyhub.llm.tools;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 도구 구현을 위한 추상 클래스.
 * 
 * 공통 기능을 제공합니다.
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractTool implements Tool {
    
    private final String name;
    private final String description;
    private boolean enabled = true;
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 도구 활성화/비활성화
     * 
     * @param enabled 활성화 여부
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        log.info("Tool '{}' enabled: {}", name, enabled);
    }
    
    /**
     * 도구 실행 전 로깅
     * 
     * @param args 인자
     */
    protected void logExecution(Object args) {
        if (log.isDebugEnabled()) {
            log.debug("Executing tool '{}' with args: {}", name, args);
        }
    }
    
    /**
     * 도구 실행 결과 로깅
     * 
     * @param result 실행 결과
     */
    protected void logResult(ToolResult result) {
        if (result.isSuccess()) {
            log.debug("Tool '{}' executed successfully", name);
        } else {
            log.warn("Tool '{}' failed: {}", name, result.getError());
        }
    }
}