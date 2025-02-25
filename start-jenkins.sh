#!/bin/bash

# 스크립트 실행 디렉토리를 기준으로 경로 설정
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR"

echo "Jenkins 빌드 및 시작"

# Jenkins 디렉토리 확인 및 생성
if [ ! -d "./jenkins" ]; then
  echo "Jenkins 디렉토리 생성"
  mkdir -p jenkins
  # Jenkins Dockerfile 생성
  cp jenkins-dockerfile jenkins/Dockerfile || {
    echo "Jenkins Dockerfile 복사 실패"
    exit 1
  }
fi

# Docker 네트워크 확인 및 생성
docker network inspect app-network >/dev/null 2>&1 || {
  echo "app-network 생성"
  docker network create app-network
}

# 현재 실행 중인 Jenkins 컨테이너 확인
JENKINS_RUNNING=$(docker ps -q -f "name=jenkins")
if [ -n "$JENKINS_RUNNING" ]; then
  echo "Jenkins가 이미 실행 중입니다."
else
  # Jenkins 이미지 빌드 및 실행
  docker-compose -f jenkins-compose.yml up -d --build
  echo "Jenkins 시작됨 - http://localhost:9090"
  echo "초기 관리자 비밀번호를 확인하려면 다음 명령어를 실행하세요:"
  echo "docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword"
fi

echo "Jenkins 설정 방법:"
echo "1. http://localhost:9090 접속"
echo "2. 초기 관리자 비밀번호 입력"
echo "3. 권장 플러그인 설치"
echo "4. 관리자 계정 생성"
echo "5. 새 파이프라인 생성"
echo "6. 파이프라인 정의에서 'Pipeline script from SCM' 선택"
echo "7. SCM에서 'Git' 선택"
echo "8. 저장소 URL 입력"
echo "9. Jenkinsfile 경로 지정: 'Jenkinsfile'"