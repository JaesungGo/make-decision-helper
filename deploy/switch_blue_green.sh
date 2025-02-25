

#!/bin/bash

# 네트워크 생성 (없는 경우)
docker network create app-network || true

BLUE_RUNNING=$(docker ps -q -f "name=make-decision-helper-backend-blue")

if [ -n "$BLUE_RUNNING" ]; then
    echo "🟢 Blue (현재 운영 중) → Green으로 전환 시작..."

    # Green 버전 실행
    docker-compose -f docker-compose.yml -f docker-compose.green.yml up -d --build

    # 헬스체크
    MAX_ATTEMPTS=6
    WAIT_TIME=5

    for i in $(seq 1 $MAX_ATTEMPTS); do
        if curl -sf http://localhost:8081/api/health > /dev/null; then
            echo "✅ Green 서비스 헬스체크 성공!"
            echo "🛑 기존 Blue 서비스 종료..."
            docker-compose -f docker-compose.blue.yml down
            echo "✅ Green 서비스가 운영 상태로 변경됨!"
            exit 0
        fi
        echo "⏳ 헬스체크 대기 중... ($i/$MAX_ATTEMPTS)"
        sleep $WAIT_TIME
    done

    echo "❌ Green 서비스 헬스체크 실패"
    docker-compose -f docker-compose.green.yml down
    exit 1
else
    echo "🟢 Green (현재 운영 중) → Blue로 전환 시작..."

    # Blue 버전 실행
    docker-compose -f docker-compose.yml -f docker-compose.blue.yml up -d --build

    # 헬스체크
    MAX_ATTEMPTS=6
    WAIT_TIME=5

    for i in $(seq 1 $MAX_ATTEMPTS); do
        if curl -sf http://localhost:8080/api/health > /dev/null; then
            echo "✅ Blue 서비스 헬스체크 성공!"
            echo "🛑 기존 Green 서비스 종료..."
            docker-compose -f docker-compose.green.yml down
            echo "✅ Blue 서비스가 운영 상태로 변경됨!"
            exit 0
        fi
        echo "⏳ 헬스체크 대기 중... ($i/$MAX_ATTEMPTS)"
        sleep $WAIT_TIME
    done

    echo "❌ Blue 서비스 헬스체크 실패"
    docker-compose -f docker-compose.blue.yml down
    exit 1
fi