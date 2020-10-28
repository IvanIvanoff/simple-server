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

1) Service that was deployed: https://github.com/santiment/simple-server
2) Dockerfile: https://github.com/santiment/simple-server/blob/master/Dockerfile
3) Docker-compose: https://github.com/santiment/simple-server/blob/master/docker-compose.yml
4) Jenkinsfile: https://github.com/santiment/simple-server/blob/master/Jenkinsfile
5) k8s deployment: https://github.com/santiment/devops/pull/553/files
6) Jenkins job: https://github.com/santiment/simple-server/blob/master/Jenkins-job.groovy
7) npm run <docker-dev/docker-test> to run locally: https://github.com/santiment/simple-server/blob/master/package.json#L8-L9

Jenkins is accessible from: https://jenkins.internal.santiment.net/
You log in with your github account, which should be part of Santiment organization.
When there is a new commit in master, Jenkins will automatically pick it up and use
the Jenkinsfile (4) to execute what's described in there. In most cases the Jenkinsfile 
contains instructions how to build an image from the given Dockerfile. Also in most cases it runs a few stages and aborts if any of them fail: test, build, push to ECR. When an image is
built, it is pushed to our ECR from where we can deploy it in our clusters. Usually the master build is also triggering a stage deployment. Production deployment must always be manually initiated

There is another jenkins job (it's defined in Jenkins, not the repo in github) which knows how to deploy a service in our cluster. In the case of 6) this is the sh "kubectl set image deployment/simple-server simple-server=${taggedSource}" line.

If there is a new repo, you need to ask the devops to create ECR for it (https://aws.amazon.com/ecr/) and manually run an organization scan, so jenkins can pick up that repo. It is running such scans once per day and we do not want to wait that long usually.
Scanning is done by going to Santiment LLC from the home page and then clicking on Scan Organization Now on the left.
