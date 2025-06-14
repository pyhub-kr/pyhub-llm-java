package kr.pyhub.llm;

import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.providers.*;
import kr.pyhub.llm.exceptions.LLMException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Factory class for creating LLM instances based on model names.
 * This class provides a unified way to create different LLM implementations.
 */
@Slf4j
public class LLM {
    
    private static final Map<String, BiFunction<String, Config, BaseLLM>> PROVIDERS = new HashMap<>();
    
    static {
        // Register default providers
        registerDefaultProviders();
    }
    
    /**
     * Create an LLM instance based on the model name.
     * 
     * @param model The model name (e.g., "gpt-4", "claude-3-opus", "gemini-pro")
     * @return A configured LLM instance
     */
    public static BaseLLM create(String model) {
        return create(model, (Config) null);
    }
    
    /**
     * Create an LLM instance with an API key.
     * 
     * @param model The model name
     * @param apiKey The API key
     * @return A configured LLM instance
     */
    public static BaseLLM create(String model, String apiKey) {
        return create(model, Config.withApiKey(apiKey));
    }
    
    /**
     * Create an LLM instance with a configuration object.
     * 
     * @param model The model name
     * @param config The configuration
     * @return A configured LLM instance
     */
    public static BaseLLM create(String model, Config config) {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name cannot be null or empty");
        }
        
        // Find the provider for this model
        String providerKey = findProviderKey(model);
        BiFunction<String, Config, BaseLLM> provider = PROVIDERS.get(providerKey);
        
        if (provider == null) {
            throw new LLMException("Unknown model: " + model + ". Available providers: " + PROVIDERS.keySet());
        }
        
        try {
            return provider.apply(model, config);
        } catch (Exception e) {
            throw new LLMException("Failed to create LLM instance for model: " + model, e);
        }
    }
    
    /**
     * Register a custom provider.
     * 
     * @param prefix The provider prefix (e.g., "custom")
     * @param provider The provider function that creates LLM instances
     */
    public static void registerProvider(String prefix, BiFunction<String, Config, BaseLLM> provider) {
        PROVIDERS.put(prefix.toLowerCase(), provider);
        log.info("Registered LLM provider: {}", prefix);
    }
    
    /**
     * Find the provider key for a given model name.
     * 
     * @param model The model name
     * @return The provider key
     */
    private static String findProviderKey(String model) {
        String lowerModel = model.toLowerCase();
        
        // Check for explicit provider prefix (e.g., "ollama:llama2")
        int colonIndex = lowerModel.indexOf(':');
        if (colonIndex > 0) {
            return lowerModel.substring(0, colonIndex);
        }
        
        // Check for known model patterns
        if (lowerModel.startsWith("gpt-") || lowerModel.contains("gpt-3.5") || lowerModel.contains("gpt-4")) {
            return "openai";
        } else if (lowerModel.startsWith("claude-") || lowerModel.contains("claude")) {
            return "anthropic";
        } else if (lowerModel.startsWith("gemini-") || lowerModel.contains("gemini")) {
            return "google";
        } else if (lowerModel.startsWith("solar-")) {
            return "upstage";
        }
        
        // Default to the model name itself as the provider
        return lowerModel;
    }
    
    /**
     * Register default providers.
     */
    private static void registerDefaultProviders() {
        // OpenAI provider
        registerProvider("openai", (model, config) -> {
            if (config == null) {
                return new OpenAILLM(model);
            } else {
                return new OpenAILLM(model, config);
            }
        });
        
        // Anthropic provider
        registerProvider("anthropic", (model, config) -> {
            if (config == null) {
                return new AnthropicLLM(model);
            } else {
                return new AnthropicLLM(model, config);
            }
        });
        
        // Google provider
        registerProvider("google", (model, config) -> {
            if (config == null) {
                return new GoogleLLM(model);
            } else {
                return new GoogleLLM(model, config);
            }
        });
        
        // Ollama provider
        registerProvider("ollama", (model, config) -> {
            // Remove "ollama:" prefix if present
            String actualModel = model;
            if (model.toLowerCase().startsWith("ollama:")) {
                actualModel = model.substring(7);
            }
            
            if (config == null) {
                return new OllamaLLM(actualModel);
            } else {
                return new OllamaLLM(actualModel, config);
            }
        });
        
        // Upstage provider
        registerProvider("upstage", (model, config) -> {
            // Remove "upstage:" prefix if present
            String actualModel = model;
            if (model.toLowerCase().startsWith("upstage:")) {
                actualModel = model.substring(8);
            }
            
            if (config == null) {
                return new UpstageLLM(actualModel);
            } else {
                return new UpstageLLM(actualModel, config);
            }
        });
    }
    
    /**
     * Provider function interface for creating LLM instances.
     */
    @FunctionalInterface
    public interface LLMProvider {
        BaseLLM create(String model, Config config);
    }
}