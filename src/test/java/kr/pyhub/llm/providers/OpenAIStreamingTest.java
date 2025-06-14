package kr.pyhub.llm.providers;

import kr.pyhub.llm.Config;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import kr.pyhub.llm.types.StreamChunk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenAI 스트리밍 테스트
 * 
 * 실제 API 호출은 제외하고 기본 스트리밍 동작만 테스트
 */
class OpenAIStreamingTest {
    
    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_MODEL = "gpt-3.5-turbo";
    
    private Config config;
    
    @BeforeEach
    void setUp() {
        config = Config.builder()
            .apiKey(TEST_API_KEY)
            .temperature(0.7)
            .maxTokens(100)
            .build();
    }
    
    @Test
    @DisplayName("스트리밍 기본 폴백 동작 테스트")
    void shouldFallbackToDefaultStreaming() {
        // Given
        List<Message> messages = Arrays.asList(
            Message.user("Hello!")
        );
        
        // When
        // 실제 API 호출을 피하기 위해 TestOpenAILLM 사용
        TestOpenAILLM llm = new TestOpenAILLM(TEST_MODEL, config);
        Flux<StreamChunk> stream = llm.askStream(messages);
        
        // Then
        StepVerifier.create(stream)
            .expectNextMatches(chunk -> !chunk.isFinished())
            .expectNextCount(2) // "response" 단어 수에 따라
            .expectNextMatches(chunk -> chunk.isFinished())
            .verifyComplete();
    }
    
    @Test
    @DisplayName("스트리밍 응답을 문자열로 수집할 수 있어야 함")
    void shouldCollectStreamToString() {
        // Given
        List<Message> messages = Arrays.asList(
            Message.user("Tell me a joke")
        );
        
        // When
        TestOpenAILLM llm = new TestOpenAILLM(TEST_MODEL, config);
        String result = llm.askStream(messages)
            .filter(chunk -> !chunk.isFinished())
            .map(StreamChunk::getContent)
            .collectList()
            .map(chunks -> String.join("", chunks))
            .block(Duration.ofSeconds(5));
        
        // Then
        assertThat(result).isEqualTo("Test response");
    }
    
    /**
     * 테스트용 OpenAILLM 구현
     */
    static class TestOpenAILLM extends OpenAILLM {
        public TestOpenAILLM(String model, Config config) {
            super(model, config);
        }
        
        @Override
        protected LLMReply doAsk(List<Message> messages) {
            // 실제 API 호출 대신 테스트 응답 반환
            return LLMReply.builder()
                .text("Test response")
                .model(getModel())
                .finishReason("stop")
                .build();
        }
    }
}