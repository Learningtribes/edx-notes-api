pipeline {
    agent {
        dockerfile {
            filename 'ci/Dockerfile'
            -v /var/run/docker.sock:/var/run/docker.sock
            -v /usr/bin/docker:/usr/bin/docker
            -v /usr/local/bin/docker-compose:/usr/local/bin/docker-compose
        }
    }
    stages {
        stage('Test') {
            steps {
                sh 'make ci_up'
                sh 'make test'
            }
            post {
                always {
                    echo 'something'
                }
            }
        }
    }
}
