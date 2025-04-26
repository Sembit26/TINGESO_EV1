pipeline {
    agent any
    tools {
        maven 'maven_3_8_1'
    }
    stages {
        stage('Build maven') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/main']],
                    extensions: [],
                    userRemoteConfigs: [[url: 'https://github.com/Sembit26/TINGESO_EV1']]
                )
                bat 'mvn clean package'
            }
        }

        stage('Unit Tests') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Build docker image') {
            steps {
                script {
                    bat 'docker build -t sembit26/appwebkarting-backend .'
                }
            }
        }

        stage('Push image to Docker Hub') {
            steps {
                script {
                    // Usar la credencial de Docker Hub para login
                    withCredentials([string(credentialsId: 'dhpswid', variable: 'dhpsw')]) {
                        bat 'docker login -u sembit26 -p %dhpsw%'
                    }
                    bat 'docker push sembit26/appwebkarting-backend'
                }
            }
        }
    }
}
