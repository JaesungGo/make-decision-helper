#!/bin/bash

# 시스템 종료 스크립트

# 스크립트 실행 디렉토리를 기준으로 경로 설정
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR"

echo "시스템 종료 시작..."

# 종료 모드 선택
echo "종료 모드를 선택하세요:"
echo "1. 전체 종료 (모든 컨테이너 및 볼륨 삭제)"
echo "2. 애플리케이션만 종료 (데이터베이스 유지)"
echo "3. 취소"
read -p "선택 (1-3): " SHUTDOWN_MODE

case $SHUTDOWN_MODE in
    1)
        echo "전체 시스템을 종료합니다..."

        # Blue 환경 종료
        cd deploy
        docker-compose -f docker-compose.blue.yml down

        # Green 환경 종료
        docker-compose -f docker-compose.green.yml down

        # 공통 서비스 종료 (볼륨 포함)
        docker-compose -f docker-compose.yml down -v
        cd ..

        # Jenkins 종료
        docker-compose -f jenkins-compose.yml down -v

        echo "모든 Docker 이미지 정리"
        docker image prune -af

        echo "Docker 네트워크 정리"
        docker network rm app-network || true

        echo "✅ 전체 종료 완료!"
        ;;

    2)
        echo "애플리케이션 환경만 종료합니다..."

        # Blue 환경 종료
        cd deploy
        docker-compose -f docker-compose.blue.yml down

        # Green 환경 종료
        docker-compose -f docker-compose.green.yml down
        cd ..

        echo "✅ 애플리케이션 종료 완료! (데이터베이스 유지)"
        ;;

    3)
        echo "종료가 취소되었습니다."
        exit 0
        ;;

    *)
        echo "잘못된 선택입니다. 종료합니다."
        exit 1
        ;;
esac