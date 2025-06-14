package kr.pyhub.llm.base;

import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import kr.pyhub.llm.exceptions.LLMException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BaseLLMTest {
    
    private TestLLM llm;
    
    @BeforeEach
    void setUp() {
        llm = new TestLLM();
    }
    
    @Test
    @DisplayName("ask 메서드는 문자열 입력을 받아 LLMReply를 반환해야 한다")
    void testAskWithStringInput() {
        // Given
        String prompt = "Hello, how are you?";
        String expectedResponse = "I'm doing well, thank you!";
        llm.setMockResponse(expectedResponse);
        
        // When
        LLMReply reply = llm.ask(prompt);
        
        // Then
        assertThat(reply).isNotNull();
        assertThat(reply.getText()).isEqualTo(expectedResponse);
        assertThat(reply.getModel()).isEqualTo("test-model");
    }
    
    @Test
    @DisplayName("ask 메서드는 Message 리스트를 받아 LLMReply를 반환해야 한다")
    void testAskWithMessageList() {
        // Given
        List<Message> messages = Arrays.asList(
            Message.system("You are a helpful assistant"),
            Message.user("What is 2+2?")
        );
        String expectedResponse = "2+2 equals 4";
        llm.setMockResponse(expectedResponse);
        
        // When
        LLMReply reply = llm.ask(messages);
        
        // Then
        assertThat(reply).isNotNull();
        assertThat(reply.getText()).isEqualTo(expectedResponse);
    }
    
    @Test
    @DisplayName("askAsync 메서드는 비동기로 LLMReply를 반환해야 한다")
    void testAskAsync() throws ExecutionException, InterruptedException {
        // Given
        String prompt = "Tell me a joke";
        String expectedResponse = "Why did the chicken cross the road?";
        llm.setMockResponse(expectedResponse);
        
        // When
        CompletableFuture<LLMReply> future = llm.askAsync(prompt);
        LLMReply reply = future.get();
        
        // Then
        assertThat(reply).isNotNull();
        assertThat(reply.getText()).isEqualTo(expectedResponse);
    }
    
    @Test
    @DisplayName("withSystemPrompt 메서드는 시스템 프롬프트를 설정할 수 있어야 한다")
    void testWithSystemPrompt() {
        // Given
        String systemPrompt = "You are a helpful coding assistant";
        
        // When
        BaseLLM result = llm.withSystemPrompt(systemPrompt);
        
        // Then
        assertThat(result).isSameAs(llm);
        assertThat(llm.getSystemPrompt()).isEqualTo(systemPrompt);
    }
    
    @Test
    @DisplayName("withTemperature 메서드는 temperature를 설정할 수 있어야 한다")
    void testWithTemperature() {
        // Given
        double temperature = 0.7;
        
        // When
        BaseLLM result = llm.withTemperature(temperature);
        
        // Then
        assertThat(result).isSameAs(llm);
        assertThat(llm.getTemperature()).isEqualTo(temperature);
    }
    
    @Test
    @DisplayName("withMaxTokens 메서드는 maxTokens를 설정할 수 있어야 한다")
    void testWithMaxTokens() {
        // Given
        int maxTokens = 1000;
        
        // When
        BaseLLM result = llm.withMaxTokens(maxTokens);
        
        // Then
        assertThat(result).isSameAs(llm);
        assertThat(llm.getMaxTokens()).isEqualTo(maxTokens);
    }
    
    @Test
    @DisplayName("잘못된 temperature 값은 예외를 발생시켜야 한다")
    void testInvalidTemperature() {
        assertThatThrownBy(() -> llm.withTemperature(-0.1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Temperature must be between 0 and 2");
            
        assertThatThrownBy(() -> llm.withTemperature(2.1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Temperature must be between 0 and 2");
    }
    
    @Test
    @DisplayName("잘못된 maxTokens 값은 예외를 발생시켜야 한다")
    void testInvalidMaxTokens() {
        assertThatThrownBy(() -> llm.withMaxTokens(0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Max tokens must be positive");
            
        assertThatThrownBy(() -> llm.withMaxTokens(-100))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Max tokens must be positive");
    }
    
    @Test
    @DisplayName("getModel 메서드는 모델 이름을 반환해야 한다")
    void testGetModel() {
        assertThat(llm.getModel()).isEqualTo("test-model");
    }
    
    // Test implementation of BaseLLM for testing purposes
    private static class TestLLM extends BaseLLM {
        private String mockResponse = "Default response";
        
        public TestLLM() {
            super("test-model");
        }
        
        public void setMockResponse(String response) {
            this.mockResponse = response;
        }
        
        @Override
        protected LLMReply doAsk(List<Message> messages) {
            return LLMReply.builder()
                .text(mockResponse)
                .model(getModel())
                .usage(LLMReply.Usage.builder()
                    .promptTokens(10)
                    .completionTokens(20)
                    .totalTokens(30)
                    .build())
                .build();
        }
    }
}