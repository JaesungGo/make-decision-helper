FROM jenkins/jenkins:lts

USER root

RUN apt-get update && apt-get install -y openjdk-17-jdk curl \
    && curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v \
    && jenkins-plugin-cli --plugins workflow-aggregator

USER jenkins