# PRD_JAVA.md - pyhub-llm Java 포팅 프로젝트

## 1. 프로젝트 개요

### 1.1 목적
Python으로 개발된 pyhub-llm 라이브러리를 Java로 포팅하여, Java 개발자들이 통합 LLM 인터페이스를 사용할 수 있도록 한다.

### 1.2 핵심 가치
- **간결한 API**: 복잡한 내부 구현에도 불구하고 사용자는 간단하게 사용
- **통합 인터페이스**: 여러 LLM 프로바이더를 동일한 방식으로 사용
- **확장성**: 새로운 프로바이더 추가가 용이
- **Python 버전과의 일관성**: API 사용 경험의 일관성 유지

## 2. 기술 스택

### 2.1 기본 환경
- **Java 버전**: Java 8+ (초기 버전은 Java 8 지원)
- **빌드 도구**: Gradle 7.x
- **패키징**: 단일 JAR 파일

### 2.2 주요 라이브러리
- **HTTP 클라이언트**: OkHttp 4.x (Java 8 지원, 작고 강력)
- **JSON 처리**: Jackson 2.x (가장 안정적이고 널리 사용됨)
- **비동기 처리**: CompletableFuture (Java 8 내장) + Reactor Core (스트리밍용)
- **불변 객체**: Immutables 2.x
- **템플릿 엔진**: Mustache.java
- **설정 파일**: SnakeYAML
- **로깅**: SLF4J + Logback (업계 표준)
- **테스트**: JUnit 5 + AutoParams + Mockito
- **Validation**: Hibernate Validator 6.x

## 3. 프로젝트 구조

```
pyhub-llm-java/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── kr/
│   │   │       └── pyhub/
│   │   │           └── llm/
│   │   │               ├── LLM.java                 # 팩토리 클래스
│   │   │               ├── base/
│   │   │               │   ├── BaseLLM.java         # 추상 기본 클래스
│   │   │               │   └── LLMReply.java        # 응답 모델
│   │   │               ├── providers/
│   │   │               │   ├── OpenAILLM.java
│   │   │               │   ├── AnthropicLLM.java
│   │   │               │   ├── GoogleLLM.java
│   │   │               │   ├── OllamaLLM.java
│   │   │               │   └── UpstageLLM.java
│   │   │               ├── cache/
│   │   │               │   ├── Cache.java           # 캐시 인터페이스
│   │   │               │   ├── MemoryCache.java
│   │   │               │   └── FileCache.java
│   │   │               ├── tools/
│   │   │               │   └── Tool.java
│   │   │               ├── agents/
│   │   │               │   └── ReactAgent.java
│   │   │               ├── mcp/
│   │   │               │   └── MCPClient.java
│   │   │               ├── types/
│   │   │               │   └── Message.java
│   │   │               ├── exceptions/
│   │   │               │   └── LLMException.java
│   │   │               └── cli/
│   │   │                   └── CLI.java
│   │   └── resources/
│   │       ├── templates/
│   │       │   ├── system_prompt.mustache
│   │       │   └── user_prompt.mustache
│   │       └── logback.xml
│   └── test/
│       ├── java/
│       └── resources/
```

## 4. 구현 계획

### Phase 1: 핵심 기능 (1-2주)
1. **기본 구조 설정**
   - Gradle 프로젝트 설정
   - 기본 패키지 구조 생성
   - 의존성 설정

2. **코어 인터페이스**
   - `BaseLLM` 추상 클래스
   - `LLMReply` 응답 모델
   - `LLM` 팩토리 클래스
   - 기본 예외 클래스

3. **첫 번째 프로바이더 (OpenAI)**
   - 기본 `ask()` 메서드 구현
   - 동기/비동기 지원
   - 환경변수 및 설정 관리

### Phase 2: 프로바이더 확장 (2-3주)
1. **나머지 프로바이더 구현**
   - Anthropic
   - Google
   - Ollama
   - Upstage

2. **스트리밍 지원**
   - Reactor Core를 활용한 스트리밍 API
   - 각 프로바이더별 스트리밍 구현

3. **대화 히스토리 관리**
   - 메시지 히스토리 추적
   - 컨텍스트 관리

### Phase 3: 고급 기능 (3-4주)
1. **캐싱 시스템**
   - Caffeine 기반 메모리 캐시
   - 파일 기반 캐시
   - TTL 지원

2. **도구/함수 호출**
   - Tool 인터페이스
   - 프로바이더별 구현

3. **파일 처리**
   - 이미지 처리
   - PDF 처리 (Apache PDFBox 활용)

### Phase 4: 부가 기능 (2-3주)
1. **구조화된 출력**
   - Immutables 모델
   - JSON Schema 검증

2. **템플릿 시스템**
   - Mustache 템플릿 통합
   - 프롬프트 관리

3. **CLI 도구**
   - Picocli 기반 CLI
   - 대화형 모드

### Phase 5: 에이전트 및 MCP (3-4주)
1. **ReactAgent 구현**
   - 에이전트 프레임워크
   - 도구 통합

2. **MCP 지원**
   - MCP 클라이언트
   - 다양한 전송 방식 지원

## 5. API 설계 예시

### 5.1 기본 사용법
```java
// 간단한 사용
LLM llm = LLM.create("gpt-4o-mini");
LLMReply reply = llm.ask("안녕하세요!");
System.out.println(reply.getText());

// API 키 직접 전달
LLM llm = LLM.create("gpt-4o-mini", 
    new Config().apiKey("your-key"));

// 메서드 체이닝
String answer = LLM.create("gpt-4o-mini")
    .withSystemPrompt("You are a helpful assistant")
    .withTemperature(0.7)
    .ask("질문")
    .getText();
```

### 5.2 스트리밍
```java
// Reactor 기반 스트리밍
Flux<String> stream = llm.askStream("긴 답변을 주세요");
stream.subscribe(chunk -> System.out.print(chunk));

// CompletableFuture 기반
CompletableFuture<LLMReply> future = llm.askAsync("질문");
future.thenAccept(reply -> System.out.println(reply.getText()));
```

### 5.3 고급 기능
```java
// 캐싱
Cache cache = new MemoryCache(Duration.ofHours(1));
LLM llm = LLM.create("gpt-4o-mini").withCache(cache);

// 도구 사용
Tool weatherTool = Tool.builder()
    .name("get_weather")
    .description("Get weather information")
    .function(city -> "Sunny in " + city)
    .build();

LLMReply reply = llm
    .withTools(weatherTool)
    .ask("What's the weather in Seoul?");

// 구조화된 출력
@Value.Immutable
public interface User {
    String name();
    int age();
    String email();
}

ImmutableUser user = llm
    .ask("Create a user: John, 30, john@example.com")
    .as(ImmutableUser.class);
```

## 6. 예외 처리 전략

Runtime Exception 기반으로 구현:
- `LLMException`: 기본 예외 클래스
- `ProviderException`: 프로바이더별 예외
- `ValidationException`: 입력 검증 예외
- `ConfigurationException`: 설정 관련 예외

## 7. 테스트 전략

1. **단위 테스트**
   - JUnit 5 + AutoParams
   - Mockito를 사용한 외부 의존성 모킹

2. **통합 테스트**
   - 실제 API 호출 (환경변수로 제어)
   - WireMock을 사용한 API 모킹

3. **성능 테스트**
   - JMH를 사용한 벤치마크

## 8. 문서화 계획

1. **JavaDoc**: 모든 public API에 상세한 문서화
2. **README.md**: 빠른 시작 가이드
3. **예제 코드**: examples/ 디렉토리에 다양한 사용 예제
4. **Wiki**: 상세한 사용법 및 고급 기능 설명

## 9. 릴리스 계획

### v0.1.0 (MVP)
- 기본 LLM 인터페이스
- OpenAI 프로바이더
- 동기/비동기 ask() 메서드

### v0.2.0
- 모든 프로바이더 지원
- 스트리밍 지원
- 대화 히스토리

### v0.3.0
- 캐싱 시스템
- 도구/함수 호출
- 파일 처리

### v1.0.0
- 모든 기능 구현 완료
- 안정화
- Maven Central 배포

## 10. 성공 지표

1. **사용성**: Python 버전과 유사한 간결한 API
2. **성능**: 효율적인 메모리 사용 및 응답 시간
3. **안정성**: 95% 이상의 테스트 커버리지
4. **확장성**: 새 프로바이더 추가 용이성
5. **문서화**: 명확하고 충분한 문서

## 11. 위험 요소 및 대응

1. **Java 8 제약사항**
   - 일부 최신 기능 사용 불가 → 대체 구현 필요
   - var 키워드 없음 → 명시적 타입 선언

2. **스트리밍 복잡성**
   - 프로바이더별 상이한 구현 → 추상화 계층 필요

3. **동적 타입 부재**
   - Python의 유연성 재현 어려움 → 제네릭과 오버로딩 활용

4. **의존성 충돌**
   - 다양한 라이브러리 사용 → 신중한 버전 관리 필요