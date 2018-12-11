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
