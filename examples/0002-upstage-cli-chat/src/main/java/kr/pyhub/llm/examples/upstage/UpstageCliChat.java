package kr.pyhub.llm.examples.upstage;

import kr.pyhub.llm.Config;
import kr.pyhub.llm.LLM;
import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.types.LLMReply;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Upstage Solar LLM을 사용한 대화형 CLI 챗봇.
 * 
 * 한국어에 최적화된 Solar 모델을 사용하여 자연스러운 한국어 대화를 제공합니다.
 * 이 예시는 pyhub-llm-java의 자동 대화 관리 기능을 사용합니다.
 * 
 * 사용법:
 * - UPSTAGE_API_KEY 환경 변수를 설정해야 합니다
 * - ./gradlew run 으로 실행
 * - 'quit', 'exit', '종료' 중 하나를 입력하면 종료
 */
@Slf4j
public class UpstageCliChat {
    
    private final BaseLLM llm;
    private final BufferedReader reader;
    
    public UpstageCliChat() {
        // Upstage Solar 모델 초기화
        Config config = Config.builder()
            .temperature(0.7)
            .maxTokens(1000)
            .build();
        
        // 시스템 프롬프트 설정
        String systemPrompt = "당신은 친근하고 도움이 되는 AI 어시스턴트입니다. "
            + "한국어로 답변해주세요. 이전 대화 내용을 기억하고 문맥에 맞게 답변해주세요. "
            + "사용자가 도움을 요청하면 친절하고 자세하게 설명해주세요.";
            
        this.llm = LLM.create("solar-1-mini-chat", config)
            .enableConversation(systemPrompt); // 자동 대화 관리 활성화
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        
        log.info("Upstage Solar CLI Chat 초기화 완료 - 모델: {} (자동 대화 관리 활성화)", llm.getModel());
    }
    
    public void start() {
        System.out.println("============================================================");
        System.out.println("🌟 Upstage Solar AI 챗봇에 오신 것을 환영합니다!");
        System.out.println("============================================================");
        System.out.println("한국어에 최적화된 Solar 모델과 대화를 시작해보세요.");
        System.out.println("💬 자동 대화 관리 기능으로 이전 대화를 기억합니다.");
        System.out.println("종료하려면 'quit', 'exit' 또는 '종료'를 입력하세요.");
        System.out.println("------------------------------------------------------------\n");
        
        while (true) {
            try {
                System.out.print("👤 사용자: ");
                String input = reader.readLine();
                
                if (input == null || isExitCommand(input)) {
                    break;
                }
                
                if (input.trim().isEmpty()) {
                    System.out.println("💬 메시지를 입력해주세요.\n");
                    continue;
                }
                
                System.out.print("🤖 Solar: ");
                
                // 자동 대화 관리를 사용하여 메시지 전송
                LLMReply reply = llm.chat(input);
                String response = reply.getText();
                
                System.out.println(response);
                System.out.println();
                
            } catch (IOException e) {
                log.error("입력 읽기 오류", e);
                System.err.println("입력을 읽는 중 오류가 발생했습니다.");
            } catch (Exception e) {
                log.error("LLM 응답 오류", e);
                System.err.println("응답 생성 중 오류가 발생했습니다: " + e.getMessage());
                System.err.println("API 키가 올바르게 설정되어 있는지 확인해주세요.");
            }
        }
        
        System.out.println("\n------------------------------------------------------------");
        System.out.println("🌟 대화를 종료합니다. 감사합니다!");
        System.out.println("============================================================");
    }
    
    private boolean isExitCommand(String input) {
        String normalized = input.trim().toLowerCase();
        return "quit".equals(normalized) || 
               "exit".equals(normalized) || 
               "종료".equals(normalized) ||
               "끝".equals(normalized);
    }
    
    public static void main(String[] args) {
        // API 키 확인
        String apiKey = System.getenv("UPSTAGE_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("❌ 오류: UPSTAGE_API_KEY 환경 변수가 설정되지 않았습니다.");
            System.err.println("다음 명령으로 API 키를 설정해주세요:");
            System.err.println("export UPSTAGE_API_KEY='your-api-key'");
            System.exit(1);
        }
        
        try {
            UpstageCliChat chat = new UpstageCliChat();
            chat.start();
        } catch (Exception e) {
            log.error("애플리케이션 실행 중 오류 발생", e);
            System.err.println("애플리케이션 실행 중 오류가 발생했습니다: " + e.getMessage());
            System.exit(1);
        }
    }
}