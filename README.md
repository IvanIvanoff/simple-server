## Preface

This repo and the linked deployment steps serve the purposes of learning how to create and deploy new services in Santiment.

## Setup

To create the database you need `db-migrate`.
After installing it run `db-migrate db:create github-exporter` to create the database. The migrations are run by executing `db-migrate up`. The migrations also run as a `prestart` script, so they are run on `npm start`.

The `docker-compose.yaml` file provides `zookeeper`, `kafka` and `postgres` services so you can test without external dependencies.

To test the application:

- Run `docker-compose up zookeeper kafka postgres` to start these services
- Run `docker-compose build && docker-compose run exporter` to run the exporter itself.

Note: If you want to execute outside of docker-compose you need to provide several ENV variables:

- DATABASE_URL - Without it you cannot run the application. `db-migrate` requires either this ENV var or `database.json` file to be available. The migrations are run on `npm start`
- KAFKA_URL - Connection string for kafka. Defaults to `localhost:2181`
- ZOOKEEPER_URL - Connection string for zookeeper. Defaults to `localhost:9092`

## MacOS Setup

To avoid some issues with installing `node-rdkafka` execute this: `CPPFLAGS=-I/usr/local/opt/openssl/include LDFLAGS=-L/usr/local/opt/openssl/lib yarn add node-rdkafka`

Issues described here: https://github.com/Blizzard/node-rdkafka/issues/373

## Test

To run the tests execute:

```
docker-compose -f docker-compose-test.yml build test && docker-compose -f docker-compose-test.yml run test
```

This will start all needed dependencies - zookeeper, kafka and postgres.

## Useful commands

- Read 100 messages from the topic to ensure it works correctly :

```bash
$ docker-compose exec kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --from-beginning --topic github --max-messages 100
```

- List available topics:

```bash
$ docker-compose exec kafka kafka-topics.sh --zookeeper zookeeper:2181 --list
```

These commands are provided from the kafka image.

In order to test kafka from the command line locally you need to download a **Binary** download from https://kafka.apache.org/downloads.
You may want to do this when reading a topic from stage.

## Deployment Steps

This wiki serves the purposing of guiding you through the steps needed to be done for developing and deploying a new service from zero to production. The knowledge here can be applied to already existing services, too.

We're going to follow the development and deployment of a javascript service which logs a message every 10 seconds to the console. Usually, the application logic complexity is not connected to the deployment complexity, so such a simple service should do the job. We're going to call this service `simple-server`.

The steps that are done and the code that is written following roughly this order:

1) Service that is deployed: https://github.com/santiment/simple-server
2) Dockerfile: https://github.com/santiment/simple-server/blob/master/Dockerfile
3) Docker-compose: https://github.com/santiment/simple-server/blob/master/docker-compose.yml
4) Jenkinsfile: https://github.com/santiment/simple-server/blob/master/Jenkinsfile
5) k8s deployment: https://github.com/santiment/devops/pull/553/files
6) Jenkins job: https://github.com/santiment/simple-server/blob/master/Jenkins-job.groovy
7) npm run <docker-dev/docker-test> to run locally: https://github.com/santiment/simple-server/blob/master/package.json#L8-L9

## 1) Service that is deployed
The service that will be deployed needs to be hosted on github under the `santiment` organization. There are no restrictions on language/framework used.

## 2) Dockerfile
The dockerfile explains the steps that needed to be performed in order to build an image from the source code. Please check the [Dockerfile reference](https://docs.docker.com/engine/reference/builder/) and the `Dockerfile`s in any of our repos.

## 3) Docker-compose
Having a docker-compose file is optional, but useful. It allow us to build the dockerfile and start it alongside its dependencies easily. For example, if you are building an exporter that needs zookeeper and kafka, the process of building and starting the exporter will be simply executing `docker-compose build exporter && docker-compose run exporter` if you have the correct `docker-compose.yaml` file.
For example, if you want to start kafka and zookeeper in your tests you can have [docker-compose-test.yml](https://github.com/santiment/github-exporter/blob/master/docker-compose-test.yml) file and just run `docker-compose -f docker-compose-test.yml build && docker-compose -f docker-compose-test.yml run test`

## 4) Jenkinsfile
The Jenkinsfile contains instructions how to build an image from the given Dockerfile. In most cases it runs a few stages and aborts if any of them fail. The most common stages are `test`, `build` and `push to ECR`. When an image is built, it is pushed to our [ECR](https://aws.amazon.com/ecr/) from where we can deploy it in our clusters. Usually the master build is also triggering a stage deployment. Production deployment must always be manually initiated. In order to create an ECR for a new repository, you need to write to the devops team in the #devops channel - they'll know how to handle this.

In order to make Jenkins to start following your repo and build new images when there are new stuff in master or in another branch, you need to tell Jenkins about your new repository. This is done by logging in Jenkins with your github account (it must be part of santiment organization) from [here](https://jenkins.internal.santiment.net). Scanning is done by going to Santiment LLC from the home page and then clicking on Scan Organization Now on the left.

Now, when there are changes in any branch, Jenkins will automatically trigger the Jenkinsfile. What we often do is have a guard that builds and pushes a new container only on master builds. In all other cases we can only run the tests.

## 5) k8s-deployment
After we have our Dockerfile explaining how to build an image and the Jenkinsfile explaining how to test, build and push the build image to our ECR from where we can deploy it, we need to define how exactly we're going to deploy this service in our kubernetes cluster.

## 6) Jenkins job
There is another jenkins job (it's defined in Jenkins, not the repo in github) which knows how to deploy a service in our cluster. In the case of 6) this is the `sh "kubectl set image deployment/simple-server simple-server=${taggedSource}"` line.

There are 2 jobs you need to define. One resides in `Deploy to STAGING` directory and now resides in the `Deploy to PRODUCTION` directory. It's likely that you won't have access to these jobs configurations - test this by trying to access [this](https://jenkins.internal.santiment.net/job/Deploy%20to%20STAGING/job/Deploy%20golem-watcher%20to%20Staging/configure). In case you do not have access try contacting someon from the devops or the backend team for support.

## 7) Run locally
While developing you'll need to run the service locally to test and experiment with it. This is easily done by making use of the dockerfile and docker-compose file already defined
