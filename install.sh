#!/bin/bash

# EC2에 필요한 패키지 설치 및 배포 환경 구성 스크립트
echo "EC2 배포 환경 구성 시작"

# 1. 필수 패키지 설치
sudo apt update
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# 2. Docker 설치
if ! command -v docker &> /dev/null; then
    echo "Docker 설치 중..."
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
    sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
    sudo apt update
    sudo apt install -y docker-ce docker-ce-cli containerd.io
    sudo usermod -aG docker $USER
    echo "Docker 설치 완료"
else
    echo "Docker가 이미 설치되어 있습니다."
fi

# 3. Docker Compose 설치
if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose 설치 중..."
    sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    echo "Docker Compose 설치 완료"
else
    echo "Docker Compose가 이미 설치되어 있습니다."
fi

# 4. 프로젝트 디렉토리 구성
mkdir -p jenkins nginx make-decision-helper-back make-decision-helper-front deploy

# 5. Docker 네트워크 생성
docker network create app-network || true

# 6. 권한 설정
chmod +x start-jenkins.sh

echo "설치 완료! 다음 단계를 진행하세요:"
echo "1. 소스 코드를 각 디렉토리에 복사하세요."
echo "2. './start-jenkins.sh'를 실행하여 Jenkins를 시작하세요."
echo "3. Jenkins 파이프라인을 설정하세요."

# 새 세션에서 Docker 그룹 적용
echo "새 터미널을 열어 'newgrp docker' 명령어를 실행하여 Docker 그룹 권한을 적용하세요."