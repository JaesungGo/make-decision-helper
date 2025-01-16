pipeline {
    agent any

    environment {
        DOCKER_COMPOSE = 'docker-compose'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Build') {
            steps {
                dir('make-decision-helper-back') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean bootJar -x test'
                }
            }
        }

        stage('Frontend Build') {
            steps {
                dir('make-decision-helper-front') {
                    sh '''
                            node -v
                            npm -v
                            npm ci  # npm install 대신 ci 사용
                            npm run build

                    '''
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                sh "${DOCKER_COMPOSE} build"
            }
        }

        stage('Deploy') {
            steps {
                sh "${DOCKER_COMPOSE} down || true"
                sh "${DOCKER_COMPOSE} up -d"
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}