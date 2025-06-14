package kr.pyhub.llm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class ConfigTest {
    
    private Map<String, String> originalEnv;
    
    @BeforeEach
    void setUp() {
        // Save original environment variables
        originalEnv = new HashMap<>();
        originalEnv.put("OPENAI_API_KEY", System.getenv("OPENAI_API_KEY"));
        originalEnv.put("ANTHROPIC_API_KEY", System.getenv("ANTHROPIC_API_KEY"));
        originalEnv.put("OPENAI_ORG_ID", System.getenv("OPENAI_ORG_ID"));
        originalEnv.put("OPENAI_PROJECT_ID", System.getenv("OPENAI_PROJECT_ID"));
        originalEnv.put("OPENAI_BASE_URL", System.getenv("OPENAI_BASE_URL"));
    }
    
    @AfterEach
    void tearDown() {
        // Note: In real tests, we'd use a library to mock environment variables
        // For now, we'll just note that env vars should be mocked
    }
    
    @Test
    @DisplayName("빌더 패턴으로 Config 객체를 생성할 수 있어야 한다")
    void testConfigBuilder() {
        // Given & When
        Config config = Config.builder()
            .apiKey("test-api-key")
            .temperature(0.7)
            .maxTokens(1000)
            .topP(0.9)
            .systemPrompt("You are a helpful assistant")
            .organizationId("org-123")
            .projectId("proj-456")
            .baseUrl("https://api.example.com")
            .build();
        
        // Then
        assertThat(config.getApiKey()).isEqualTo("test-api-key");
        assertThat(config.getTemperature()).isEqualTo(0.7);
        assertThat(config.getMaxTokens()).isEqualTo(1000);
        assertThat(config.getTopP()).isEqualTo(0.9);
        assertThat(config.getSystemPrompt()).isEqualTo("You are a helpful assistant");
        assertThat(config.getOrganizationId()).isEqualTo("org-123");
        assertThat(config.getProjectId()).isEqualTo("proj-456");
        assertThat(config.getBaseUrl()).isEqualTo("https://api.example.com");
    }
    
    @Test
    @DisplayName("API 키만으로 Config 객체를 생성할 수 있어야 한다")
    void testConfigWithApiKeyOnly() {
        // Given & When
        Config config = Config.withApiKey("test-api-key");
        
        // Then
        assertThat(config.getApiKey()).isEqualTo("test-api-key");
        assertThat(config.getTemperature()).isNull();
        assertThat(config.getMaxTokens()).isNull();
        assertThat(config.getTopP()).isNull();
        assertThat(config.getSystemPrompt()).isNull();
    }
    
    @Test
    @DisplayName("환경변수에서 Config를 로드할 수 있어야 한다")
    void testConfigFromEnvironment() {
        // Given
        // Assume environment variables are set (in real test, we'd mock this)
        
        // When
        Config config = Config.fromEnvironment("openai");
        
        // Then
        // Would check values from environment
        assertThat(config).isNotNull();
    }
    
    @Test
    @DisplayName("Config 객체를 복사하고 수정할 수 있어야 한다")
    void testConfigCopyAndModify() {
        // Given
        Config original = Config.builder()
            .apiKey("original-key")
            .temperature(0.5)
            .maxTokens(500)
            .build();
        
        // When
        Config modified = original.toBuilder()
            .temperature(0.8)
            .maxTokens(1000)
            .build();
        
        // Then
        assertThat(modified.getApiKey()).isEqualTo("original-key");
        assertThat(modified.getTemperature()).isEqualTo(0.8);
        assertThat(modified.getMaxTokens()).isEqualTo(1000);
        
        // Original should not be modified
        assertThat(original.getTemperature()).isEqualTo(0.5);
        assertThat(original.getMaxTokens()).isEqualTo(500);
    }
    
    @Test
    @DisplayName("temperature 값은 0과 2 사이여야 한다")
    void testTemperatureValidation() {
        // Valid temperature
        Config validConfig = Config.builder()
            .apiKey("test-key")
            .temperature(1.5)
            .build();
        assertThat(validConfig.getTemperature()).isEqualTo(1.5);
        
        // Invalid temperature should throw exception
        assertThatThrownBy(() -> Config.builder()
            .apiKey("test-key")
            .temperature(-0.1)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Temperature must be between 0 and 2");
            
        assertThatThrownBy(() -> Config.builder()
            .apiKey("test-key")
            .temperature(2.1)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Temperature must be between 0 and 2");
    }
    
    @Test
    @DisplayName("maxTokens는 양수여야 한다")
    void testMaxTokensValidation() {
        // Valid maxTokens
        Config validConfig = Config.builder()
            .apiKey("test-key")
            .maxTokens(1000)
            .build();
        assertThat(validConfig.getMaxTokens()).isEqualTo(1000);
        
        // Invalid maxTokens should throw exception
        assertThatThrownBy(() -> Config.builder()
            .apiKey("test-key")
            .maxTokens(0)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Max tokens must be positive");
            
        assertThatThrownBy(() -> Config.builder()
            .apiKey("test-key")
            .maxTokens(-100)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Max tokens must be positive");
    }
    
    @Test
    @DisplayName("topP 값은 0과 1 사이여야 한다")
    void testTopPValidation() {
        // Valid topP
        Config validConfig = Config.builder()
            .apiKey("test-key")
            .topP(0.9)
            .build();
        assertThat(validConfig.getTopP()).isEqualTo(0.9);
        
        // Invalid topP should throw exception
        assertThatThrownBy(() -> Config.builder()
            .apiKey("test-key")
            .topP(-0.1)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TopP must be between 0 and 1");
            
        assertThatThrownBy(() -> Config.builder()
            .apiKey("test-key")
            .topP(1.1)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("TopP must be between 0 and 1");
    }
    
    @Test
    @DisplayName("Config 병합 기능이 올바르게 동작해야 한다")
    void testConfigMerge() {
        // Given
        Config base = Config.builder()
            .apiKey("base-key")
            .temperature(0.5)
            .maxTokens(500)
            .build();
            
        Config override = Config.builder()
            .temperature(0.8)
            .systemPrompt("New prompt")
            .build();
        
        // When
        Config merged = base.merge(override);
        
        // Then
        assertThat(merged.getApiKey()).isEqualTo("base-key"); // From base
        assertThat(merged.getTemperature()).isEqualTo(0.8); // Overridden
        assertThat(merged.getMaxTokens()).isEqualTo(500); // From base
        assertThat(merged.getSystemPrompt()).isEqualTo("New prompt"); // From override
    }
}