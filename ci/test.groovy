pipeline {
    agent any
    stages {
        stage('Stop Old Build') {
            steps {
                milestone label: '', ordinal:  Integer.parseInt(env.BUILD_ID) - 1
                milestone label: '', ordinal:  Integer.parseInt(env.BUILD_ID)
            }
        }
        stage('Test') {
            steps {
                sh "make ci_up"
                sh 'make ci_test'
                junit 'nosetests.xml'
                cobertura coberturaReportFile: '**/build/coverage/coverage.xml', failUnstable: false, maxNumberOfBuilds: 20, onlyStable: false, zoomCoverageChart: false
            }
            post {
                always {
                    sh 'make ci_clean'
                    sh 'make ci_down'
                }
            }
        }
    }
}
