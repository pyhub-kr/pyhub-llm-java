# pyhub-llm-java

pyhub-llm-java는 Python의 pyhub-llm 라이브러리를 Java로 포팅한 프로젝트입니다. 여러 LLM 프로바이더(OpenAI, Anthropic, Google, Ollama, Upstage)에 대한 통합 인터페이스를 제공합니다.

## 주요 특징

- **통합 인터페이스**: 모든 LLM 프로바이더를 동일한 API로 사용
- **간결한 API**: 복잡한 내부 구현에도 불구하고 사용자는 간단하게 사용
- **Java 8+ 호환**: Java 8 이상에서 실행 가능
- **공식 SDK 사용**: OpenAI와 Anthropic의 공식 Java SDK 사용
- **확장 가능**: 새로운 프로바이더 추가 용이

## 설치

### Gradle

```gradle
dependencies {
    implementation 'kr.pyhub:pyhub-llm-java:0.1.0-SNAPSHOT'
}
```

### Maven

```xml
<dependency>
    <groupId>kr.pyhub</groupId>
    <artifactId>pyhub-llm-java</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## 빠른 시작

### 기본 사용법

```java
import kr.pyhub.llm.LLM;
import kr.pyhub.llm.types.LLMReply;

// 환경변수에서 API 키를 읽어 LLM 인스턴스 생성
LLM llm = LLM.create("gpt-4o-mini");
LLMReply reply = llm.ask("안녕하세요!");
System.out.println(reply.getText());
```

### API 키 직접 전달

```java
LLM llm = LLM.create("gpt-4o-mini", "your-api-key");
LLMReply reply = llm.ask("Hello, world!");
System.out.println(reply.getText());
```

### 설정 옵션 사용

```java
import kr.pyhub.llm.Config;

Config config = Config.builder()
    .apiKey("your-api-key")
    .temperature(0.7)
    .maxTokens(1000)
    .systemPrompt("You are a helpful assistant")
    .build();

LLM llm = LLM.create("gpt-4o-mini", config);
LLMReply reply = llm.ask("Tell me a joke");
System.out.println(reply.getText());
```

### 메서드 체이닝

```java
String answer = LLM.create("gpt-4o-mini")
    .withSystemPrompt("You are a helpful coding assistant")
    .withTemperature(0.3)
    .withMaxTokens(500)
    .ask("Write a hello world program in Java")
    .getText();
```

### 비동기 호출

```java
import java.util.concurrent.CompletableFuture;

LLM llm = LLM.create("gpt-4o-mini");
CompletableFuture<LLMReply> future = llm.askAsync("What is the meaning of life?");

future.thenAccept(reply -> {
    System.out.println(reply.getText());
});
```

## 지원하는 모델

### OpenAI
- `gpt-4`, `gpt-4-turbo`, `gpt-4o`, `gpt-4o-mini`
- `gpt-3.5-turbo`, `gpt-3.5-turbo-16k`

### Anthropic (Claude)
- `claude-3-opus`, `claude-3-sonnet`, `claude-3-haiku`
- `claude-2.1`, `claude-2`, `claude-instant`

### Google
- `gemini-pro`, `gemini-pro-vision`, `gemini-1.5-pro`, `gemini-1.5-flash`

### Ollama (로컬)
- `ollama:llama2`, `ollama:mistral`, `ollama:codellama`

### Upstage
- `upstage:solar-1-mini`, `upstage:solar-1-small`

## 환경변수

각 프로바이더에 필요한 환경변수를 설정하세요:

```bash
# OpenAI
export OPENAI_API_KEY="your-openai-api-key"
export OPENAI_ORG_ID="your-org-id"           # 선택사항
export OPENAI_PROJECT_ID="your-project-id"   # 선택사항

# Anthropic
export ANTHROPIC_API_KEY="your-anthropic-api-key"

# Google
export GOOGLE_API_KEY="your-google-api-key"

# Upstage
export UPSTAGE_API_KEY="your-upstage-api-key"

# Ollama (로컬 서버)
export OLLAMA_HOST="http://localhost:11434"  # 기본값
```

## 커스텀 프로바이더

새로운 프로바이더를 등록할 수 있습니다:

```java
LLM.registerProvider("custom", (model, config) -> {
    return new CustomLLM(model, config);
});

// 사용
LLM customLlm = LLM.create("custom:my-model");
```

## 개발 환경 설정

### 요구사항

- Java 8 이상
- Gradle 7.x

### 빌드

```bash
./gradlew build
```

### 테스트

```bash
./gradlew test
```

### JAR 생성

```bash
./gradlew shadowJar
```

## 현재 구현 상태

- ✅ **OpenAI**: 완전 구현 (공식 SDK 사용)
- ⚠️ **Anthropic**: 기본 구조 완성 (구현 중)
- 🚧 **Google**: 스텁만 구현
- 🚧 **Ollama**: 스텁만 구현  
- 🚧 **Upstage**: 스텁만 구현

## 라이선스

이 프로젝트는 Apache 2.0 라이선스 하에 배포됩니다.

## 기여하기

이슈나 풀 리퀘스트는 언제든 환영합니다!

## 관련 프로젝트

- [pyhub-llm (Python)](https://github.com/pyhub-kr/pyhub-llm) - 원본 Python 버전