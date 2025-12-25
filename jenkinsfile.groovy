pipeline {
    agent any

    parameters {
        string(name: 'THREADS', defaultValue: '10')
        string(name: 'RAMPUP', defaultValue: '30')
        string(name: 'LOOP', defaultValue: '5')
    }

    stages {

        stage('Checkout Code') {
            steps {
                // TODO: Update 'url' to your actual GitHub repository URL
                git branch: 'main',
                    url: 'https://github.com/sathi8119995/JMeterCICD.git'
            }
        }

        stage('Build JMeter Image') {
            steps {
                sh 'docker build -t jmeter-test -f docker/Dockerfile .'
            }
        }

        stage('Run JMeter Test') {
            steps {
                // Ensure results directory is clean before running; JMeter requires empty dir for HTML report
                sh 'rm -rf jmeter/results && mkdir -p jmeter/results'
                sh """
                docker run --rm \
                  -v \$(pwd)/jmeter/results:/jmeter/results \
                  -v \$(pwd)/jmeter/scripts:/jmeter/scripts \
                  jmeter-test \
                  -n \
                  -t /jmeter/scripts/login_test.jmx \
                  -l /jmeter/results/result.jtl \
                  -e -o /jmeter/results/html \
                  -Jthreads=${THREADS} \
                  -Jrampup=${RAMPUP} \
                  -Jloop=${LOOP}
                """
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