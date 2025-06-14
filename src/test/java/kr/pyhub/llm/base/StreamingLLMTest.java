package kr.pyhub.llm.base;

import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import kr.pyhub.llm.types.StreamChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스트리밍 LLM 테스트
 */
class StreamingLLMTest {
    
    @Test
    @DisplayName("스트리밍 응답을 Flux로 반환해야 함")
    void shouldReturnStreamingResponse() {
        // Given
        TestStreamingLLM llm = new TestStreamingLLM();
        List<Message> messages = Arrays.asList(
            Message.user("Hello, stream!")
        );
        
        // When
        Flux<StreamChunk> stream = llm.askStream(messages);
        
        // Then
        StepVerifier.create(stream)
            .expectNextMatches(chunk -> chunk.getContent().equals("Hello"))
            .expectNextMatches(chunk -> chunk.getContent().equals(" "))
            .expectNextMatches(chunk -> chunk.getContent().equals("from"))
            .expectNextMatches(chunk -> chunk.getContent().equals(" "))
            .expectNextMatches(chunk -> chunk.getContent().equals("stream!"))
            .expectNextMatches(chunk -> chunk.isFinished())
            .verifyComplete();
    }
    
    @Test
    @DisplayName("스트리밍 중 에러 처리")
    void shouldHandleStreamingError() {
        // Given
        TestStreamingLLM llm = new TestStreamingLLM();
        llm.setShouldError(true);
        List<Message> messages = Arrays.asList(
            Message.user("Cause error")
        );
        
        // When
        Flux<StreamChunk> stream = llm.askStream(messages);
        
        // Then
        StepVerifier.create(stream)
            .expectNextMatches(chunk -> chunk.getContent().equals("Starting"))
            .expectErrorMessage("Simulated streaming error")
            .verify();
    }
    
    @Test
    @DisplayName("스트리밍을 문자열로 수집할 수 있어야 함")
    void shouldCollectStreamToString() {
        // Given
        TestStreamingLLM llm = new TestStreamingLLM();
        List<Message> messages = Arrays.asList(
            Message.user("Hello, stream!")
        );
        
        // When
        String result = llm.askStream(messages)
            .filter(chunk -> !chunk.isFinished())
            .map(StreamChunk::getContent)
            .collectList()
            .map(chunks -> String.join("", chunks))
            .block(Duration.ofSeconds(5));
        
        // Then
        assertThat(result).isEqualTo("Hello from stream!");
    }
    
    /**
     * 테스트용 스트리밍 LLM 구현
     */
    static class TestStreamingLLM extends BaseLLM {
        private boolean shouldError = false;
        
        public TestStreamingLLM() {
            super("test-streaming-model");
        }
        
        public void setShouldError(boolean shouldError) {
            this.shouldError = shouldError;
        }
        
        @Override
        protected LLMReply doAsk(List<Message> messages) {
            return LLMReply.builder()
                .text("Hello from stream!")
                .model(getModel())
                .build();
        }
        
        @Override
        public Flux<StreamChunk> askStream(List<Message> messages) {
            if (shouldError) {
                return Flux.just("Starting")
                    .map(content -> StreamChunk.builder()
                        .content(content)
                        .finished(false)
                        .build())
                    .concatWith(Flux.error(new RuntimeException("Simulated streaming error")));
            }
            
            return Flux.just("Hello", " ", "from", " ", "stream!")
                .map(content -> StreamChunk.builder()
                    .content(content)
                    .finished(false)
                    .build())
                .concatWith(Flux.just(StreamChunk.builder()
                    .content("")
                    .finished(true)
                    .finishReason("stop")
                    .build()));
        }
    }
}