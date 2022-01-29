# Spring Boot Kafka

A basic event-driven Spring boot project.

## Project Setup

- Download Kafka (recommended binary tarball) from https://kafka.apache.org/downloads. 
- Kafka 2.13 was used for this project https://www.apache.org/dyn/closer.cgi?path=/kafka/3.1.0/kafka_2.13-3.1.0.tgz.
- Extract the contents of this folder inside `C:\kafka`.
- `bin\windows` contains the required scripts and `config` contains configs.

## Setup Zoo-keeper and Kafka Broker

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
- Instantiate Console Producer: `.\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic test-topic`.
- Instantiate Console Consumer: `.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test-topic --from-beginning`.
- Now you can actually chat between the two consoles.
  