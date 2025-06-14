package kr.pyhub.llm.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 도구 인터페이스 테스트
 */
class ToolTest {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }
    
    @Test
    @DisplayName("도구를 정의하고 실행할 수 있어야 함")
    void shouldDefineAndExecuteTool() {
        // Given
        Tool calculator = new CalculatorTool();
        Map<String, Object> args = new HashMap<>();
        args.put("operation", "add");
        args.put("a", 5);
        args.put("b", 3);
        
        // When
        ToolResult result = calculator.execute(args);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOutput()).isEqualTo("8");
    }
    
    @Test
    @DisplayName("도구 설명을 JSON Schema로 생성할 수 있어야 함")
    void shouldGenerateJsonSchema() {
        // Given
        Tool weatherTool = new WeatherTool();
        
        // When
        JsonNode schema = weatherTool.getSchema();
        
        // Then
        assertThat(schema.get("name").asText()).isEqualTo("get_weather");
        assertThat(schema.get("description").asText()).contains("weather");
        assertThat(schema.has("parameters")).isTrue();
        
        JsonNode parameters = schema.get("parameters");
        assertThat(parameters.get("type").asText()).isEqualTo("object");
        assertThat(parameters.has("properties")).isTrue();
        assertThat(parameters.get("required").isArray()).isTrue();
    }
    
    @Test
    @DisplayName("필수 파라미터가 없으면 에러를 반환해야 함")
    void shouldReturnErrorForMissingRequiredParameters() {
        // Given
        Tool weatherTool = new WeatherTool();
        Map<String, Object> args = new HashMap<>(); // location 없음
        
        // When
        ToolResult result = weatherTool.execute(args);
        
        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).contains("location");
    }
    
    @Test
    @DisplayName("여러 도구를 관리할 수 있어야 함")
    void shouldManageMultipleTools() {
        // Given
        ToolRegistry registry = new ToolRegistry();
        registry.register(new CalculatorTool());
        registry.register(new WeatherTool());
        registry.register(new SearchTool());
        
        // When
        List<Tool> tools = registry.getTools();
        Tool calculator = registry.getTool("calculator");
        
        // Then
        assertThat(tools).hasSize(3);
        assertThat(calculator).isNotNull();
        assertThat(calculator.getName()).isEqualTo("calculator");
    }
    
    /**
     * 테스트용 계산기 도구
     */
    static class CalculatorTool extends AbstractTool {
        public CalculatorTool() {
            super("calculator", "Performs basic arithmetic operations");
        }
        
        @Override
        public JsonNode getSchema() {
            ObjectNode schema = objectMapper.createObjectNode();
            schema.put("name", getName());
            schema.put("description", getDescription());
            
            ObjectNode parameters = objectMapper.createObjectNode();
            parameters.put("type", "object");
            
            ObjectNode properties = objectMapper.createObjectNode();
            
            ObjectNode operationProp = objectMapper.createObjectNode();
            operationProp.put("type", "string");
            ArrayNode operationEnum = objectMapper.createArrayNode();
            operationEnum.add("add").add("subtract").add("multiply").add("divide");
            operationProp.set("enum", operationEnum);
            operationProp.put("description", "The arithmetic operation");
            properties.set("operation", operationProp);
            
            ObjectNode aProp = objectMapper.createObjectNode();
            aProp.put("type", "number");
            aProp.put("description", "First operand");
            properties.set("a", aProp);
            
            ObjectNode bProp = objectMapper.createObjectNode();
            bProp.put("type", "number");
            bProp.put("description", "Second operand");
            properties.set("b", bProp);
            
            parameters.set("properties", properties);
            
            ArrayNode required = objectMapper.createArrayNode();
            required.add("operation").add("a").add("b");
            parameters.set("required", required);
            
            schema.set("parameters", parameters);
            return schema;
        }
        
        @Override
        public ToolResult execute(Map<String, Object> args) {
            try {
                String operation = (String) args.get("operation");
                Number a = (Number) args.get("a");
                Number b = (Number) args.get("b");
                
                if (operation == null || a == null || b == null) {
                    return ToolResult.error("Missing required parameters");
                }
                
                double result;
                switch (operation) {
                    case "add":
                        result = a.doubleValue() + b.doubleValue();
                        break;
                    case "subtract":
                        result = a.doubleValue() - b.doubleValue();
                        break;
                    case "multiply":
                        result = a.doubleValue() * b.doubleValue();
                        break;
                    case "divide":
                        if (b.doubleValue() == 0) {
                            return ToolResult.error("Division by zero");
                        }
                        result = a.doubleValue() / b.doubleValue();
                        break;
                    default:
                        return ToolResult.error("Unknown operation: " + operation);
                }
                
                // 정수인 경우 정수로 반환
                if (result == Math.floor(result)) {
                    return ToolResult.success(String.valueOf((int) result));
                } else {
                    return ToolResult.success(String.valueOf(result));
                }
            } catch (Exception e) {
                return ToolResult.error("Calculation error: " + e.getMessage());
            }
        }
        
        private final ObjectMapper objectMapper = new ObjectMapper();
    }
    
    /**
     * 테스트용 날씨 도구
     */
    static class WeatherTool extends AbstractTool {
        public WeatherTool() {
            super("get_weather", "Get the current weather for a location");
        }
        
        @Override
        public JsonNode getSchema() {
            ObjectNode schema = objectMapper.createObjectNode();
            schema.put("name", getName());
            schema.put("description", getDescription());
            
            ObjectNode parameters = objectMapper.createObjectNode();
            parameters.put("type", "object");
            
            ObjectNode properties = objectMapper.createObjectNode();
            
            ObjectNode locationProp = objectMapper.createObjectNode();
            locationProp.put("type", "string");
            locationProp.put("description", "The city and country, e.g. Seoul, Korea");
            properties.set("location", locationProp);
            
            ObjectNode unitProp = objectMapper.createObjectNode();
            unitProp.put("type", "string");
            ArrayNode unitEnum = objectMapper.createArrayNode();
            unitEnum.add("celsius").add("fahrenheit");
            unitProp.set("enum", unitEnum);
            unitProp.put("description", "Temperature unit");
            properties.set("unit", unitProp);
            
            parameters.set("properties", properties);
            
            ArrayNode required = objectMapper.createArrayNode();
            required.add("location");
            parameters.set("required", required);
            
            schema.set("parameters", parameters);
            return schema;
        }
        
        @Override
        public ToolResult execute(Map<String, Object> args) {
            String location = (String) args.get("location");
            if (location == null || location.trim().isEmpty()) {
                return ToolResult.error("Missing required parameter: location");
            }
            
            String unit = (String) args.getOrDefault("unit", "celsius");
            
            // 시뮬레이션된 날씨 데이터
            return ToolResult.success(String.format(
                "Weather in %s: 22°%s, Partly cloudy",
                location,
                unit.equals("celsius") ? "C" : "F"
            ));
        }
        
        private final ObjectMapper objectMapper = new ObjectMapper();
    }
    
    /**
     * 테스트용 검색 도구
     */
    static class SearchTool extends AbstractTool {
        public SearchTool() {
            super("search", "Search the web for information");
        }
        
        @Override
        public JsonNode getSchema() {
            ObjectNode schema = objectMapper.createObjectNode();
            schema.put("name", getName());
            schema.put("description", getDescription());
            
            ObjectNode parameters = objectMapper.createObjectNode();
            parameters.put("type", "object");
            
            ObjectNode properties = objectMapper.createObjectNode();
            
            ObjectNode queryProp = objectMapper.createObjectNode();
            queryProp.put("type", "string");
            queryProp.put("description", "The search query");
            properties.set("query", queryProp);
            
            parameters.set("properties", properties);
            
            ArrayNode required = objectMapper.createArrayNode();
            required.add("query");
            parameters.set("required", required);
            
            schema.set("parameters", parameters);
            return schema;
        }
        
        @Override
        public ToolResult execute(Map<String, Object> args) {
            String query = (String) args.get("query");
            if (query == null || query.trim().isEmpty()) {
                return ToolResult.error("Missing required parameter: query");
            }
            
            return ToolResult.success("Search results for: " + query);
        }
        
        private final ObjectMapper objectMapper = new ObjectMapper();
    }
}