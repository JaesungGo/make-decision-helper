#!/bin/bash

# 수동 배포 스크립트 (Jenkins 없이 직접 배포할 때 사용)

# 스크립트 실행 디렉토리를 기준으로 경로 설정
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR"

# 네트워크 생성 (없는 경우)
docker network create app-network || true

# 현재 실행 중인 환경 확인
BLUE_RUNNING=$(docker ps -q -f "name=make-decision-helper-backend-blue")

if [ -n "$BLUE_RUNNING" ]; then
    echo "🔵 Blue 환경이 실행 중입니다. Green 환경을 준비합니다."
    DEPLOY_ENV="green"
else
    echo "🟢 Green 환경이 실행 중이거나 아무 환경도 실행 중이 아닙니다. Blue 환경을 준비합니다."
    DEPLOY_ENV="blue"
fi

# 공통 서비스 확인 및 시작
MONGODB_RUNNING=$(docker ps -q -f "name=mongodb")
REDIS_RUNNING=$(docker ps -q -f "name=redis")

if [ -z "$MONGODB_RUNNING" ] || [ -z "$REDIS_RUNNING" ]; then
    echo "📦 데이터베이스 서비스를 시작합니다..."
    cd deploy
    docker-compose -f docker-compose.yml up -d
    cd ..
fi

# 애플리케이션 서비스 배포
echo "🚀 $DEPLOY_ENV 환경을 배포합니다..."
cd deploy
docker-compose -f docker-compose.yml -f docker-compose.$DEPLOY_ENV.yml up -d --build

# 헬스 체크
HEALTH_PORT=$DEPLOY_ENV
if [ "$DEPLOY_ENV" = "blue" ]; then
    HEALTH_PORT="8080"
else
    HEALTH_PORT="8081"
fi

MAX_ATTEMPTS=6
WAIT_TIME=10

for i in $(seq 1 $MAX_ATTEMPTS); do
    if curl -sf http://localhost:$HEALTH_PORT/api/health > /dev/null; then
        echo "✅ $DEPLOY_ENV 환경 헬스체크 성공!"

        # 환경 전환 여부 확인
        read -p "새 환경으로 트래픽을 전환하시겠습니까? (y/n): " SWITCH
        if [ "$SWITCH" = "y" ] || [ "$SWITCH" = "Y" ]; then
            chmod +x switch_blue_green.sh
            ./switch_blue_green.sh
        else
            echo "환경 전환이 취소되었습니다. 두 환경이 모두 실행 중입니다."
        fi

        break
    else
        echo "⏳ 헬스체크 대기 중... ($i/$MAX_ATTEMPTS)"
        if [ $i -eq $MAX_ATTEMPTS ]; then
            echo "❌ $DEPLOY_ENV 환경 헬스체크 실패"
            echo "배포된 환경을 정리합니다..."
            docker-compose -f docker-compose.$DEPLOY_ENV.yml down
            exit 1
        fi
        sleep $WAIT_TIME
    fi
done