# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2024-06-14

### Added
- Initial release of pyhub-llm-java
- Unified interface for multiple LLM providers
- OpenAI integration with official SDK
- Anthropic integration with official SDK  
- Upstage Solar LLM integration with HTTP client
- Streaming response support with `Flux<StreamChunk>`
- Caching system with memory and file-based implementations
- Tool/function calling infrastructure
- Automatic conversation management with history tracking
- Comprehensive configuration system with builder pattern
- Async support with `CompletableFuture`
- Java 8+ compatibility
- Comprehensive test suite with 78+ tests
- CLI examples for interactive chat applications
- Maven Central publishing support

### Features
#### Core LLM Providers
- **OpenAI**: GPT-4, GPT-4 Turbo, GPT-3.5 Turbo, and all OpenAI models
- **Anthropic**: Claude 3.5 Sonnet, Claude 3 Opus, Claude 3 Haiku
- **Upstage**: Solar-1-mini-chat (Korean-optimized)

#### Streaming Support
- Real-time response streaming with Project Reactor
- `Flux<StreamChunk>` for reactive programming
- Fallback implementation for providers without native streaming

#### Caching System
- Memory-based caching with Caffeine
- File-based persistent caching with JSON serialization
- Configurable TTL and size limits
- Automatic cache key generation

#### Tools & Function Calling
- Extensible tool registry system
- JSON Schema support for tool definitions
- Tool execution with error handling
- Built-in tool implementations

#### Conversation Management
- Automatic message history tracking
- Configurable message and token limits
- System prompt management
- Conversation state persistence

#### Configuration
- Builder pattern for fluent configuration
- Environment variable support
- Per-provider configuration options
- Temperature, max tokens, and other parameters

### Examples
- **examples/0001-java-cli-chat**: OpenAI-based CLI chat application
- **examples/0002-upstage-cli-chat**: Upstage Solar CLI chat with Korean support

### Dependencies
- Java 8+ compatibility
- OpenAI Java SDK 2.7.0
- Anthropic Java SDK 2.1.0  
- OkHttp 4.12.0 for HTTP clients
- Jackson 2.15.3 for JSON processing
- Project Reactor 3.4.33 for streaming
- Caffeine 2.9.3 for caching
- SLF4J 1.7.36 for logging
- Lombok 1.18.30 for code generation

### Testing
- JUnit 5 test framework
- Mockito for mocking
- 78+ comprehensive tests
- Integration tests with real API calls
- Mock implementations for testing

[Unreleased]: https://github.com/pyhub-kr/pyhub-llm-java/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/pyhub-kr/pyhub-llm-java/releases/tag/v0.1.0