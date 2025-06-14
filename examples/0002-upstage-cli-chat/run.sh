#!/bin/bash
# Upstage CLI Chat 실행 스크립트

# API 키 확인
if [ -z "$UPSTAGE_API_KEY" ]; then
    echo "❌ 오류: UPSTAGE_API_KEY 환경 변수가 설정되지 않았습니다."
    echo "다음 명령으로 API 키를 설정해주세요:"
    echo "export UPSTAGE_API_KEY='your-api-key'"
    exit 1
fi

# 실행
./gradlew run --console=plain -q