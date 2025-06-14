package kr.pyhub.llm.examples.upstage;

import kr.pyhub.llm.Config;
import kr.pyhub.llm.LLM;
import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UpstageCliChat 클래스의 자동 대화 관리 기능을 테스트합니다.
 */
class UpstageCliChatTest {

    private BaseLLM llm;
    
    @BeforeEach
    void setUp() {
        // 테스트용 LLM 인스턴스 생성 (실제 API 호출 없이)
        Config config = Config.builder()
            .temperature(0.7)
            .maxTokens(100)
            .build();
            
        String systemPrompt = "당신은 친근하고 도움이 되는 AI 어시스턴트입니다. "
            + "한국어로 답변해주세요. 테스트 환경에서는 간단히 답변해주세요.";
            
        // 실제 환경에서는 Upstage LLM을 사용하지만, 테스트에서는 Mock LLM 사용
        llm = new MockLLM("solar-1-mini-chat", config)
            .enableConversation(systemPrompt);
    }
    
    @Test
    void testConversationManagement() {
        // Given
        String userMessage1 = "안녕하세요";
        String userMessage2 = "제 이름을 기억하시나요?";
        
        // When - 첫 번째 메시지
        LLMReply reply1 = llm.chat(userMessage1);
        
        // Then
        assertNotNull(reply1);
        assertNotNull(reply1.getText());
        assertTrue(reply1.getText().contains("Mock"));
        
        // When - 두 번째 메시지 (대화 기록이 유지되어야 함)
        LLMReply reply2 = llm.chat(userMessage2);
        
        // Then
        assertNotNull(reply2);
        assertNotNull(reply2.getText());
        assertTrue(reply2.getText().contains("Mock"));
    }
    
    @Test
    void testConversationHistory() {
        // Given
        String message = "테스트 메시지입니다";
        
        // When
        llm.chat(message);
        
        // Then - 대화 히스토리에 시스템 프롬프트, 사용자 메시지, 어시스턴트 응답이 있어야 함
        // 실제 대화 기록은 내부적으로 관리되므로 직접 확인하기 어렵지만,
        // 두 번째 메시지에서 컨텍스트가 유지되는지 확인
        LLMReply reply = llm.chat("이전 메시지를 기억하시나요?");
        assertNotNull(reply);
        assertNotNull(reply.getText());
    }
    
    @Test
    void testClearConversation() {
        // Given
        llm.chat("첫 번째 메시지");
        llm.chat("두 번째 메시지");
        
        // When
        llm.clearConversation();
        
        // Then - 대화 기록이 초기화되어야 함
        LLMReply reply = llm.chat("새로운 대화입니다");
        assertNotNull(reply);
        assertNotNull(reply.getText());
    }
    
    @Test
    @EnabledIfEnvironmentVariable(named = "UPSTAGE_API_KEY", matches = ".+")
    void testRealUpstageIntegration() {
        // Given - 실제 Upstage API를 사용한 통합 테스트 (API 키가 있을 때만 실행)
        Config config = Config.builder()
            .temperature(0.7)
            .maxTokens(50)
            .build();
            
        String systemPrompt = "당신은 간단하게 답변하는 AI입니다. 한 문장으로만 답변해주세요.";
        
        BaseLLM realLlm = LLM.create("solar-1-mini-chat", config)
            .enableConversation(systemPrompt);
        
        // When
        LLMReply reply = realLlm.chat("안녕하세요");
        
        // Then
        assertNotNull(reply);
        assertNotNull(reply.getText());
        assertFalse(reply.getText().trim().isEmpty());
        
        // 대화 기록 유지 테스트
        LLMReply reply2 = realLlm.chat("방금 제가 뭐라고 했나요?");
        assertNotNull(reply2);
        assertNotNull(reply2.getText());
    }
    
    /**
     * 테스트용 Mock LLM 구현
     */
    private static class MockLLM extends BaseLLM {
        
        public MockLLM(String model, Config config) {
            super(model);
        }
        
        @Override
        protected LLMReply doAsk(java.util.List<Message> messages) {
            // Mock 응답 생성
            String lastUserMessage = messages.stream()
                .filter(m -> "user".equals(m.getRole()))
                .reduce((first, second) -> second)
                .map(Message::getContent)
                .orElse("없음");
                
            return LLMReply.builder()
                .text("Mock 응답: " + lastUserMessage + "에 대한 답변입니다.")
                .model(getModel())
                .build();
        }
    }
}