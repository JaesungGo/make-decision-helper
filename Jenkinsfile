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
                   sh '''
                       chmod +x gradlew
                       ./gradlew clean bootJar -x test
                       cp build/libs/*.jar app.jar
                   '''
               }
           }
       }
       stage('Frontend Build') {
           steps {
               dir('make-decision-helper-front') {
                   sh '''
                       npm ci
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
                sh "${DOCKER_COMPOSE} up -d --build"
           }
       }
   }
   post {
       always {
           cleanWs()
       }
   }
}