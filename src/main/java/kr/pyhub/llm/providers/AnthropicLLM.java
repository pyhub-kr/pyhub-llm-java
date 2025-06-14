package kr.pyhub.llm.providers;

import kr.pyhub.llm.Config;
import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import kr.pyhub.llm.exceptions.LLMException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Anthropic Claude implementation using the official Anthropic Java SDK.
 * This is a simplified implementation that will be enhanced in future versions.
 */
@Slf4j
public class AnthropicLLM extends BaseLLM {
    
    private final Config config;
    
    /**
     * Create AnthropicLLM with model name and optional API key
     */
    public AnthropicLLM(String model, String apiKey) {
        this(model, Config.withApiKey(apiKey));
    }
    
    /**
     * Create AnthropicLLM with model name (uses environment variables)
     */
    public AnthropicLLM(String model) {
        this(model, Config.fromEnvironment("anthropic"));
    }
    
    /**
     * Create AnthropicLLM with model name and config
     */
    public AnthropicLLM(String model, Config config) {
        super(model);
        this.config = config;
        
        // Apply config settings
        if (config.getTemperature() != null) {
            withTemperature(config.getTemperature());
        }
        if (config.getMaxTokens() != null) {
            withMaxTokens(config.getMaxTokens());
        }
        if (config.getSystemPrompt() != null) {
            withSystemPrompt(config.getSystemPrompt());
        }
        
        log.info("Created AnthropicLLM for model: {}", model);
    }
    
    @Override
    protected LLMReply doAsk(List<Message> messages) {
        // TODO: Implement actual Anthropic API call
        // For now, throw an exception indicating this is not yet implemented
        throw new LLMException("AnthropicLLM implementation is not yet complete. " +
            "This will be implemented in a future version using the Anthropic Java SDK.");
    }
}