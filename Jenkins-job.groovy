podTemplate(label: 'simple-server-staging-deploy', containers: [
    containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.10.9', command: 'cat', ttyEnabled: true),
    containerTemplate(name: 'docker', image: 'docker', ttyEnabled: true, command: 'cat', envVars: [
        envVar(key: 'DOCKER_HOST', value: 'tcp://docker-host-docker-host:2375')
      ])
  ]) {
  node('simple-server-staging-deploy') {
    stage('Update deployment') {
      git url: 'https://github.com/santiment/simple-server', credentialsId:'GitHubCheckoutCreds'
      def gitCommit = sh(returnStdout: true, script: "git rev-parse HEAD").trim()

      withCredentials([
          string(
            credentialsId: 'aws_account_id',
            variable: 'aws_account_id'
          )
        ]){

        def awsRegistry = "${env.aws_account_id}.dkr.ecr.eu-central-1.amazonaws.com"
        def sourceImage = "${awsRegistry}/simple-server"
        def taggedSource = "${sourceImage}:${gitCommit}"

        /* Deploy the image */

        container('kubectl') {
          sh "kubectl version"
          sh "kubectl set image deployment/simple-server simple-server=${taggedSource}"
        }

        /* Tag the deployed image */
        container('docker') {
          def timestampTag = "stage-${env.TIMESTAMP_IMAGE_TAG}"
          def taggedStage = "${sourceImage}:stage"
          def timestamped = "${sourceImage}:${timestampTag}"

          docker.withRegistry("https://${awsRegistry}", "ecr:eu-central-1:ecr-credentials") {
            sh "docker pull ${taggedSource}"
            sh "docker tag ${taggedSource} ${taggedStage}"
            sh "docker tag ${taggedSource} ${timestamped}"
            sh "docker push ${taggedStage}"
            sh "docker push ${timestamped}"
          }
        }
      }
    }
  }
}
