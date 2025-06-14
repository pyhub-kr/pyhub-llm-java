package kr.pyhub.llm.providers;

import kr.pyhub.llm.Config;
import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Google Gemini implementation.
 * TODO: Implement using Google AI SDK
 */
@Slf4j
public class GoogleLLM extends BaseLLM {
    
    public GoogleLLM(String model) {
        super(model);
    }
    
    public GoogleLLM(String model, Config config) {
        super(model);
    }
    
    @Override
    protected LLMReply doAsk(List<Message> messages) {
        // TODO: Implement Google API call
        throw new UnsupportedOperationException("GoogleLLM not yet implemented");
    }
}