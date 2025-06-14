# TODOs.md - pyhub-llm-java 프로젝트 진행 계획

## Phase 1: 핵심 기능 구현 (1-2주)

### 1. 프로젝트 초기 설정
- [ ] Gradle 프로젝트 초기화
  - [ ] `build.gradle` 파일 생성
  - [ ] `settings.gradle` 파일 생성
  - [ ] `gradle.properties` 파일 생성
  - [ ] Gradle Wrapper 설정
- [ ] 기본 디렉토리 구조 생성
  - [ ] `src/main/java/kr/pyhub/llm` 디렉토리 생성
  - [ ] `src/test/java/kr/pyhub/llm` 디렉토리 생성
  - [ ] `src/main/resources` 디렉토리 생성
  - [ ] `src/test/resources` 디렉토리 생성
- [ ] 의존성 설정
  - [ ] OkHttp 4.x 추가
  - [ ] Jackson 2.x 추가
  - [ ] SLF4J + Logback 추가
  - [ ] JUnit 5 추가
  - [ ] Mockito 추가
  - [ ] Lombok 추가 (선택사항)

### 2. 코어 인터페이스 구현
- [ ] 기본 패키지 구조 생성
  - [ ] `base` 패키지 생성
  - [ ] `types` 패키지 생성
  - [ ] `exceptions` 패키지 생성
- [ ] 핵심 클래스 구현
  - [ ] `BaseLLM` 추상 클래스 구현
  - [ ] `LLMReply` 응답 모델 구현
  - [ ] `Message` 타입 구현
  - [ ] `LLMException` 및 하위 예외 클래스 구현
- [ ] `LLM` 팩토리 클래스 구현
  - [ ] `create()` 메서드 구현
  - [ ] 프로바이더 레지스트리 구현

### 3. OpenAI 프로바이더 구현
- [ ] `providers` 패키지 생성
- [ ] `OpenAILLM` 클래스 구현
  - [ ] 환경변수에서 API 키 읽기
  - [ ] HTTP 클라이언트 설정
  - [ ] `ask()` 메서드 구현 (동기)
  - [ ] `askAsync()` 메서드 구현 (비동기)
  - [ ] 에러 처리 구현
- [ ] OpenAI API 모델 클래스 구현
  - [ ] Request/Response DTO
  - [ ] JSON 직렬화/역직렬화 설정
- [ ] 기본 테스트 작성
  - [ ] 단위 테스트 (Mockito 사용)
  - [ ] 통합 테스트 (WireMock 사용)

### 4. 설정 시스템 구현
- [ ] `Config` 클래스 구현
  - [ ] API 키 관리
  - [ ] 온도, max_tokens 등 파라미터 관리
  - [ ] 빌더 패턴 구현
- [ ] 환경변수 로더 구현
- [ ] 설정 검증 로직 구현

## Phase 2: 프로바이더 확장 (2-3주)

### 1. 추가 프로바이더 구현
- [ ] `AnthropicLLM` 구현
  - [ ] Claude API 통합
  - [ ] 메시지 포맷 변환
- [ ] `GoogleLLM` 구현
  - [ ] Gemini API 통합
  - [ ] 인증 처리
- [ ] `OllamaLLM` 구현
  - [ ] 로컬 Ollama 서버 연동
  - [ ] 모델 리스트 조회
- [ ] `UpstageLLM` 구현
  - [ ] Upstage API 통합

### 2. 스트리밍 지원
- [ ] Reactor Core 의존성 추가
- [ ] 스트리밍 인터페이스 정의
  - [ ] `askStream()` 메서드 추가
  - [ ] `Flux<String>` 반환 타입
- [ ] 각 프로바이더별 스트리밍 구현
  - [ ] OpenAI SSE 파싱
  - [ ] Anthropic SSE 파싱
  - [ ] 기타 프로바이더 스트리밍

### 3. 대화 히스토리 관리
- [ ] `Conversation` 클래스 구현
- [ ] 메시지 히스토리 추적
- [ ] 컨텍스트 길이 관리
- [ ] 토큰 카운팅 (tiktoken 포팅 또는 대안)

## Phase 3: 고급 기능 (3-4주)

### 1. 캐싱 시스템
- [ ] `cache` 패키지 생성
- [ ] `Cache` 인터페이스 정의
- [ ] `MemoryCache` 구현 (Caffeine 사용)
  - [ ] TTL 지원
  - [ ] 크기 제한
- [ ] `FileCache` 구현
  - [ ] 디스크 저장소
  - [ ] 직렬화/역직렬화
- [ ] 캐시 키 생성 로직
- [ ] 캐시 통합 테스트

### 2. 도구/함수 호출
- [ ] `tools` 패키지 생성
- [ ] `Tool` 인터페이스 정의
- [ ] `ToolCall` 및 `ToolResult` 모델
- [ ] OpenAI function calling 구현
- [ ] Anthropic tool use 구현
- [ ] 도구 실행 엔진 구현

### 3. 파일 처리
- [ ] 이미지 처리
  - [ ] Base64 인코딩
  - [ ] 이미지 유효성 검사
- [ ] PDF 처리 (Apache PDFBox)
  - [ ] 텍스트 추출
  - [ ] 페이지별 처리
- [ ] 파일 크기 제한 처리

## Phase 4: 부가 기능 (2-3주)

### 1. 구조화된 출력
- [ ] Immutables 의존성 추가
- [ ] JSON Schema 생성기 구현
- [ ] 응답 파싱 및 검증
- [ ] 타입 안전 API 설계

### 2. 템플릿 시스템
- [ ] Mustache 의존성 추가
- [ ] 템플릿 로더 구현
- [ ] 프롬프트 템플릿 관리
- [ ] 변수 바인딩 시스템

### 3. CLI 도구
- [ ] `cli` 패키지 생성
- [ ] Picocli 의존성 추가
- [ ] 명령줄 인터페이스 구현
  - [ ] 프로바이더 선택
  - [ ] 대화형 모드
  - [ ] 파일 입출력
- [ ] Shell 스크립트 래퍼

## Phase 5: 에이전트 및 MCP (3-4주)

### 1. ReactAgent 구현
- [ ] `agents` 패키지 생성
- [ ] `Agent` 인터페이스 정의
- [ ] `ReactAgent` 구현
  - [ ] 사고 과정 추적
  - [ ] 도구 실행 루프
  - [ ] 결과 평가
- [ ] 에이전트 테스트 시나리오

### 2. MCP 지원
- [ ] `mcp` 패키지 생성
- [ ] MCP 프로토콜 구현
- [ ] 전송 계층 구현
  - [ ] stdio 전송
  - [ ] HTTP 전송
- [ ] MCP 서버 디스커버리
- [ ] 리소스 및 도구 통합

## 마무리 작업

### 1. 문서화
- [ ] README.md 작성
- [ ] JavaDoc 완성
- [ ] 예제 코드 작성
- [ ] API 가이드 작성

### 2. 품질 보증
- [ ] 테스트 커버리지 95% 이상 달성
- [ ] 정적 분석 도구 설정 (SpotBugs, PMD)
- [ ] CI/CD 파이프라인 구성

### 3. 배포 준비
- [ ] 버전 관리 정책 수립
- [ ] Maven Central 배포 준비
- [ ] 릴리스 노트 작성
- [ ] 라이선스 파일 추가

## 주의사항

1. **Java 8 호환성 유지**
   - var 키워드 사용 금지
   - Stream API 제한적 사용
   - Optional 신중히 사용

2. **테스트 우선 개발**
   - 각 기능 구현 전 테스트 작성
   - 실제 API 호출은 통합 테스트에서만

3. **문서화 필수**
   - 모든 public API에 JavaDoc
   - 복잡한 로직에 주석 추가

4. **Python API와의 일관성**
   - 메서드명 및 파라미터 일치
   - 동작 방식 유사하게 유지