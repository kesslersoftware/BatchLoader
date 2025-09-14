pipeline {
    agent any
    
    // Using Maven Wrapper and system Java - no tool configuration required
    
    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'staging', 'test', 'prod'],
            description: 'Target environment'
        )
        booleanParam(
            name: 'SKIP_SONAR',
            defaultValue: false,
            description: 'Skip SonarQube analysis'
        )
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.BUILD_VERSION = sh(
                        script: 'echo "${BUILD_NUMBER}-$(echo ${GIT_COMMIT} | cut -c1-7)"',
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Build & Test') {
            steps {
                script {
                    try {
                        sh '''
                            chmod +x ./mvnw
                            ./mvnw clean test jacoco:report
                        '''
                        echo "✅ Tests and coverage completed successfully"
                    } catch (Exception e) {
                        echo "⚠️ Tests failed but continuing build: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
            post {
                always {
                    script {
                        try {
                            junit(
                                testResults: 'target/surefire-reports/*.xml',
                                allowEmptyResults: true
                            )
                            echo "✅ Test results published successfully"
                        } catch (Exception e) {
                            echo "⚠️ Failed to publish test results: ${e.getMessage()}"
                        }
                        
                        try {
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target/site/jacoco',
                                reportFiles: 'index.html',
                                reportName: 'JaCoCo Coverage Report'
                            ])
                            echo "✅ JaCoCo coverage report published"
                        } catch (Exception e) {
                            echo "⚠️ Failed to publish JaCoCo report: ${e.getMessage()}"
                        }
                    }
                }
            }
        }
        
        stage('SonarQube Analysis') {
            when {
                expression { !params.SKIP_SONAR }
            }
            steps {
                script {
                    try {
                        withSonarQubeEnv('SonarQube') {
                            sh '''
                                ./mvnw sonar:sonar \\
                                    -Dsonar.projectKey=${JOB_NAME} \\
                                    -Dsonar.projectName="${JOB_NAME}" \\
                                    -Dsonar.projectVersion=${BUILD_VERSION}
                            '''
                        }
                        echo "✅ SonarQube analysis completed successfully"
                    } catch (Exception e) {
                        echo "⚠️ SonarQube analysis failed but continuing build: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Quality Gate (Informational Only)') {
            when {
                expression { !params.SKIP_SONAR }
            }
            steps {
                script {
                    try {
                        timeout(time: 3, unit: 'MINUTES') {
                            def qg = waitForQualityGate()
                            if (qg.status != 'OK') {
                                echo "⚠️ SonarQube Quality Gate failed: ${qg.status}"
                                echo "📊 This is informational only - build will continue"
                                currentBuild.result = 'UNSTABLE'
                            } else {
                                echo "✅ SonarQube Quality Gate passed"
                            }
                        }
                    } catch (Exception e) {
                        echo "⚠️ Quality Gate check failed but continuing: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                sh '''
                    chmod +x ./mvnw
                    echo "Packaging Spring Boot application..."
                    ./mvnw package -DskipTests -B
                '''
                
                archiveArtifacts(
                    artifacts: 'target/*.jar',
                    fingerprint: true
                )
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo "✅ Spring Boot pipeline completed successfully"
        }
        failure {
            echo "❌ Spring Boot pipeline failed"
        }
    }
}