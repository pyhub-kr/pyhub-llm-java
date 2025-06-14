package kr.pyhub.llm.conversation;

import kr.pyhub.llm.types.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 대화 관리 테스트
 */
class ConversationTest {
    
    private Conversation conversation;
    
    @BeforeEach
    void setUp() {
        conversation = new Conversation("You are a helpful assistant");
    }
    
    @Test
    @DisplayName("시스템 프롬프트가 첫 번째 메시지로 추가되어야 함")
    void shouldAddSystemPromptAsFirstMessage() {
        // Then
        assertThat(conversation.getMessages()).hasSize(1);
        assertThat(conversation.getMessages().get(0).getRole()).isEqualTo(Message.Role.SYSTEM);
        assertThat(conversation.getMessages().get(0).getContent()).isEqualTo("You are a helpful assistant");
    }
    
    @Test
    @DisplayName("사용자 메시지를 추가할 수 있어야 함")
    void shouldAddUserMessage() {
        // When
        conversation.addUserMessage("Hello!");
        
        // Then
        assertThat(conversation.getMessages()).hasSize(2);
        assertThat(conversation.getMessages().get(1).getRole()).isEqualTo(Message.Role.USER);
        assertThat(conversation.getMessages().get(1).getContent()).isEqualTo("Hello!");
    }
    
    @Test
    @DisplayName("어시스턴트 메시지를 추가할 수 있어야 함")
    void shouldAddAssistantMessage() {
        // When
        conversation.addUserMessage("Hello!")
                   .addAssistantMessage("Hi there!");
        
        // Then
        assertThat(conversation.getMessages()).hasSize(3);
        assertThat(conversation.getMessages().get(2).getRole()).isEqualTo(Message.Role.ASSISTANT);
        assertThat(conversation.getMessages().get(2).getContent()).isEqualTo("Hi there!");
    }
    
    @Test
    @DisplayName("메시지 수 제한이 동작해야 함")
    void shouldLimitMessageCount() {
        // Given
        Conversation limitedConversation = new Conversation("System", 5, 1000);
        
        // When - 6개 메시지 추가 (시스템 포함하면 7개)
        for (int i = 1; i <= 6; i++) {
            limitedConversation.addUserMessage("Message " + i);
        }
        
        // Then
        assertThat(limitedConversation.getMessages()).hasSize(5); // 최대 5개로 제한
        // 시스템 프롬프트는 유지되어야 함
        assertThat(limitedConversation.getMessages().get(0).getRole()).isEqualTo(Message.Role.SYSTEM);
        // 마지막 메시지들만 유지되어야 함
        assertThat(limitedConversation.getMessages().get(4).getContent()).isEqualTo("Message 6");
    }
    
    @Test
    @DisplayName("대화를 초기화할 수 있어야 함")
    void shouldClearConversation() {
        // Given
        conversation.addUserMessage("Hello!")
                   .addAssistantMessage("Hi!");
        
        // When
        conversation.clear();
        
        // Then
        assertThat(conversation.getMessages()).hasSize(1); // 시스템 프롬프트만 남음
        assertThat(conversation.getMessages().get(0).getRole()).isEqualTo(Message.Role.SYSTEM);
    }
    
    @Test
    @DisplayName("시스템 프롬프트를 변경할 수 있어야 함")
    void shouldChangeSystemPrompt() {
        // Given
        conversation.addUserMessage("Hello!");
        
        // When
        conversation.setSystemPrompt("You are a translator");
        
        // Then
        assertThat(conversation.getMessages()).hasSize(2); // 새 시스템 프롬프트 + 사용자 메시지
        assertThat(conversation.getMessages().get(0).getRole()).isEqualTo(Message.Role.SYSTEM);
        assertThat(conversation.getMessages().get(0).getContent()).isEqualTo("You are a translator");
        assertThat(conversation.getMessages().get(1).getContent()).isEqualTo("Hello!");
    }
    
    @Test
    @DisplayName("빈 대화 상태를 확인할 수 있어야 함")
    void shouldCheckIfEmpty() {
        // Given
        Conversation emptyConversation = new Conversation();
        
        // Then
        assertThat(emptyConversation.isEmpty()).isTrue();
        
        // When
        emptyConversation.addUserMessage("Hello!");
        
        // Then
        assertThat(emptyConversation.isEmpty()).isFalse();
    }
    
    @Test
    @DisplayName("토큰 수를 추정할 수 있어야 함")
    void shouldEstimateTokenCount() {
        // When
        conversation.addUserMessage("Hello there!")
                   .addAssistantMessage("Hi! How can I help you?");
        
        // Then
        int tokenCount = conversation.estimateTokenCount();
        assertThat(tokenCount).isGreaterThan(0);
    }
}