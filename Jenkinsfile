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
            environment {
                COMPOSE_PROJECT_NAME = "ci-${sh(script: 'printf "%s" "${JOB_NAME}:${BUILD_NUMBER}:${WORKSPACE}" | sha256sum | cut -c1-24', returnStdout: true).trim()}"
            }

            steps {
                dir('target/surefire-reports') {
                    deleteDir()
                }
                sh 'docker compose --profile test run --name "${COMPOSE_PROJECT_NAME}-test" test'
            }

            post {
                always {
                    script {
                        try {
                            sh '''
                                mkdir -p target/surefire-reports
                                docker cp "${COMPOSE_PROJECT_NAME}-test:/workspace/target/surefire-reports/." target/surefire-reports/
                            '''
                            junit testResults: 'target/surefire-reports/TEST-*.xml', allowEmptyResults: false
                        } finally {
                            sh '''
                                docker rm -f "${COMPOSE_PROJECT_NAME}-test"
                                docker compose --profile test down --volumes
                            '''
                        }
                    }
                }
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
