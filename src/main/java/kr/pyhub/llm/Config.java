package kr.pyhub.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@With
public class Config {
    
    private String apiKey;
    private Double temperature;
    private Integer maxTokens;
    private Double topP;
    private String systemPrompt;
    private String organizationId;
    private String projectId;
    private String baseUrl;
    
    /**
     * Create a Config with just an API key
     */
    public static Config withApiKey(String apiKey) {
        return Config.builder()
            .apiKey(apiKey)
            .build();
    }
    
    /**
     * Load configuration from environment variables based on provider
     * 
     * @param provider The provider name (e.g., "openai", "anthropic")
     * @return Config loaded from environment
     */
    public static Config fromEnvironment(String provider) {
        ConfigBuilder builder = Config.builder();
        
        switch (provider.toLowerCase()) {
            case "openai":
                builder.apiKey(System.getenv("OPENAI_API_KEY"))
                    .organizationId(System.getenv("OPENAI_ORG_ID"))
                    .projectId(System.getenv("OPENAI_PROJECT_ID"))
                    .baseUrl(System.getenv("OPENAI_BASE_URL"));
                break;
                
            case "anthropic":
                builder.apiKey(System.getenv("ANTHROPIC_API_KEY"))
                    .baseUrl(System.getenv("ANTHROPIC_BASE_URL"));
                break;
                
            case "google":
                builder.apiKey(System.getenv("GOOGLE_API_KEY"));
                break;
                
            case "upstage":
                builder.apiKey(System.getenv("UPSTAGE_API_KEY"));
                break;
                
            case "ollama":
                String ollamaHost = System.getenv("OLLAMA_HOST");
                if (ollamaHost == null) {
                    ollamaHost = "http://localhost:11434";
                }
                builder.baseUrl(ollamaHost);
                break;
                
            default:
                // Try generic environment variable
                builder.apiKey(System.getenv(provider.toUpperCase() + "_API_KEY"));
        }
        
        return builder.build();
    }
    
    /**
     * Merge this config with another, with the other config taking precedence
     * 
     * @param other The config to merge with
     * @return A new Config with merged values
     */
    public Config merge(Config other) {
        if (other == null) {
            return this;
        }
        
        return Config.builder()
            .apiKey(other.apiKey != null ? other.apiKey : this.apiKey)
            .temperature(other.temperature != null ? other.temperature : this.temperature)
            .maxTokens(other.maxTokens != null ? other.maxTokens : this.maxTokens)
            .topP(other.topP != null ? other.topP : this.topP)
            .systemPrompt(other.systemPrompt != null ? other.systemPrompt : this.systemPrompt)
            .organizationId(other.organizationId != null ? other.organizationId : this.organizationId)
            .projectId(other.projectId != null ? other.projectId : this.projectId)
            .baseUrl(other.baseUrl != null ? other.baseUrl : this.baseUrl)
            .build();
    }
    
    /**
     * Custom builder with validation
     */
    public static class ConfigBuilder {
        
        public ConfigBuilder temperature(Double temperature) {
            if (temperature != null && (temperature < 0.0 || temperature > 2.0)) {
                throw new IllegalArgumentException("Temperature must be between 0 and 2");
            }
            this.temperature = temperature;
            return this;
        }
        
        public ConfigBuilder maxTokens(Integer maxTokens) {
            if (maxTokens != null && maxTokens <= 0) {
                throw new IllegalArgumentException("Max tokens must be positive");
            }
            this.maxTokens = maxTokens;
            return this;
        }
        
        public ConfigBuilder topP(Double topP) {
            if (topP != null && (topP < 0.0 || topP > 1.0)) {
                throw new IllegalArgumentException("TopP must be between 0 and 1");
            }
            this.topP = topP;
            return this;
        }
    }
}