# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is **pyhub-llm-java**, a Java port of the Python pyhub-llm library that provides a unified interface for multiple LLM providers (OpenAI, Anthropic, Google, Ollama, Upstage).

**Current Status**: Project is in planning phase with only PRD.md present. No code has been implemented yet.

## Build and Development Commands

Once the project is set up with Gradle, use these commands:

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "kr.pyhub.llm.providers.OpenAILLMTest"

# Run tests with coverage
./gradlew test jacocoTestReport

# Check code style
./gradlew checkstyleMain checkstyleTest

# Clean build artifacts
./gradlew clean

# Create JAR file
./gradlew jar

# Run the CLI tool (once implemented)
./gradlew run --args="--provider gpt-4o-mini --prompt 'Hello'"
```

## Architecture Overview

### Package Structure
- `kr.pyhub.llm` - Root package
  - `LLM.java` - Main factory class for creating LLM instances
  - `base/` - Core abstractions (BaseLLM, LLMReply)
  - `providers/` - LLM provider implementations (OpenAI, Anthropic, etc.)
  - `cache/` - Caching system (Memory and File-based)
  - `tools/` - Tool/function calling support
  - `agents/` - Agent framework (ReactAgent)
  - `mcp/` - MCP (Model Context Protocol) client
  - `types/` - Data types (Message, etc.)
  - `exceptions/` - Custom exceptions
  - `cli/` - Command-line interface

### Key Design Principles
1. **Simple API despite complex internals** - Users should be able to use LLMs with minimal code
2. **Unified interface** - All providers use the same API
3. **Builder pattern** - Fluent API with method chaining
4. **Immutable objects** - Using Immutables library for value objects
5. **Async-first** - Support for CompletableFuture and Reactor Core streaming

### Technology Stack
- **Java 8+** for compatibility
- **Gradle 7.x** for build management
- **OkHttp** for HTTP requests
- **Jackson** for JSON processing
- **Reactor Core** for streaming
- **Immutables** for value objects
- **SLF4J + Logback** for logging
- **JUnit 5 + Mockito** for testing

## Implementation Phases

According to PRD.md, the project has 5 implementation phases:
1. **Phase 1**: Core functionality with OpenAI provider
2. **Phase 2**: Additional providers and streaming support
3. **Phase 3**: Caching, tools, and file processing
4. **Phase 4**: Structured output, templates, and CLI
5. **Phase 5**: Agent framework and MCP support

## API Examples

Basic usage pattern:
```java
LLM llm = LLM.create("gpt-4o-mini");
LLMReply reply = llm.ask("Hello!");
```

With configuration:
```java
LLM llm = LLM.create("gpt-4o-mini")
    .withSystemPrompt("You are a helpful assistant")
    .withTemperature(0.7)
    .withCache(new MemoryCache());
```