# Spring Boot Kafka

A basic event-driven Spring boot project.

## Project Setup

- Download Kafka (recommended binary tarball) from https://kafka.apache.org/downloads. 
- Kafka 2.13 was used for this project https://www.apache.org/dyn/closer.cgi?path=/kafka/3.1.0/kafka_2.13-3.1.0.tgz.
- Extract the contents of this folder inside `C:\kafka`.
- `bin\windows` contains the required scripts and `config` contains configs.

## Setup Kafka

- In `config\server.properties` change to `log.dirs=c:/kafka/kafka-logs`.
- In `config\zookeeper.properties` change to `dataDir=c:/kafka/zookeeper`.
- In `config\server.properties` add following lines.
  
```
listeners=PLAINTEXT://localhost:9092
auto.create.topics.enable=false
```

- Start Zookeeper: Open cmd under `C:\kafka` and execute `.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties`.
- Start Kafka: Open cmd under `C:\kafka` and execute `.\bin\windows\kafka-server-start.bat .\config\server.properties`.
- Create Topic: `.\bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 4 --topic test-topic`.

## Send messages without key

- Instantiate Console Producer: `.\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic test-topic`.
- Instantiate Console Consumer: `.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test-topic --from-beginning`.
- Now you can send messages (only value, no key) between the two consoles.

## Send messages with key

- Instantiate Console Producer: `.\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic test-topic --property "key.separator=-" --property "parse.key=true"`.
- Instantiate Console Consumer: `.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test-topic --from-beginning -property "key.separator=-" --property "print.key=true"`.
- Now you can send messages (with key and value) between the two consoles. Example, 'A-Apple', 'A-Adam', 'A-Alpha', 'B-Boomer', etc. 

## Send messages to consumer group

- Instantiate Console Producer: `.\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic test-topic --property "key.separator=-" --property "parse.key=true"`.
- List down consumer groups: `.\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list`.
- Instantiate multiple Console Consumers (execute multiple times): `.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test-topic --group <group-name>"`.
- When you send messages from producer, they will get divided between the multiple instances of consumers. 
- If the producer has 4 partitions and 2 consumers are created, then each consumer will poll 2 partitions each.

## Setup multiple Kafka brokers

- The first step is to add a new server.properties (copy paste the existing server.properties under config folder). For example, rename to, `server-1.properties`.
- We need to modify three properties to start up a multi broker set up.

```
broker.id=<unique-broker-d>
listeners=PLAINTEXT://localhost:<unique-port>
log.dirs=/tmp/<unique-kafka-folder>
auto.create.topics.enable=false
```

- Example config for `server-1.properties` will be like below.

```
broker.id=1
listeners=PLAINTEXT://localhost:9093
log.dirs=/tmp/kafka-logs-1
auto.create.topics.enable=false
```

- Create two such new property files incrementing broker.id, listeners ports, and log.dirs by 1.
- Start the Kafka brokers one by one with new property files.

```
.\bin\windows\kafka-server-start.bat .\config\server.properties
```
```
.\bin\windows\kafka-server-start.bat .\config\server-1.properties
```
```
.\bin\windows\kafka-server-start.bat .\config\server-2.properties
```

- Create Topic: `.\bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 3 --partitions 3 --topic test-topic-replicated`.
- Note that `--replication-factor` should be less than or equal to the number of Kafka clusters. Read Replication and In-Sync-Replica (ISR).
- Instantiate Console Producer: `.\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic test-topic-replicated`.
- Instantiate Console Consumer: `.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test-topic-replicated --from-beginning`.

## Extra Commands

- List down topics: `.\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list`.
- List down consumer groups: `.\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --list`.
- List down consumer group offset: `.\bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group <consumer-group>`.
