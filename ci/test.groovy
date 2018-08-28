pipeline {
    agent any
    environment {
        TERM = "xterm-256color"
    }
    stages {
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
