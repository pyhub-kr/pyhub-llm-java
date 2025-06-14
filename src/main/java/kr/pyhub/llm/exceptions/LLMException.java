package kr.pyhub.llm.exceptions;

public class LLMException extends RuntimeException {
    
    public LLMException(String message) {
        super(message);
    }
    
    public LLMException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public LLMException(Throwable cause) {
        super(cause);
    }
}