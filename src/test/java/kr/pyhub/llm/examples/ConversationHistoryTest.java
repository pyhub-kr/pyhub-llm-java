package kr.pyhub.llm.examples;

import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 대화 히스토리 기능 테스트
 * CLI 채팅 애플리케이션에서 대화 컨텍스트를 유지하는지 검증
 */
class ConversationHistoryTest {
    
    private MockLLM mockLLM;
    private List<Message> conversationHistory;
    
    @BeforeEach
    void setUp() {
        mockLLM = new MockLLM();
        conversationHistory = new ArrayList<>();
    }
    
    @Test
    @DisplayName("대화 히스토리에 시스템 프롬프트가 추가되어야 함")
    void shouldAddSystemPromptToHistory() {
        // Given
        String systemPrompt = "You are a helpful assistant.";
        
        // When
        conversationHistory.add(Message.system(systemPrompt));
        
        // Then
        assertThat(conversationHistory).hasSize(1);
        assertThat(conversationHistory.get(0).getRole()).isEqualTo(Message.Role.SYSTEM);
        assertThat(conversationHistory.get(0).getContent()).isEqualTo(systemPrompt);
    }
    
    @Test
    @DisplayName("사용자 메시지와 AI 응답이 순서대로 히스토리에 추가되어야 함")
    void shouldMaintainConversationOrder() {
        // Given
        conversationHistory.add(Message.system("You are a helpful assistant."));
        
        // When - 첫 번째 대화
        String userInput1 = "Hello!";
        conversationHistory.add(Message.user(userInput1));
        
        LLMReply reply1 = mockLLM.ask(conversationHistory);
        conversationHistory.add(Message.assistant(reply1.getText()));
        
        // When - 두 번째 대화
        String userInput2 = "How are you?";
        conversationHistory.add(Message.user(userInput2));
        
        LLMReply reply2 = mockLLM.ask(conversationHistory);
        conversationHistory.add(Message.assistant(reply2.getText()));
        
        // Then
        assertThat(conversationHistory).hasSize(5);
        assertThat(conversationHistory.get(0).getRole()).isEqualTo(Message.Role.SYSTEM);
        assertThat(conversationHistory.get(1).getRole()).isEqualTo(Message.Role.USER);
        assertThat(conversationHistory.get(1).getContent()).isEqualTo(userInput1);
        assertThat(conversationHistory.get(2).getRole()).isEqualTo(Message.Role.ASSISTANT);
        assertThat(conversationHistory.get(3).getRole()).isEqualTo(Message.Role.USER);
        assertThat(conversationHistory.get(3).getContent()).isEqualTo(userInput2);
        assertThat(conversationHistory.get(4).getRole()).isEqualTo(Message.Role.ASSISTANT);
    }
    
    @Test
    @DisplayName("LLM이 전체 대화 히스토리를 받아야 함")
    void shouldPassFullHistoryToLLM() {
        // Given
        conversationHistory.add(Message.system("You are a helpful assistant."));
        conversationHistory.add(Message.user("My name is John."));
        conversationHistory.add(Message.assistant("Nice to meet you, John!"));
        conversationHistory.add(Message.user("What's my name?"));
        
        // When
        mockLLM.ask(conversationHistory);
        
        // Then
        List<Message> receivedMessages = mockLLM.getLastReceivedMessages();
        assertThat(receivedMessages).hasSize(4);
        assertThat(receivedMessages).isEqualTo(conversationHistory);
    }
    
    /**
     * 테스트용 Mock LLM 구현
     */
    private static class MockLLM extends BaseLLM {
        private List<Message> lastReceivedMessages;
        
        public MockLLM() {
            super("mock-model");
        }
        
        @Override
        protected LLMReply doAsk(List<Message> messages) {
            this.lastReceivedMessages = new ArrayList<>(messages);
            
            // 간단한 응답 생성
            String response = "Mock response to: " + 
                messages.get(messages.size() - 1).getContent();
            
            return LLMReply.builder()
                .text(response)
                .model("mock-model")
                .finishReason("stop")
                .build();
        }
        
        public List<Message> getLastReceivedMessages() {
            return lastReceivedMessages;
        }
    }
}