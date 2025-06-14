# Java CLI Chat Example

이 예시는 pyhub-llm-java 라이브러리를 사용하여 OpenAI GPT 모델과 대화할 수 있는 간단한 명령행 채팅 인터페이스를 제공합니다.

## 기능

- OpenAI GPT-4o-mini 모델과의 실시간 대화
- 스트리밍 효과로 응답 출력
- 대화 기록 유지
- 한국어 시스템 프롬프트 설정
- 환경변수 또는 명령행 인수로 API 키 설정

## 사용법

### 1. 환경변수 설정

```bash
export OPENAI_API_KEY=your_openai_api_key_here
```

### 2. 실행

#### Gradle로 직접 실행
```bash
# 메인 프로젝트 디렉토리에서
cd examples/0001-java-cli-chat
./gradlew run --console=plain
```

> 💡 **팁**: `--console=plain` 옵션을 사용하면 Gradle 진행 표시줄이 비활성화되어 깔끔한 대화형 인터페이스를 사용할 수 있습니다.

#### 편리한 실행 스크립트 사용
```bash
# 더 간단하게 실행하기
./run.sh
```

#### API 키를 매개변수로 전달
```bash
./gradlew runWithApiKey -Papi.key=your_openai_api_key_here
```

#### JAR 파일 생성 후 실행
```bash
# JAR 파일 생성
./gradlew jar

# JAR 파일 실행
java -jar build/libs/java-cli-chat-1.0.0.jar --api-key your_openai_api_key_here
```

### 3. 채팅 사용

- 프로그램이 시작되면 `You:` 프롬프트가 표시됩니다
- 원하는 질문이나 대화를 입력하세요
- AI가 응답을 스트리밍 효과로 출력합니다
- `/quit`을 입력하면 프로그램이 종료됩니다

## 예시 대화

```
=== Java CLI Chat ===
OpenAI gpt-4o-mini 모델과 채팅을 시작합니다.
'/quit' 입력시 종료됩니다.
========================

You: 안녕하세요!
AI: 안녕하세요! 저는 친근하고 도움이 되는 AI 어시스턴트입니다. 무엇을 도와드릴까요?

You: Java로 Hello World를 출력하는 코드를 알려주세요
AI: Java로 Hello World를 출력하는 간단한 코드는 다음과 같습니다:

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
```

이 코드를 HelloWorld.java 파일에 저장한 후, 다음 명령어로 컴파일하고 실행할 수 있습니다:
- 컴파일: `javac HelloWorld.java`
- 실행: `java HelloWorld`

You: /quit
채팅을 종료합니다. 안녕히 가세요!
```

## 설정

### 시스템 프롬프트 변경

`JavaCliChat.java` 파일의 생성자에서 시스템 프롬프트를 변경할 수 있습니다:

```java
this.llm.withSystemPrompt("당신은 친근하고 도움이 되는 AI 어시스턴트입니다. 한국어로 답변해주세요.");
```

### 모델 변경

`DEFAULT_MODEL` 상수를 변경하여 다른 OpenAI 모델을 사용할 수 있습니다:

```java
private static final String DEFAULT_MODEL = "gpt-4o"; // 또는 다른 모델
```

### 스트리밍 효과 조정

`simulateStreamingOutput` 메서드의 `Thread.sleep()` 값을 조정하여 출력 속도를 변경할 수 있습니다:

```java
Thread.sleep(20); // 밀리초 단위, 더 작은 값 = 더 빠른 출력
```

## 의존성

이 예시는 다음 라이브러리들을 사용합니다:

- pyhub-llm-java (메인 라이브러리)
- OpenAI Java SDK
- Logback (로깅)

## 빌드 정보

- Java 8+ 호환
- Gradle 빌드 시스템
- Fat JAR 생성 지원 (모든 의존성 포함)

## 문제 해결

### API 키 관련 오류
- 올바른 OpenAI API 키가 설정되어 있는지 확인하세요
- API 키에 충분한 크레딧이 있는지 확인하세요

### 컴파일 오류
- Java 8 이상이 설치되어 있는지 확인하세요
- 메인 프로젝트의 라이브러리가 정상적으로 빌드되었는지 확인하세요

### 실행 오류
- 모든 의존성이 클래스패스에 포함되어 있는지 확인하세요
- JAR 파일을 사용하는 경우 Fat JAR이 정상적으로 생성되었는지 확인하세요