def call(Map config = [:]) {

    stage('Clone') {
        echo "Cloning Ansible Assignment-5 repository..."

        git(
            branch: 'main',
            url: 'https://github.com/aanya-devops/Ansible_assignment.git'
        )
    }

    stage('User Approval') {

        if (config.keepApprovalStage) {
            input(
                message: "Deploy SonarQube to ${config.environment}?",
                ok: "Approve Deployment"
            )
        }
    }

    stage('Playbook Execution') {

        echo "Running SonarQube Ansible Playbook..."

        sh """
            ansible-playbook \
            ${config.codeBasePath}/site.yml \
            -i ${config.codeBasePath}/inventory
        """
    }

    stage('Notification') {

        echo "Sending deployment notification..."

        slackSend(
            channel: config.slackChannelName,
            message: config.actionMessage
        )
    }
}
