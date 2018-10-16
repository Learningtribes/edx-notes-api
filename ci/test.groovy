pipeline {
    agent any
    environment {
        BUILD_TAG = "${env.BUILD_TAG.toLowerCase()}"
    }
    stages {
        stage("Stop Old Build") {
            steps {
                milestone label: "", ordinal:  Integer.parseInt(env.BUILD_ID) - 1
                milestone label: "", ordinal:  Integer.parseInt(env.BUILD_ID)
            }
        }
        stage("Test") {
            steps {
                sh "BUILD_TAG=${BUILD_TAG} make ci_up"
                sh "BUILD_TAG=${BUILD_TAG} make ci_test"
                junit "nosetests.xml"
                cobertura coberturaReportFile: "**/build/coverage/coverage.xml", failUnstable: false, maxNumberOfBuilds: 20, onlyStable: false, zoomCoverageChart: false
            }
            post {
                always {
                    sh "BUILD_TAG=${BUILD_TAG} make ci_clean"
                    sh "BUILD_TAG=${BUILD_TAG} make ci_down"
                }
            }
        }
    }
}
