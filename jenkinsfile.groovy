pipeline {
    agent any

    parameters {
        string(name: 'THREADS', defaultValue: '10')
        string(name: 'RAMPUP', defaultValue: '30')
        string(name: 'LOOP', defaultValue: '5')
    }

    triggers {
        pollSCM 'H/15 * * * *'
    }

    stages {

        stage('Checkout Code') {
            steps {
                // Debug: List files to confirm checkout worked and files are present
                bat 'dir'
            }
        }

        stage('Build JMeter Image') {
            steps {
                // Disable BuildKit using withEnv to avoid Windows batch syntax errors
                withEnv(['DOCKER_BUILDKIT=0']) {
                    bat 'docker build --network=host -t jmeter-test -f Dockerfile .'
                }
                // Verify that the image was created successfully by listing it
                bat 'docker images jmeter-test'
            }
        }

        stage('Run JMeter Test') {
            steps {
                // Cleanup previous container so we can reuse the name (ignore error if not exists)
                bat 'docker rm -f jmeter-runner || echo Container not found, skipping removal'

                // Ensure results directory is clean before running; JMeter requires empty dir for HTML report
                bat 'if exist jmeter\\results rmdir /s /q jmeter\\results'
                bat 'mkdir jmeter\\results'
                bat """
                docker run --name jmeter-runner ^
                  -v "%WORKSPACE%":/jenkins_workspace ^
                  -v "%WORKSPACE%\\jmeter\\results":/jmeter/results ^
                  -v "%WORKSPACE%\\jmeter\\scripts":/jmeter/scripts ^
                  jmeter-test ^
                  -n ^
                  -t /jmeter/scripts/login_test.jmx ^
                  -l /jmeter/results/result.jtl ^
                  -e -o /jmeter/results/html ^
                  -Jthreads=${THREADS} ^
                  -Jrampup=${RAMPUP} ^
                  -Jloop=${LOOP}
                """

                // Debug: Verify container exists and show status (likely 'Exited')
                bat 'docker ps -a --filter "name=jmeter-runner"'
            }
        }

        stage('Publish Report') {
            steps {
                publishHTML([
                    reportDir: 'jmeter/results/html',
                    reportFiles: 'index.html',
                    reportName: 'JMeter Performance Report'
                ])
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'jmeter/results/**/*.jtl', allowEmptyArchive: true
        }
    }
}