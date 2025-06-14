package kr.pyhub.llm.base;

import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import kr.pyhub.llm.exceptions.LLMException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Getter
public abstract class BaseLLM {
    
    private final String model;
    private String systemPrompt;
    private Double temperature = 1.0;
    private Integer maxTokens;
    
    protected BaseLLM(String model) {
        this.model = model;
    }
    
    /**
     * Send a prompt to the LLM and get a response synchronously.
     * 
     * @param prompt The user prompt
     * @return The LLM response
     */
    public LLMReply ask(String prompt) {
        List<Message> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Message.system(systemPrompt));
        }
        messages.add(Message.user(prompt));
        return ask(messages);
    }
    
    /**
     * Send messages to the LLM and get a response synchronously.
     * 
     * @param messages The conversation messages
     * @return The LLM response
     */
    public LLMReply ask(List<Message> messages) {
        try {
            log.debug("Sending {} messages to {}", messages.size(), model);
            return doAsk(messages);
        } catch (Exception e) {
            log.error("Error calling LLM {}: {}", model, e.getMessage(), e);
            throw new LLMException("Failed to get response from " + model, e);
        }
    }
    
    /**
     * Send a prompt to the LLM and get a response asynchronously.
     * 
     * @param prompt The user prompt
     * @return A future containing the LLM response
     */
    public CompletableFuture<LLMReply> askAsync(String prompt) {
        return CompletableFuture.supplyAsync(() -> ask(prompt))
            .exceptionally(throwable -> {
                if (throwable instanceof CompletionException) {
                    throwable = throwable.getCause();
                }
                throw new LLMException("Async call failed", throwable);
            });
    }
    
    /**
     * Send messages to the LLM and get a response asynchronously.
     * 
     * @param messages The conversation messages
     * @return A future containing the LLM response
     */
    public CompletableFuture<LLMReply> askAsync(List<Message> messages) {
        return CompletableFuture.supplyAsync(() -> ask(messages))
            .exceptionally(throwable -> {
                if (throwable instanceof CompletionException) {
                    throwable = throwable.getCause();
                }
                throw new LLMException("Async call failed", throwable);
            });
    }
    
    /**
     * Set the system prompt for the conversation.
     * 
     * @param systemPrompt The system prompt
     * @return This instance for method chaining
     */
    public BaseLLM withSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
        return this;
    }
    
    /**
     * Set the temperature for response generation.
     * 
     * @param temperature The temperature (0.0 to 2.0)
     * @return This instance for method chaining
     */
    public BaseLLM withTemperature(double temperature) {
        if (temperature < 0.0 || temperature > 2.0) {
            throw new IllegalArgumentException("Temperature must be between 0 and 2");
        }
        this.temperature = temperature;
        return this;
    }
    
    /**
     * Set the maximum number of tokens for response generation.
     * 
     * @param maxTokens The maximum number of tokens
     * @return This instance for method chaining
     */
    public BaseLLM withMaxTokens(int maxTokens) {
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("Max tokens must be positive");
        }
        this.maxTokens = maxTokens;
        return this;
    }
    
    /**
     * Abstract method to be implemented by subclasses for actual LLM communication.
     * 
     * @param messages The messages to send
     * @return The LLM response
     */
    protected abstract LLMReply doAsk(List<Message> messages);
}