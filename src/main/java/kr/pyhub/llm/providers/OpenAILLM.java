package kr.pyhub.llm.providers;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.completions.CompletionUsage;
import kr.pyhub.llm.Config;
import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import kr.pyhub.llm.exceptions.LLMException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class OpenAILLM extends BaseLLM {
    
    private final OpenAIClient client;
    private final Config config;
    
    /**
     * Create OpenAILLM with model name and optional API key
     */
    public OpenAILLM(String model, String apiKey) {
        this(model, Config.withApiKey(apiKey));
    }
    
    /**
     * Create OpenAILLM with model name (uses environment variables)
     */
    public OpenAILLM(String model) {
        this(model, Config.fromEnvironment("openai"));
    }
    
    /**
     * Create OpenAILLM with model name and config
     */
    public OpenAILLM(String model, Config config) {
        super(model);
        this.config = config;
        
        // Create OpenAI client
        OpenAIOkHttpClient.Builder builder = OpenAIOkHttpClient.builder();
        
        if (config.getApiKey() != null) {
            builder.apiKey(config.getApiKey());
        }
        
        if (config.getOrganizationId() != null) {
            builder.organization(config.getOrganizationId());
        }
        
        if (config.getProjectId() != null) {
            builder.project(config.getProjectId());
        }
        
        if (config.getBaseUrl() != null) {
            builder.baseUrl(config.getBaseUrl());
        }
        
        this.client = builder.build();
        
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
    }
    
    /**
     * Constructor for testing with mocked client
     */
    OpenAILLM(String model, OpenAIClient client) {
        super(model);
        this.client = client;
        this.config = Config.builder().build();
    }
    
    @Override
    protected LLMReply doAsk(List<Message> messages) {
        try {
            // Build request parameters
            ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .model(getModel());
            
            // Add messages using the convenience methods
            for (Message message : messages) {
                switch (message.getRole()) {
                    case SYSTEM:
                        paramsBuilder.addSystemMessage(message.getContent());
                        break;
                        
                    case USER:
                        paramsBuilder.addUserMessage(message.getContent());
                        break;
                        
                    case ASSISTANT:
                        // Assistant messages need to be added differently
                        log.warn("Assistant messages are not directly supported by convenience methods");
                        break;
                        
                    case TOOL:
                        // Tool messages require special handling
                        log.warn("Tool messages are not yet fully supported");
                        break;
                        
                    default:
                        throw new IllegalArgumentException("Unknown message role: " + message.getRole());
                }
            }
            
            // Apply optional parameters
            if (getTemperature() != null) {
                paramsBuilder.temperature(getTemperature());
            }
            
            if (getMaxTokens() != null) {
                paramsBuilder.maxCompletionTokens(getMaxTokens());
            }
            
            if (config.getTopP() != null) {
                paramsBuilder.topP(config.getTopP());
            }
            
            // Make API call
            ChatCompletion completion = client.chat().completions().create(paramsBuilder.build());
            
            // Extract response from the first choice
            List<ChatCompletion.Choice> choices = completion.choices();
            if (choices == null || choices.isEmpty()) {
                throw new LLMException("No choices returned from OpenAI API");
            }
            
            ChatCompletion.Choice firstChoice = choices.get(0);
            String responseText = firstChoice.message().content().orElse("");
            
            // Build reply
            LLMReply.LLMReplyBuilder replyBuilder = LLMReply.builder()
                .text(responseText)
                .model(completion.model())
                .finishReason(firstChoice.finishReason().toString());
            
            // Add usage if available
            if (completion.usage().isPresent()) {
                CompletionUsage usage = completion.usage().get();
                replyBuilder.usage(LLMReply.Usage.builder()
                    .promptTokens((int) usage.promptTokens())
                    .completionTokens((int) usage.completionTokens())
                    .totalTokens((int) usage.totalTokens())
                    .build());
            }
            
            return replyBuilder.build();
            
        } catch (Exception e) {
            log.error("Failed to call OpenAI API: {}", e.getMessage(), e);
            throw new LLMException("Failed to call OpenAI API", e);
        }
    }
}