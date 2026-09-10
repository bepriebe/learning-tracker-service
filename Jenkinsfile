pipeline {
    agent any

    parameters {
        booleanParam(name: 'PUBLISH_IMAGE', defaultValue: false,
            description: 'Publish the tested image to GitHub Container Registry')
    }

    options {
        disableConcurrentBuilds()
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        IMAGE_NAME = 'learning-tracker-service'
        REGISTRY_IMAGE = 'ghcr.io/bepriebe/learning-tracker-service'
    }

    stages {
        stage('Prepare image tag') {
            steps {
                script {
                    def branchHash = sh(script: 'printf "%s" "$BRANCH_NAME" | sha256sum | cut -c1-12', returnStdout: true).trim()
                    def revision = sh(script: 'git rev-parse --short=12 HEAD', returnStdout: true).trim()
                    env.IMAGE_TAG = "${branchHash}-${revision}-${env.BUILD_NUMBER}"
                }
            }
        }

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
                      --label "org.opencontainers.image.source=https://github.com/bepriebe/learning-tracker-service" \
                      --tag "${IMAGE_NAME}:${IMAGE_TAG}" \
                      .
                '''
            }
        }

        stage('Verify image') {
            steps {
                sh '''
                    docker image inspect \
                      "${IMAGE_NAME}:${IMAGE_TAG}" \
                      --format '{{.Id}}'
                '''
            }
        }

        stage('Publish image') {
            when {
                allOf {
                    expression { params.PUBLISH_IMAGE || env.BRANCH_NAME == 'dev' }
                    anyOf {
                        branch 'dev'
                        branch 'ci/jenkins-pipeline'
                        branch 'main'
                    }
                }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'ghcr-push',
                    usernameVariable: 'GHCR_USERNAME', passwordVariable: 'GHCR_TOKEN')]) {
                    sh '''
                        set +x
                        set -eu
                        DOCKER_CONFIG="$(mktemp -d)"
                        export DOCKER_CONFIG
                        trap 'rm -rf -- "$DOCKER_CONFIG"' EXIT
                        printf '%s' "$GHCR_TOKEN" | docker login ghcr.io \
                          --username "$GHCR_USERNAME" --password-stdin
                        docker tag "${IMAGE_NAME}:${IMAGE_TAG}" "${REGISTRY_IMAGE}:${IMAGE_TAG}"
                        docker push "${REGISTRY_IMAGE}:${IMAGE_TAG}"
                    '''
                }
                echo "Published ${env.REGISTRY_IMAGE}:${env.IMAGE_TAG}"
            }
        }

        stage('Deploy dev') {
            when {
                branch 'dev'
            }
            steps {
                sh '''
                    set -eu
                    DEPLOY_IMAGE="$(docker image inspect "${REGISTRY_IMAGE}:${IMAGE_TAG}" \
                      --format '{{range .RepoDigests}}{{println .}}{{end}}' \
                      | awk -v prefix="${REGISTRY_IMAGE}@sha256:" 'index($0, prefix) == 1 { print; exit }')"
                    bash scripts/deploy-dev.sh "$DEPLOY_IMAGE"
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
