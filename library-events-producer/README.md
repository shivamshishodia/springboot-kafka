# Library Events Producer

A basic Library events producer project.

## Project Structure

- `domain` package includes `Book` and `LibraryEvent` models.
- This microservice includes `New Book (POST)` and `Update Book (PUT)` endpoints (refer Thunderclient requests under resources).

## Send Messages

- `KafkaTemplates` are used to produce messages to Kafka topics. Docs: https://docs.spring.io/spring-kafka/reference/html/#kafka-template.
- `application.properties` contains all the required configurations for Kafka.

## Create Topics Using Code

- `KafkaAdmin` is used to create topics using code. 
- We need two beans, `KafkaAdmin` and `NewTopic` under `config.AutoCreateConfig` class. 
- Also, refer `spring.kafka.admin.bootstrap.servers` property.
- Start your project and look for your topic using `.\bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list`.

## Producer Approach #1

- `KafkaTemplate` is created under `producer.LibraryEventsProducer`.
- `KafkaTemplate.send()` is overloaded. Learn about all the methods. Docs: https://docs.spring.io/spring-kafka/reference/html/#kafka-template.
