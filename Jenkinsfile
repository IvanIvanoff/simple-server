podTemplate(label: 'simple-service-builder', containers: [
  containerTemplate(
    name: 'docker-compose',
    image: 'docker/compose:1.22.0',
    ttyEnabled: true,
    command: 'cat',
    envVars: [
      envVar(key: 'DOCKER_HOST', value: 'tcp://docker-host-docker-host:2375')
    ])
]) {
  node('simple-service-builder') {

    def scmVars = checkout scm

    container('docker-compose') {
      stage('Build') {
        sh "docker build --target builder -t localhost/simple-service-builder:${scmVars.GIT_COMMIT} ."
      }

      stage('Test') {
          try {
            sh "docker-compose -f docker-compose-test.yml build test"
            sh "docker-compose -f docker-compose-test.yml run test"
          } finally {
            sh "docker-compose -f docker-compose-test.yml down -v"
          }
      }

      if (env.BRANCH_NAME == "master") {
        stage('Publish') {
          withCredentials([
            string(
              credentialsId: 'aws_account_id',
              variable: 'aws_account_id'
            )
          ]) {
            def awsRegistry = "${env.aws_account_id}.dkr.ecr.eu-central-1.amazonaws.com"
            docker.withRegistry("https://${awsRegistry}", "ecr:eu-central-1:ecr-credentials") {
              sh "docker build \
                -t ${awsRegistry}/simple-service:${env.BRANCH_NAME} \
                -t ${awsRegistry}/simple-service:${scmVars.GIT_COMMIT} ."
              sh "docker push ${awsRegistry}/simple-service:${env.BRANCH_NAME}"
              sh "docker push ${awsRegistry}/simple-service:${scmVars.GIT_COMMIT}"
            }
          }
        }
      }
    }
  }
}