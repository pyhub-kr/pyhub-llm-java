package kr.pyhub.llm;

import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.providers.OpenAILLM;
import kr.pyhub.llm.exceptions.LLMException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class LLMTest {
    
    private Map<String, String> originalEnv;
    
    @BeforeEach
    void setUp() {
        // Save original environment variables
        originalEnv = new HashMap<>();
        originalEnv.put("OPENAI_API_KEY", System.getenv("OPENAI_API_KEY"));
        originalEnv.put("ANTHROPIC_API_KEY", System.getenv("ANTHROPIC_API_KEY"));
    }
    
    @AfterEach
    void tearDown() {
        // Restore environment variables
        // Note: In real tests, we'd use a library like system-lambda or powermock
        // For now, we'll just note that env vars should be mocked
    }
    
    @Test
    @DisplayName("create 메서드는 모델 이름으로 적절한 LLM 인스턴스를 생성해야 한다")
    void testCreateWithModelName() {
        // Given
        String modelName = "gpt-4o-mini";
        
        // When
        BaseLLM llm = LLM.create(modelName);
        
        // Then
        assertThat(llm).isNotNull();
        assertThat(llm).isInstanceOf(OpenAILLM.class);
        assertThat(llm.getModel()).isEqualTo(modelName);
    }
    
    @Test
    @DisplayName("create 메서드는 API 키를 직접 전달받을 수 있어야 한다")
    void testCreateWithApiKey() {
        // Given
        String modelName = "gpt-3.5-turbo";
        String apiKey = "test-api-key";
        
        // When
        BaseLLM llm = LLM.create(modelName, apiKey);
        
        // Then
        assertThat(llm).isNotNull();
        assertThat(llm).isInstanceOf(OpenAILLM.class);
        assertThat(llm.getModel()).isEqualTo(modelName);
        // API key is stored internally, we can't directly verify it
    }
    
    @Test
    @DisplayName("create 메서드는 Config 객체를 받을 수 있어야 한다")
    void testCreateWithConfig() {
        // Given
        String modelName = "gpt-4";
        Config config = Config.builder()
            .apiKey("test-api-key")
            .temperature(0.7)
            .maxTokens(1000)
            .build();
        
        // When
        BaseLLM llm = LLM.create(modelName, config);
        
        // Then
        assertThat(llm).isNotNull();
        assertThat(llm).isInstanceOf(OpenAILLM.class);
        assertThat(llm.getModel()).isEqualTo(modelName);
        assertThat(llm.getTemperature()).isEqualTo(0.7);
        assertThat(llm.getMaxTokens()).isEqualTo(1000);
    }
    
    @Test
    @DisplayName("create 메서드는 다양한 OpenAI 모델을 지원해야 한다")
    void testCreateVariousOpenAIModels() {
        String[] openAIModels = {
            "gpt-4", "gpt-4-turbo", "gpt-4o", "gpt-4o-mini",
            "gpt-3.5-turbo", "gpt-3.5-turbo-16k"
        };
        
        for (String model : openAIModels) {
            BaseLLM llm = LLM.create(model);
            assertThat(llm).isInstanceOf(OpenAILLM.class);
            assertThat(llm.getModel()).isEqualTo(model);
        }
    }
    
    @Test
    @DisplayName("create 메서드는 Anthropic 모델을 지원해야 한다")
    void testCreateAnthropicModels() {
        String[] anthropicModels = {
            "claude-3-opus", "claude-3-sonnet", "claude-3-haiku",
            "claude-2.1", "claude-2", "claude-instant"
        };
        
        for (String model : anthropicModels) {
            BaseLLM llm = LLM.create(model);
            assertThat(llm).isInstanceOf(kr.pyhub.llm.providers.AnthropicLLM.class);
            assertThat(llm.getModel()).isEqualTo(model);
        }
    }
    
    @Test
    @DisplayName("create 메서드는 Google 모델을 지원해야 한다")
    void testCreateGoogleModels() {
        String[] googleModels = {
            "gemini-pro", "gemini-pro-vision", "gemini-1.5-pro", "gemini-1.5-flash"
        };
        
        for (String model : googleModels) {
            BaseLLM llm = LLM.create(model);
            assertThat(llm).isInstanceOf(kr.pyhub.llm.providers.GoogleLLM.class);
            assertThat(llm.getModel()).isEqualTo(model);
        }
    }
    
    @Test
    @DisplayName("create 메서드는 Ollama 모델을 지원해야 한다")
    void testCreateOllamaModels() {
        String[] ollamaModels = {
            "ollama:llama2", "ollama:mistral", "ollama:codellama",
            "ollama:phi", "ollama:neural-chat"
        };
        
        for (String model : ollamaModels) {
            BaseLLM llm = LLM.create(model);
            assertThat(llm).isInstanceOf(kr.pyhub.llm.providers.OllamaLLM.class);
            assertThat(llm.getModel()).isEqualTo(model.substring(7)); // Remove "ollama:" prefix
        }
    }
    
    @Test
    @DisplayName("create 메서드는 Upstage 모델을 지원해야 한다")
    void testCreateUpstageModels() {
        String[] upstageModels = {
            "upstage:solar-1-mini", "upstage:solar-1-small"
        };
        
        for (String model : upstageModels) {
            BaseLLM llm = LLM.create(model);
            assertThat(llm).isInstanceOf(kr.pyhub.llm.providers.UpstageLLM.class);
            assertThat(llm.getModel()).isEqualTo(model.substring(8)); // Remove "upstage:" prefix
        }
    }
    
    @Test
    @DisplayName("create 메서드는 알 수 없는 모델에 대해 예외를 발생시켜야 한다")
    void testCreateUnknownModel() {
        assertThatThrownBy(() -> LLM.create("unknown-model"))
            .isInstanceOf(LLMException.class)
            .hasMessageContaining("Unknown model: unknown-model");
    }
    
    @Test
    @DisplayName("create 메서드는 null 모델 이름에 대해 예외를 발생시켜야 한다")
    void testCreateNullModel() {
        assertThatThrownBy(() -> LLM.create(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Model name cannot be null or empty");
    }
    
    @Test
    @DisplayName("create 메서드는 빈 모델 이름에 대해 예외를 발생시켜야 한다")
    void testCreateEmptyModel() {
        assertThatThrownBy(() -> LLM.create(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Model name cannot be null or empty");
    }
    
    @Test
    @DisplayName("registerProvider 메서드는 커스텀 프로바이더를 등록할 수 있어야 한다")
    void testRegisterProvider() {
        // Given
        String prefix = "custom";
        java.util.function.BiFunction<String, Config, BaseLLM> provider = (model, config) -> new TestCustomLLM(model);
        
        // When
        LLM.registerProvider(prefix, provider);
        BaseLLM llm = LLM.create("custom:my-model");
        
        // Then
        assertThat(llm).isInstanceOf(TestCustomLLM.class);
        assertThat(llm.getModel()).isEqualTo("custom:my-model");
    }
    
    // Test implementation for custom provider
    private static class TestCustomLLM extends BaseLLM {
        public TestCustomLLM(String model) {
            super(model);
        }
        
        @Override
        protected kr.pyhub.llm.types.LLMReply doAsk(java.util.List<kr.pyhub.llm.types.Message> messages) {
            return kr.pyhub.llm.types.LLMReply.builder()
                .text("Custom response")
                .model(getModel())
                .build();
        }
    }
}