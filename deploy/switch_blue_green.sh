#!/bin/bash

BLUE_RUNNING=$(docker ps -q -f "name=make-decision-helper-backend-blue")
DEPLOY_COLOR=""
CURRENT_COLOR=""

if [ -n "$BLUE_RUNNING" ]; then
    DEPLOY_COLOR="green"
    CURRENT_COLOR="blue"
else
    DEPLOY_COLOR="blue"
    CURRENT_COLOR="green"
fi

echo "🚀 Deploying ${DEPLOY_COLOR} version..."

# 새 버전 배포
docker-compose -f docker-compose.${DEPLOY_COLOR}.yml up -d --build

# 헬스체크를 위한 대기
echo "⏳ Waiting for health check (30s)..."
sleep 30

# 이전 버전 종료
echo "🛑 Shutting down ${CURRENT_COLOR} version..."
docker-compose -f docker-compose.${CURRENT_COLOR}.yml down

echo "✅ Deployment complete! Now running on ${DEPLOY_COLOR} version"