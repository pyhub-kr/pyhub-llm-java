# PyHub LLM Java

[![Maven Central](https://img.shields.io/maven-central/v/kr.pyhub/pyhub-llm)](https://central.sonatype.com/artifact/kr.pyhub/pyhub-llm)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-8%2B-orange)](https://www.oracle.com/java/)
[![Build Status](https://github.com/pyhub-kr/pyhub-llm-java/workflows/CI/badge.svg)](https://github.com/pyhub-kr/pyhub-llm-java/actions)

PyHub LLM Java는 여러 LLM 프로바이더(OpenAI, Anthropic, Upstage 등)에 대한 통합 인터페이스를 제공하는 Java 라이브러리입니다. 
Python의 [pyhub-llm](https://github.com/pyhub-kr/pyhub-llm) 라이브러리를 Java로 포팅하였습니다.

## ✨ 주요 특징

- **🔗 통합 인터페이스**: 모든 LLM 프로바이더를 동일한 API로 사용
- **⚡ 스트리밍 지원**: `Flux<StreamChunk>`를 통한 실시간 응답 처리
- **💾 캐싱 시스템**: 메모리 및 파일 기반 캐싱으로 성능 최적화
- **🛠️ 도구 호출**: 확장 가능한 함수/도구 호출 기능
- **💬 대화 관리**: 자동 컨텍스트 유지 및 대화 기록 관리
- **☕ Java 8+ 호환**: Java 8 이상에서 실행 가능
- **📦 공식 SDK 사용**: OpenAI와 Anthropic의 공식 Java SDK 사용
- **🔧 설정 유연성**: Builder 패턴과 환경변수 지원
- **🧪 포괄적 테스트**: 78개 이상의 테스트로 검증된 안정성

## 📦 설치

### Maven

```xml
<dependency>
    <groupId>kr.pyhub</groupId>
    <artifactId>pyhub-llm</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```gradle
dependencies {
    implementation 'kr.pyhub:pyhub-llm:0.1.0'
}
```

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("kr.pyhub:pyhub-llm:0.1.0")
}
```

### SBT

```scala
libraryDependencies += "kr.pyhub" % "pyhub-llm" % "0.1.0"
```

## 🚀 빠른 시작

### 기본 사용법

```java
import kr.pyhub.llm.LLM;
import kr.pyhub.llm.types.LLMReply;

// 환경변수에서 API 키를 읽어 LLM 인스턴스 생성
LLM llm = LLM.create("gpt-4o-mini");
LLMReply reply = llm.ask("안녕하세요!");
System.out.println(reply.getText());
```

### 설정 옵션 사용

```java
import kr.pyhub.llm.Config;

Config config = Config.builder()
    .apiKey("your-api-key")
    .temperature(0.7)
    .maxTokens(1000)
    .build();

LLM llm = LLM.create("gpt-4o-mini", config)
    .withSystemPrompt("You are a helpful assistant");
    
LLMReply reply = llm.ask("Tell me a joke");
System.out.println(reply.getText());
```

### 스트리밍 응답

```java
import reactor.core.publisher.Flux;
import kr.pyhub.llm.types.StreamChunk;

LLM llm = LLM.create("gpt-4o-mini");
Flux<StreamChunk> stream = llm.askStream("Write a short story");

stream.subscribe(chunk -> {
    if (chunk.getContent() != null) {
        System.out.print(chunk.getContent());
    }
    if (chunk.isFinished()) {
        System.out.println("\n[완료: " + chunk.getFinishReason() + "]");
    }
});
```

### 대화 관리 (자동 히스토리)

```java
// 대화 모드 활성화
LLM llm = LLM.create("gpt-4o-mini")
    .enableConversation("You are a helpful assistant");

// 자동으로 대화 기록이 유지됩니다
LLMReply reply1 = llm.chat("My name is John");
LLMReply reply2 = llm.chat("What's my name?"); // "John"이라고 기억함

// 대화 기록 초기화
llm.clearConversation();
```

### 캐싱 사용

```java
import kr.pyhub.llm.cache.MemoryCache;

MemoryCache cache = new MemoryCache()
    .withMaxSize(1000)
    .withTtlMinutes(60);

LLM llm = LLM.create("gpt-4o-mini")
    .withCache(cache);

// 동일한 질문은 캐시에서 즉시 응답
LLMReply reply1 = llm.ask("What is 2+2?"); // API 호출
LLMReply reply2 = llm.ask("What is 2+2?"); // 캐시에서 응답
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

## 🤖 지원하는 모델

### OpenAI ✅
- `gpt-4`, `gpt-4-turbo`, `gpt-4o`, `gpt-4o-mini`
- `gpt-3.5-turbo`, `gpt-3.5-turbo-16k`

### Anthropic (Claude) ✅
- `claude-3-5-sonnet-20240620`
- `claude-3-opus-20240229`, `claude-3-sonnet-20240229`, `claude-3-haiku-20240307`
- `claude-2.1`, `claude-2.0`, `claude-instant-1.2`

### Upstage (Solar) ✅
- `solar-1-mini-chat` - 한국어 최적화 모델
- `solar-1-mini-chat-ja` - 일본어 지원 모델

### Google (Gemini) 🚧
- `gemini-pro`, `gemini-pro-vision`
- `gemini-1.5-pro`, `gemini-1.5-flash`

### Ollama (로컬) 🚧
- `ollama:llama2`, `ollama:mistral`, `ollama:codellama`

**범례:**
- ✅ 완전 구현됨
- 🚧 개발 중 (기본 구조만 구현)

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

## 🔧 고급 기능

### 도구/함수 호출

```java
import kr.pyhub.llm.tools.Tool;
import kr.pyhub.llm.tools.ToolRegistry;

// 커스텀 도구 정의
Tool calculator = Tool.builder()
    .name("calculator")
    .description("간단한 수학 계산을 수행합니다")
    .parameter("expression", "계산할 수식", true)
    .execute(args -> {
        String expr = (String) args.get("expression");
        // 계산 로직 구현
        return ToolResult.success("결과: " + result);
    })
    .build();

LLM llm = LLM.create("gpt-4o-mini")
    .withTools(calculator);
```

### 파일 캐싱

```java
import kr.pyhub.llm.cache.FileCache;

FileCache fileCache = new FileCache("./cache")
    .withTtlHours(24);

LLM llm = LLM.create("gpt-4o-mini")
    .withCache(fileCache);
```

## 📊 성능 최적화

- **메모리 캐싱**: Caffeine 기반 고성능 캐시
- **파일 캐싱**: JSON 직렬화를 통한 영구 캐시
- **연결 풀링**: OkHttp 연결 재사용
- **스트리밍**: 대용량 응답의 실시간 처리
- **비동기 처리**: CompletableFuture 지원

## 🔗 Maven Central

이 라이브러리는 Maven Central에 배포되어 있습니다:
- **Group ID**: `kr.pyhub`
- **Artifact ID**: `pyhub-llm`
- **Latest Version**: `0.1.0`

## 📚 예시 프로젝트

- [examples/0001-java-cli-chat](examples/0001-java-cli-chat) - OpenAI 기반 CLI 챗봇
- [examples/0002-upstage-cli-chat](examples/0002-upstage-cli-chat) - Upstage Solar 기반 한국어 챗봇

## 📄 라이선스

이 프로젝트는 [Apache 2.0 라이선스](LICENSE) 하에 배포됩니다.

## 🤝 기여하기

기여는 언제나 환영합니다! 다음 방법으로 참여하실 수 있습니다:

1. 🐛 [이슈 신고](https://github.com/pyhub-kr/pyhub-llm-java/issues)
2. 💡 [기능 제안](https://github.com/pyhub-kr/pyhub-llm-java/issues)
3. 🔧 [Pull Request](https://github.com/pyhub-kr/pyhub-llm-java/pulls)

## 📞 지원

- 📖 [문서](https://github.com/pyhub-kr/pyhub-llm-java/wiki)
- 🐞 [이슈 트래커](https://github.com/pyhub-kr/pyhub-llm-java/issues)
- 💬 [토론](https://github.com/pyhub-kr/pyhub-llm-java/discussions)

## 🔗 관련 프로젝트

- [pyhub-llm (Python)](https://github.com/pyhub-kr/pyhub-llm) - 원본 Python 버전
- [PyHub](https://pyhub.kr) - 한국의 Python 커뮤니티

---

<div align="center">
Made with ❤️ by the PyHub Team
</div>