package kr.pyhub.llm.cache;

import kr.pyhub.llm.types.Message;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 캐시 구현을 위한 추상 클래스.
 * 
 * 공통 기능인 키 생성 로직을 제공합니다.
 */
@Slf4j
public abstract class AbstractCache implements Cache {
    
    @Override
    public String generateKey(List<Message> messages, String model, Double temperature, Integer maxTokens) {
        // 캐시 키 생성을 위한 문자열 조합
        StringBuilder keyBuilder = new StringBuilder();
        
        // 모델 정보
        keyBuilder.append("model:").append(model).append("|");
        
        // 파라미터 정보
        if (temperature != null) {
            keyBuilder.append("temp:").append(temperature).append("|");
        }
        if (maxTokens != null) {
            keyBuilder.append("max:").append(maxTokens).append("|");
        }
        
        // 메시지 정보
        String messagesStr = messages.stream()
            .map(msg -> msg.getRole() + ":" + msg.getContent())
            .collect(Collectors.joining("|"));
        keyBuilder.append("messages:").append(messagesStr);
        
        // SHA-256 해시로 변환
        return sha256(keyBuilder.toString());
    }
    
    /**
     * 문자열을 SHA-256 해시로 변환합니다.
     * 
     * @param input 입력 문자열
     * @return SHA-256 해시 문자열
     */
    protected String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // 바이트 배열을 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256은 항상 사용 가능하므로 이 예외는 발생하지 않음
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}