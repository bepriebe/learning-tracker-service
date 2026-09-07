pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        IMAGE_NAME = 'learning-tracker-service'
    }

    stages {
        stage('Test') {
            steps {
                sh 'docker compose --profile test run --rm test'
            }
        }

        stage('Build image') {
            steps {
                sh '''
                    docker build \
                      --target runtime \
                      --tag "${IMAGE_NAME}:${BUILD_NUMBER}" \
                      .
                '''
            }
        }

        stage('Verify image') {
            steps {
                sh '''
                    docker image inspect \
                      "${IMAGE_NAME}:${BUILD_NUMBER}" \
                      --format '{{.Id}}'
                '''
            }
        }
    }

    post {
        success {
            echo "Build ${env.BUILD_NUMBER} completed successfully."
        }

        failure {
            echo "Build ${env.BUILD_NUMBER} failed."
        }

        always {
            echo "Pipeline finished with status: ${currentBuild.currentResult}"
        }
    }
}
