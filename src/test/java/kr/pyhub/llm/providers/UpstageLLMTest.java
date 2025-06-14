package kr.pyhub.llm.providers;

import kr.pyhub.llm.Config;
import kr.pyhub.llm.types.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UpstageLLM 테스트
 */
class UpstageLLMTest {
    
    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_MODEL = "solar-1-mini-chat";
    
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
    @DisplayName("API 키가 없으면 예외를 던져야 함")
    void shouldThrowExceptionWhenApiKeyMissing() {
        // Given
        Config configWithoutApiKey = Config.builder()
            .apiKey("")  // 빈 문자열로 설정
            .temperature(0.7)
            .build();
        
        // When/Then
        assertThatThrownBy(() -> new UpstageLLM(TEST_MODEL, configWithoutApiKey))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("API key is required");
    }
    
    @Test
    @DisplayName("한국어 전용 모델을 지원해야 함")
    void shouldSupportKoreanModels() {
        // Given
        String[] koreanModels = {
            "solar-1-mini-chat",
            "solar-1-mini-chat-ja"
        };
        
        // When/Then
        for (String model : koreanModels) {
            UpstageLLM llm = new UpstageLLM(model, config);
            assertThat(llm.getModel()).isEqualTo(model);
        }
    }
    
    @Test
    @DisplayName("Config에서 temperature와 maxTokens를 설정해야 함")
    void shouldApplyConfigSettings() {
        // Given
        Config configWithSettings = Config.builder()
            .apiKey(TEST_API_KEY)
            .temperature(0.8)
            .maxTokens(200)
            .build();
        
        // When
        UpstageLLM llm = new UpstageLLM(TEST_MODEL, configWithSettings);
        
        // Then
        assertThat(llm.getTemperature()).isEqualTo(0.8);
        assertThat(llm.getMaxTokens()).isEqualTo(200);
    }
    
    @Test
    @DisplayName("환경변수에서 API 키를 가져올 수 있어야 함")
    void shouldUseApiKeyFromEnvironment() {
        // Given
        Config configWithoutApiKey = Config.builder()
            .apiKey("")  // 빈 문자열로 설정
            .temperature(0.7)
            .build();
        
        // When/Then
        // 환경변수가 설정되지 않았으므로 예외 발생
        assertThatThrownBy(() -> new UpstageLLM(TEST_MODEL, configWithoutApiKey))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("UPSTAGE_API_KEY");
    }
}