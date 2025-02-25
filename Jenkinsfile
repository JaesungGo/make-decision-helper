pipeline {
    agent any

    environment {
        WORKSPACE = "/workspace"
    }

    stages {
        stage('Preparation') {
            steps {
                sh '''
                echo "워크스페이스 확인"
                ls -la $WORKSPACE
                echo "호스트 Docker 확인"
                docker --version
                docker-compose --version
                '''
            }
        }

        stage('Deploy Database Services') {
            steps {
                sh '''
                cd $WORKSPACE/deploy
                docker network create app-network || true

                # 현재 실행 중인 서비스 확인
                BLUE_RUNNING=$(docker ps -q -f "name=make-decision-helper-backend-blue")
                GREEN_RUNNING=$(docker ps -q -f "name=make-decision-helper-backend-green")

                # DB 서비스 실행
                docker-compose -f docker-compose.yml up -d mongodb redis
                '''
            }
        }

        stage('Deploy Application') {
            steps {
                sh '''
                cd $WORKSPACE/deploy

                # 현재 실행 중인 환경 확인
                BLUE_RUNNING=$(docker ps -q -f "name=make-decision-helper-backend-blue")

                if [ -n "$BLUE_RUNNING" ]; then
                    echo "Blue 환경이 실행 중입니다. Green 환경을 준비합니다."
                    DEPLOY_ENV="green"
                else
                    echo "Green 환경이 실행 중이거나 아무 환경도 실행 중이 아닙니다. Blue 환경을 준비합니다."
                    DEPLOY_ENV="blue"
                fi

                # 선택된 환경 배포
                echo "$DEPLOY_ENV 환경을 배포합니다."
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
                        break
                    else
                        echo "⏳ 헬스체크 대기 중... ($i/$MAX_ATTEMPTS)"
                        if [ $i -eq $MAX_ATTEMPTS ]; then
                            echo "❌ $DEPLOY_ENV 환경 헬스체크 실패"
                            exit 1
                        fi
                        sleep $WAIT_TIME
                    fi
                done
                '''
            }
        }

        stage('Switch Environment') {
            steps {
                script {
                    def userInput = input(
                        message: '새 환경으로 전환하시겠습니까?',
                        parameters: [
                            booleanParam(defaultValue: false, description: '예: 트래픽 전환, 아니오: 롤백', name: 'SWITCH')
                        ]
                    )

                    if (userInput) {
                        sh '''
                        cd $WORKSPACE/deploy
                        chmod +x switch_blue_green.sh
                        ./switch_blue_green.sh
                        '''
                    } else {
                        sh '''
                        cd $WORKSPACE/deploy

                        # 롤백: 가장 최근에 배포된 환경 종료
                        BLUE_RUNNING=$(docker ps -q -f "name=make-decision-helper-backend-blue")

                        if [ -n "$BLUE_RUNNING" ]; then
                            echo "Blue 환경을 롤백합니다."
                            docker-compose -f docker-compose.blue.yml down
                        else
                            echo "Green 환경을 롤백합니다."
                            docker-compose -f docker-compose.green.yml down
                        fi
                        '''
                    }
                }
            }
        }
    }

    post {
        failure {
            sh '''
                echo "❌ 배포 실패, 정리 중..."
                cd $WORKSPACE/deploy

                # 마지막으로 배포된 환경 확인
                BLUE_RUNNING=$(docker ps -q -f "name=make-decision-helper-backend-blue")

                if [ -n "$BLUE_RUNNING" ]; then
                    echo "실패한 Blue 환경을 정리합니다."
                    docker-compose -f docker-compose.blue.yml down
                else
                    echo "실패한 Green 환경을 정리합니다."
                    docker-compose -f docker-compose.green.yml down
                fi
            '''
        }
        success {
            echo "✅ 배포가 성공적으로 완료되었습니다!"
        }
    }
}