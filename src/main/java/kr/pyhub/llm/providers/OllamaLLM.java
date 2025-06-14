package kr.pyhub.llm.providers;

import kr.pyhub.llm.Config;
import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Ollama local LLM implementation.
 * TODO: Implement using HTTP client for Ollama API
 */
@Slf4j
public class OllamaLLM extends BaseLLM {
    
    public OllamaLLM(String model) {
        super(model);
    }
    
    public OllamaLLM(String model, Config config) {
        super(model);
    }
    
    @Override
    protected LLMReply doAsk(List<Message> messages) {
        // TODO: Implement Ollama API call
        throw new UnsupportedOperationException("OllamaLLM not yet implemented");
    }
}