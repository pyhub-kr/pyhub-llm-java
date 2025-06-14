package kr.pyhub.llm.tools;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 도구 레지스트리.
 * 
 * 사용 가능한 도구들을 관리합니다.
 */
@Slf4j
public class ToolRegistry {
    
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();
    
    /**
     * 도구 등록
     * 
     * @param tool 등록할 도구
     */
    public void register(Tool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("Tool cannot be null");
        }
        
        String name = tool.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tool name cannot be empty");
        }
        
        tools.put(name, tool);
        log.info("Registered tool: {}", name);
    }
    
    /**
     * 여러 도구 일괄 등록
     * 
     * @param toolsToRegister 등록할 도구들
     */
    public void registerAll(Tool... toolsToRegister) {
        for (Tool tool : toolsToRegister) {
            register(tool);
        }
    }
    
    /**
     * 도구 등록 해제
     * 
     * @param name 도구 이름
     */
    public void unregister(String name) {
        Tool removed = tools.remove(name);
        if (removed != null) {
            log.info("Unregistered tool: {}", name);
        }
    }
    
    /**
     * 도구 조회
     * 
     * @param name 도구 이름
     * @return 도구 또는 null
     */
    public Tool getTool(String name) {
        return tools.get(name);
    }
    
    /**
     * 모든 도구 조회
     * 
     * @return 도구 목록
     */
    public List<Tool> getTools() {
        return new ArrayList<>(tools.values());
    }
    
    /**
     * 활성화된 도구만 조회
     * 
     * @return 활성화된 도구 목록
     */
    public List<Tool> getEnabledTools() {
        List<Tool> enabledTools = new ArrayList<>();
        for (Tool tool : tools.values()) {
            if (tool.isEnabled()) {
                enabledTools.add(tool);
            }
        }
        return enabledTools;
    }
    
    /**
     * 도구 이름 목록 조회
     * 
     * @return 도구 이름 집합
     */
    public Set<String> getToolNames() {
        return new HashSet<>(tools.keySet());
    }
    
    /**
     * 도구 존재 여부 확인
     * 
     * @param name 도구 이름
     * @return 존재 여부
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }
    
    /**
     * 모든 도구 삭제
     */
    public void clear() {
        tools.clear();
        log.info("Cleared all tools from registry");
    }
    
    /**
     * 등록된 도구 개수
     * 
     * @return 도구 개수
     */
    public int size() {
        return tools.size();
    }
}