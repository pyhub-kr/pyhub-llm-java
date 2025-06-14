package kr.pyhub.llm.providers;

import kr.pyhub.llm.Config;
import kr.pyhub.llm.base.BaseLLM;
import kr.pyhub.llm.types.LLMReply;
import kr.pyhub.llm.types.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Upstage Solar implementation.
 * TODO: Implement using HTTP client for Upstage API
 */
@Slf4j
public class UpstageLLM extends BaseLLM {
    
    public UpstageLLM(String model) {
        super(model);
    }
    
    public UpstageLLM(String model, Config config) {
        super(model);
    }
    
    @Override
    protected LLMReply doAsk(List<Message> messages) {
        // TODO: Implement Upstage API call
        throw new UnsupportedOperationException("UpstageLLM not yet implemented");
    }
}