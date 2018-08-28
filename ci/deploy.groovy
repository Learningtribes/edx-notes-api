@NonCPS
def commitHashForBuild(build) {
  def scmAction = build?.actions.find { action -> action instanceof jenkins.scm.api.SCMRevisionAction }
  return scmAction?.revision?.hash
}
def commitId = null
def upstreamProjectName = "notes"
def selectedIpAddress = null
def step = null
def failPercentage = null

pipeline {
    agent any
    stages {
        stage("Get parameters") {
            steps {
                script {
                    def machine = null
                    timeout(time: 5) {

                        // get parameters
                        if(env.GIT_BRANCH =~ "(master|hotfix).*"){
                            machine = input message: "environment you want to deploy to?",
                                parameters: [choice(choices: ['stage', 'production'], description: '', name: 'machine')]
                        } else {
                            machine = "stage"
                        }
                        if (machine == "stage" || env.GIT_BRANCH =~ "(hotfix).*") {
                            commitId = input message: "you are going to deploy to ${machine} environment, which commit id you want to use?",
                                parameters: [string(defaultValue: 'HEAD', description: '', name: 'commitId', trim: true)]
                        } else {
                            def build = input message: "you are going to deploy to ${machine} environment, choose a build to use.",
                                parameters: [run(description: '', filter: 'SUCCESSFUL', name: 'commitId', projectName: "${upstreamProjectName}/${env.GIT_BRANCH}")]

                            commitId = commitHashForBuild(build)
                        }

                        def ipAddresses = readFile("/tmp/stage.txt").tokenize("\n")
                        def para = input message: 'choose machine, use all for full deployment',
                            parameters: [choice(choices: ["all"] + ipAddresses, description: "", name: 'ipAddress'),
                                         string(defaultValue: '1', description: "", name: 'step', trim: true),
                                         string(defaultValue: '10', description: "", name: 'failPercentage', trim: true)]


                        // post process parameters
                        failPercentage = para['failPercentage'] as int
                        step = para['step'] as int
                        selectedIpAddress = para['ipAddress']

                        if (selectedIpAddress == 'all') {
                            selectedIpAddress = ipAddresses.join(",")
                        } else {
                            selectedIpAddress += ","
                        }
                        if (commitId.contains('~')) {
                            commitId = env.GIT_BRANCH + commitId.substring(4)
                        } else if (commitId == 'HEAD') {
                            commitId = env.GIT_BRANCH
                        }
                    }
                }
            }
        }
        stage("Deploy") {
            steps {
                sh """
                cd /edx/app/edx_ansible/edx_ansible/playbooks
                . ../../venvs/edx_ansible/bin/activate
                ansible-playbook --ssh-common-args='-o "StrictHostKeyChecking no"' \
                -u ubuntu -i ${selectedIpAddress} --key-file="/tmp/STAGING_SG.pem" \
                -e "EDX_NOTES_API_VERSION=${commitId}" -e "serial_count=${step}" \
                -e "fail_percentage=${failPercentage}" notes.yml
                """
            }
        }
    }
}
