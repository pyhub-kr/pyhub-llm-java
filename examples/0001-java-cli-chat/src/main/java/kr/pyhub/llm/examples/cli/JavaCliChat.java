package kr.pyhub.llm.examples.cli;

import kr.pyhub.llm.Config;
import kr.pyhub.llm.LLM;
import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.cache.MemoryCache;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 간단한 CLI 채팅 예시
 * 
 * OpenAI GPT 모델과 대화할 수 있는 명령행 인터페이스를 제공합니다.
 * 
 * 사용법:
 * - 일반적인 대화 입력
 * - /quit 입력시 프로그램 종료
 * 
 * 환경변수: UPSTAGE_API_KEY
 * 또는 --api-key 매개변수로 API 키 전달
 */
public class JavaCliChat {
    private static final String DEFAULT_MODEL = "solar-1-mini-chat";
    private static final String QUIT_COMMAND = "/quit";
    
    private final BaseLLM llm;
    private final BufferedReader reader;
    private final List<Message> conversationHistory;
    
    public JavaCliChat(String apiKey) {
        Config config = Config.builder()
            .apiKey(apiKey)
            .temperature(0.7)
            .maxTokens(1000)
            .build();
            
        this.llm = LLM.create(DEFAULT_MODEL, config)
            .withCache(new MemoryCache(100, 30, java.util.concurrent.TimeUnit.MINUTES));
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.conversationHistory = new ArrayList<>();
        
        // 시스템 프롬프트를 대화 히스토리에 추가
        String systemPrompt = "당신은 친근하고 도움이 되는 AI 어시스턴트입니다. 한국어로 답변해주세요. 이전 대화 내용을 기억하고 문맥에 맞게 답변해주세요.";
        this.conversationHistory.add(Message.system(systemPrompt));
    }
    
    public void start() {
        System.out.println("=== Java CLI Chat ===");
        System.out.println("Upstage " + DEFAULT_MODEL + " 모델과 채팅을 시작합니다.");
        System.out.println("'" + QUIT_COMMAND + "' 입력시 종료됩니다.");
        System.out.println("캐싱이 활성화되어 있습니다 (30분간 유지)");
        System.out.println("========================");
        System.out.println();
        
        String input;
        try {
            while (true) {
                System.out.print("You: ");
                input = reader.readLine();
                
                if (input == null || QUIT_COMMAND.equals(input.trim())) {
                    System.out.println("채팅을 종료합니다. 안녕히 가세요!");
                    break;
                }
                
                if (input.trim().isEmpty()) {
                    continue;
                }
                
                // 사용자 메시지를 대화 히스토리에 추가
                conversationHistory.add(Message.user(input));
                
                try {
                    // 전체 대화 히스토리를 LLM에게 전달
                    LLMReply reply = llm.ask(conversationHistory);
                    String response = reply.getText();
                    
                    // 응답 출력 (스트리밍 효과)
                    System.out.print("AI: ");
                    simulateStreamingOutput(response);
                    System.out.println();
                    System.out.println();
                    
                    // AI 응답을 대화 히스토리에 추가
                    conversationHistory.add(Message.assistant(response));
                    
                } catch (Exception e) {
                    System.err.println("오류 발생: " + e.getMessage());
                    System.out.println();
                }
            }
        } catch (IOException e) {
            System.err.println("입력 오류: " + e.getMessage());
        }
    }
    
    /**
     * 스트리밍 효과를 시뮬레이션하여 응답을 점진적으로 출력
     */
    private void simulateStreamingOutput(String text) {
        char[] chars = text.toCharArray();
        for (char c : chars) {
            System.out.print(c);
            System.out.flush();
            
            try {
                // 스트리밍 효과를 위한 짧은 지연
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        String apiKey = null;
        
        // 명령행 인수에서 API 키 찾기
        for (int i = 0; i < args.length - 1; i++) {
            if ("--api-key".equals(args[i])) {
                apiKey = args[i + 1];
                break;
            }
        }
        
        // 환경변수에서 API 키 찾기
        if (apiKey == null) {
            apiKey = System.getenv("UPSTAGE_API_KEY");
        }
        
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("Error: Upstage API 키가 필요합니다.");
            System.err.println("환경변수 UPSTAGE_API_KEY를 설정하거나 --api-key 매개변수를 사용하세요.");
            System.err.println();
            System.err.println("사용법:");
            System.err.println("  java -jar cli-chat.jar --api-key YOUR_API_KEY");
            System.err.println("  또는");
            System.err.println("  export UPSTAGE_API_KEY=your_api_key");
            System.err.println("  java -jar cli-chat.jar");
            System.exit(1);
        }
        
        try {
            JavaCliChat chat = new JavaCliChat(apiKey);
            chat.start();
        } catch (Exception e) {
            System.err.println("애플리케이션 시작 오류: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}