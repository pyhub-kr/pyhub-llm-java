#!/bin/bash
# Java CLI Chat 실행 스크립트
# Gradle 진행 표시줄 없이 깔끔한 대화형 인터페이스 제공

# 스크립트 위치 기준으로 프로젝트 디렉토리 찾기
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Gradle 실행 (진행 표시줄 비활성화)
./gradlew run --console=plain -q