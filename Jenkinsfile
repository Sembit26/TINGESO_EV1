pipeline {
    agent any
    tools {
        maven 'maven_3_8_1'
    }
    stages {
        stage('Build maven') {
            steps {
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/Sembit26/TINGESO_EV1']])
                dir("TINGESO"){
                    bat 'mvn clean package'
                }
            }
        }

        stage('Test') {
            steps {
                dir("TINGESO"){
                    bat 'mvn test'
                }
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                dir("TINGESO"){
                    script {
                        withDockerRegistry(credentialsId: "docker-credentials"){
                            bat 'docker build -t sembit26/appwebkarting-backend .'
                        }
                        bat 'docker build -t sembit26/appwebkarting-backend .'
                        bat 'docker push sembit26/appwebkarting-backend'
                    }
                }
            }
        }
    }
}
