package kr.pyhub.llm.conversation;

import kr.pyhub.llm.types.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 대화 세션을 관리하는 클래스.
 * 
 * 메시지 히스토리를 자동으로 관리하고, 컨텍스트 제한을 처리합니다.
 */
@Slf4j
@Getter
public class Conversation {
    
    private final String id;
    private final List<Message> messages;
    private final int maxMessages;
    private final int maxTokens;
    private String systemPrompt;
    
    /**
     * 기본 설정으로 대화 생성
     */
    public Conversation() {
        this(null, 100, 4000);
    }
    
    /**
     * 시스템 프롬프트와 함께 대화 생성
     * 
     * @param systemPrompt 시스템 프롬프트
     */
    public Conversation(String systemPrompt) {
        this(systemPrompt, 100, 4000);
    }
    
    /**
     * 사용자 정의 설정으로 대화 생성
     * 
     * @param systemPrompt 시스템 프롬프트
     * @param maxMessages 최대 메시지 수
     * @param maxTokens 최대 토큰 수 (대략적)
     */
    public Conversation(String systemPrompt, int maxMessages, int maxTokens) {
        this.id = UUID.randomUUID().toString();
        this.messages = new ArrayList<>();
        this.maxMessages = maxMessages;
        this.maxTokens = maxTokens;
        this.systemPrompt = systemPrompt;
        
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            this.messages.add(Message.system(systemPrompt));
        }
        
        log.debug("새 대화 세션 생성: {} (최대 메시지: {}, 최대 토큰: {})", 
            id, maxMessages, maxTokens);
    }
    
    /**
     * 사용자 메시지 추가
     * 
     * @param content 사용자 메시지 내용
     * @return 이 대화 인스턴스 (메서드 체이닝)
     */
    public Conversation addUserMessage(String content) {
        addMessage(Message.user(content));
        return this;
    }
    
    /**
     * 어시스턴트 메시지 추가
     * 
     * @param content 어시스턴트 메시지 내용
     * @return 이 대화 인스턴스 (메서드 체이닝)
     */
    public Conversation addAssistantMessage(String content) {
        addMessage(Message.assistant(content));
        return this;
    }
    
    /**
     * 메시지 추가 (내부 메서드)
     * 
     * @param message 추가할 메시지
     */
    private void addMessage(Message message) {
        messages.add(message);
        
        // 메시지 수 제한 처리
        if (messages.size() > maxMessages) {
            trimMessages();
        }
        
        log.debug("메시지 추가: {} (총 메시지 수: {})", 
            message.getRole(), messages.size());
    }
    
    /**
     * 오래된 메시지 제거 (시스템 프롬프트는 유지)
     */
    private void trimMessages() {
        // 시스템 프롬프트가 있는지 확인
        boolean hasSystemPrompt = !messages.isEmpty() && 
            messages.get(0).getRole() == Message.Role.SYSTEM;
        
        int startIndex = hasSystemPrompt ? 1 : 0;
        int removeCount = messages.size() - maxMessages;
        
        if (removeCount > 0) {
            // 시스템 프롬프트 다음부터 오래된 메시지 제거
            for (int i = 0; i < removeCount; i++) {
                if (messages.size() > startIndex + 1) { // 최소 1개 메시지는 유지
                    messages.remove(startIndex);
                }
            }
            log.debug("오래된 메시지 {} 개 제거됨", removeCount);
        }
    }
    
    /**
     * 현재 대화의 모든 메시지 반환
     * 
     * @return 메시지 리스트 (읽기 전용)
     */
    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }
    
    /**
     * 메시지 개수 반환
     * 
     * @return 현재 메시지 개수
     */
    public int getMessageCount() {
        return messages.size();
    }
    
    /**
     * 대화 초기화 (시스템 프롬프트만 유지)
     * 
     * @return 이 대화 인스턴스 (메서드 체이닝)
     */
    public Conversation clear() {
        messages.clear();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            messages.add(Message.system(systemPrompt));
        }
        log.debug("대화 초기화됨: {}", id);
        return this;
    }
    
    /**
     * 시스템 프롬프트 변경
     * 
     * @param newSystemPrompt 새로운 시스템 프롬프트
     * @return 이 대화 인스턴스 (메서드 체이닝)
     */
    public Conversation setSystemPrompt(String newSystemPrompt) {
        // 기존 시스템 프롬프트 제거
        if (!messages.isEmpty() && messages.get(0).getRole() == Message.Role.SYSTEM) {
            messages.remove(0);
        }
        
        this.systemPrompt = newSystemPrompt;
        
        // 새로운 시스템 프롬프트 추가
        if (newSystemPrompt != null && !newSystemPrompt.trim().isEmpty()) {
            messages.add(0, Message.system(newSystemPrompt));
        }
        
        log.debug("시스템 프롬프트 변경됨: {}", id);
        return this;
    }
    
    /**
     * 대화가 비어있는지 확인 (시스템 프롬프트 제외)
     * 
     * @return 비어있으면 true
     */
    public boolean isEmpty() {
        boolean hasSystemPrompt = !messages.isEmpty() && 
            messages.get(0).getRole() == Message.Role.SYSTEM;
        return messages.size() <= (hasSystemPrompt ? 1 : 0);
    }
    
    /**
     * 대략적인 토큰 수 계산 (간단한 추정)
     * 
     * @return 추정 토큰 수
     */
    public int estimateTokenCount() {
        int totalChars = messages.stream()
            .mapToInt(msg -> msg.getContent().length())
            .sum();
        // 대략적으로 4글자당 1토큰으로 계산
        return totalChars / 4;
    }
}