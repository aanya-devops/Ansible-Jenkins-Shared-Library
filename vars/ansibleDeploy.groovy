def call() {

    def config = [:]

    stage('Load Configuration') {

        echo "Loading deployment configuration..."

        def configFile = libraryResource('deployment.conf')

        configFile.split('\n').each { line ->

            line = line.trim()

            if (!line || line.startsWith('#')) {
                return
            }

            def parts = line.split('=', 2)

            if (parts.size() == 2) {
                config[parts[0].trim()] = parts[1].trim()
            }
        }

        echo "Environment: ${config.ENVIRONMENT}"
        echo "Code Base Path: ${config.CODE_BASE_PATH}"
        echo "Approval Stage: ${config.KEEP_APPROVAL_STAGE}"
    }

    stage('Clone') {

        echo "Cloning Ansible Assignment-5 repository..."

        git(
            branch: 'main',
            url: 'https://github.com/aanya-devops/Ansible_assignment.git'
        )
    }

    stage('User Approval') {

        if (config.KEEP_APPROVAL_STAGE.toBoolean()) {

            input(
                message: "Deploy SonarQube to ${config.ENVIRONMENT}?",
                ok: "Approve Deployment"
            )
        }
    }

    stage('Playbook Execution') {

    echo "Running SonarQube Ansible Playbook..."

    withCredentials([
        sshUserPrivateKey(
            credentialsId: 'ansible-ec2-key',
            keyFileVariable: 'SSH_KEY'
        )
    ]) {

        sh """
            cd ${config.CODE_BASE_PATH}

            chmod 600 "\$SSH_KEY"

            ansible-playbook \
            site.yml \
            -i inventory \
            --private-key "\$SSH_KEY" \
            -vvv
        """
    }
}
    stage('Notification') {

        echo "Sending deployment notification..."

        slackSend(
            channel: config.SLACK_CHANNEL_NAME,
            message: config.ACTION_MESSAGE
        )
    }
}
