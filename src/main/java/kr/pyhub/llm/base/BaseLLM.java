package kr.pyhub.llm.base;

import kr.pyhub.llm.cache.Cache;
import kr.pyhub.llm.conversation.Conversation;
import kr.pyhub.llm.tools.Tool;
import kr.pyhub.llm.tools.ToolRegistry;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import kr.pyhub.llm.types.StreamChunk;
import kr.pyhub.llm.exceptions.LLMException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Getter
public abstract class BaseLLM {
    
    private final String model;
    private String systemPrompt;
    private Double temperature = 1.0;
    private Integer maxTokens;
    private Cache cache;
    private ToolRegistry toolRegistry;
    private boolean toolsEnabled = true;
    private Conversation conversation;
    
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
            // 캐시 확인
            if (cache != null && cache.isEnabled()) {
                String cacheKey = cache.generateKey(messages, model, temperature, maxTokens);
                Optional<LLMReply> cachedReply = cache.get(cacheKey);
                
                if (cachedReply.isPresent()) {
                    log.debug("Returning cached response for {} messages", messages.size());
                    return cachedReply.get();
                }
                
                // 캐시 미스 - API 호출
                log.debug("Cache miss, sending {} messages to {}", messages.size(), model);
                LLMReply reply = doAsk(messages);
                
                // 응답 캐싱
                cache.put(cacheKey, reply);
                return reply;
            } else {
                // 캐시 없이 직접 호출
                log.debug("Sending {} messages to {}", messages.size(), model);
                return doAsk(messages);
            }
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
     * Set the cache for this LLM instance.
     * 
     * @param cache The cache implementation
     * @return This instance for method chaining
     */
    public BaseLLM withCache(Cache cache) {
        this.cache = cache;
        return this;
    }
    
    /**
     * Set the tool registry for this LLM instance.
     * 
     * @param toolRegistry The tool registry
     * @return This instance for method chaining
     */
    public BaseLLM withTools(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
        return this;
    }
    
    /**
     * Add tools to this LLM instance.
     * 
     * @param tools The tools to add
     * @return This instance for method chaining
     */
    public BaseLLM withTools(Tool... tools) {
        if (this.toolRegistry == null) {
            this.toolRegistry = new ToolRegistry();
        }
        this.toolRegistry.registerAll(tools);
        return this;
    }
    
    /**
     * Enable or disable tool usage.
     * 
     * @param enabled Whether tools are enabled
     * @return This instance for method chaining
     */
    public BaseLLM withToolsEnabled(boolean enabled) {
        this.toolsEnabled = enabled;
        return this;
    }
    
    /**
     * Get available tools for LLM calls.
     * 
     * @return List of enabled tools or empty list
     */
    protected List<Tool> getAvailableTools() {
        if (!toolsEnabled || toolRegistry == null) {
            return new ArrayList<>();
        }
        return toolRegistry.getEnabledTools();
    }
    
    /**
     * Enable conversation mode with automatic history management.
     * 
     * @return This instance for method chaining
     */
    public BaseLLM enableConversation() {
        return enableConversation(systemPrompt);
    }
    
    /**
     * Enable conversation mode with automatic history management.
     * 
     * @param systemPrompt System prompt for the conversation
     * @return This instance for method chaining
     */
    public BaseLLM enableConversation(String systemPrompt) {
        this.conversation = new Conversation(systemPrompt);
        return this;
    }
    
    /**
     * Disable conversation mode (return to stateless mode).
     * 
     * @return This instance for method chaining
     */
    public BaseLLM disableConversation() {
        this.conversation = null;
        return this;
    }
    
    /**
     * Get the current conversation instance.
     * 
     * @return Current conversation or null if not enabled
     */
    public Conversation getConversation() {
        return conversation;
    }
    
    /**
     * Check if conversation mode is enabled.
     * 
     * @return true if conversation mode is enabled
     */
    public boolean isConversationEnabled() {
        return conversation != null;
    }
    
    /**
     * Send a message in conversation mode and get response.
     * Automatically manages conversation history.
     * 
     * @param message User message content
     * @return LLM response
     * @throws IllegalStateException if conversation mode is not enabled
     */
    public LLMReply chat(String message) {
        if (conversation == null) {
            throw new IllegalStateException("Conversation mode is not enabled. Call enableConversation() first.");
        }
        
        // Add user message to conversation
        conversation.addUserMessage(message);
        
        // Get response using full conversation history
        LLMReply reply = ask(conversation.getMessages());
        
        // Add assistant response to conversation
        conversation.addAssistantMessage(reply.getText());
        
        return reply;
    }
    
    /**
     * Clear conversation history but keep the conversation session active.
     * 
     * @return This instance for method chaining
     * @throws IllegalStateException if conversation mode is not enabled
     */
    public BaseLLM clearConversation() {
        if (conversation == null) {
            throw new IllegalStateException("Conversation mode is not enabled. Call enableConversation() first.");
        }
        
        conversation.clear();
        return this;
    }
    
    /**
     * Send a prompt to the LLM and get a streaming response.
     * 
     * @param prompt The user prompt
     * @return A Flux of stream chunks
     */
    public Flux<StreamChunk> askStream(String prompt) {
        List<Message> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Message.system(systemPrompt));
        }
        messages.add(Message.user(prompt));
        return askStream(messages);
    }
    
    /**
     * Send messages to the LLM and get a streaming response.
     * Default implementation converts non-streaming response to a stream.
     * Override this method in subclasses to provide true streaming.
     * 
     * @param messages The conversation messages
     * @return A Flux of stream chunks
     */
    public Flux<StreamChunk> askStream(List<Message> messages) {
        // Default implementation: convert non-streaming response to stream
        return Flux.defer(() -> {
            try {
                LLMReply reply = ask(messages);
                String text = reply.getText();
                
                // Split text into words for simulated streaming
                String[] words = text.split(" ");
                Flux<StreamChunk> textChunks = Flux.fromArray(words)
                    .index()
                    .map(tuple -> {
                        Long index = tuple.getT1();
                        String word = tuple.getT2();
                        // Add space before words (except first)
                        String content = index > 0 ? " " + word : word;
                        return StreamChunk.text(content);
                    });
                
                // Add finish chunk
                return textChunks.concatWith(
                    Flux.just(StreamChunk.finish(reply.getFinishReason()))
                );
            } catch (Exception e) {
                return Flux.error(new LLMException("Streaming failed", e));
            }
        });
    }
    
    /**
     * Abstract method to be implemented by subclasses for actual LLM communication.
     * 
     * @param messages The messages to send
     * @return The LLM response
     */
    protected abstract LLMReply doAsk(List<Message> messages);
}