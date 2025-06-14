package kr.pyhub.llm.integration;

import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.cache.MemoryCache;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 캐싱이 통합된 LLM 테스트
 */
class CachedLLMTest {
    
    private TestLLM llm;
    private MemoryCache cache;
    
    @BeforeEach
    void setUp() {
        cache = new MemoryCache();
        llm = new TestLLM();
        llm.withCache(cache);
    }
    
    @Test
    @DisplayName("동일한 요청은 캐시에서 응답해야 함")
    void shouldReturnCachedResponse() {
        // Given
        List<Message> messages = Arrays.asList(
            Message.user("What is 2+2?")
        );
        
        // When
        LLMReply reply1 = llm.ask(messages);
        LLMReply reply2 = llm.ask(messages);
        
        // Then
        assertThat(reply1.getText()).isEqualTo(reply2.getText());
        assertThat(llm.getCallCount()).isEqualTo(1); // API는 한 번만 호출됨
        assertThat(cache.getStats().getHitCount()).isEqualTo(1);
        assertThat(cache.getStats().getMissCount()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("다른 파라미터는 다른 캐시 키를 생성해야 함")
    void shouldUseDifferentCacheKeys() {
        // Given
        List<Message> messages = Arrays.asList(
            Message.user("Hello!")
        );
        
        // When
        llm.withTemperature(0.7);
        LLMReply reply1 = llm.ask(messages);
        
        llm.withTemperature(0.9);
        LLMReply reply2 = llm.ask(messages);
        
        // Then
        assertThat(llm.getCallCount()).isEqualTo(2); // 두 번 호출됨
        assertThat(cache.getStats().getHitCount()).isEqualTo(0);
        assertThat(cache.getStats().getMissCount()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("캐시를 비활성화하면 항상 API를 호출해야 함")
    void shouldBypassCacheWhenDisabled() {
        // Given
        cache.setEnabled(false);
        List<Message> messages = Arrays.asList(
            Message.user("Test message")
        );
        
        // When
        llm.ask(messages);
        llm.ask(messages);
        llm.ask(messages);
        
        // Then
        assertThat(llm.getCallCount()).isEqualTo(3); // 모든 호출이 API로 감
    }
    
    @Test
    @DisplayName("시스템 프롬프트가 포함된 캐싱")
    void shouldCacheWithSystemPrompt() {
        // Given
        llm.withSystemPrompt("You are a helpful assistant");
        
        // When
        LLMReply reply1 = llm.ask("Hello!");
        LLMReply reply2 = llm.ask("Hello!");
        
        // Then
        assertThat(reply1.getText()).isEqualTo(reply2.getText());
        assertThat(llm.getCallCount()).isEqualTo(1);
    }
    
    /**
     * 테스트용 LLM 구현
     */
    static class TestLLM extends BaseLLM {
        private final AtomicInteger callCount = new AtomicInteger(0);
        
        public TestLLM() {
            super("test-model");
        }
        
        @Override
        protected LLMReply doAsk(List<Message> messages) {
            callCount.incrementAndGet();
            return LLMReply.builder()
                .text("Response " + callCount.get())
                .model(getModel())
                .finishReason("stop")
                .build();
        }
        
        public int getCallCount() {
            return callCount.get();
        }
    }
}