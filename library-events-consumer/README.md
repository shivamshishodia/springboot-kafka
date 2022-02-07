# Library Events Consumer

A basic library events consumer project.

## Spring Kafka Consumer Options

- `MessageListenerContainer` (Interface, has two implementation given below).
  - `KafkaMessageListenerContainer` (Single threaded, polls the records, commits the offsets).
  - `ConcurrentMessageListenerContainer` (multiple `KafkaMessageListenerContainer`).
- `@KafkaListener` Annotation (uses `ConcurrentMessageListenerContainer` behind the scenes).
- [Spring Receiving Messages](https://docs.spring.io/spring-kafka/reference/html/#receiving-messages).

## Receive Messages

- Refer `application.properties` for properties related to bootstrap-servers, key-deserializer, value-deserializer and group-id.
- `@EnableKafka` is used under `config.LibraryEventsConsumerConfig`.
- `@KafkaListener(topics = {"library-events"})` is used under `consumer.LibraryEventsConsumer.onMessage()`.
- Values passed inside `ConsumerRecords` are given below. Notice headers and key-value data.

```
String topic;
int partition;
long offset;
long timestamp;
TimestampType timestampType;
int serializedKeySize;
int serializedValueSize;
Headers headers;
K key;
V value;
Optional<Integer> leaderEpoch;

ConsumerRecord(topic = library-events, partition = 2, leaderEpoch = 9, offset = 6, CreateTime = 1644001728552, serialized key size = -1, serialized value size = 147, headers = RecordHeaders(headers = [RecordHeader(key = event-source, value = [98, 97, 114, 99, 111, 100, 101, 45, 115, 99, 97, 110, 110, 101, 114]), RecordHeader(key = event-purpose, value = [99, 114, 101, 97, 116, 101, 45, 117, 112, 100, 97, 116, 101, 45, 98, 111, 111, 107])], isReadOnly = false), key = null, value = {"libraryEventId":null,"libraryEventType":"NEW","book":{"bookId":456,"bookName":"(Producer Record) Kafka using Spring boot","bookAuthor":"Shivam"}})
```

## Publish Messages from Producer 

- We use 'Producer Approach #3' (Specific Topic with ProducerRecord and Headers).
- Start Zookeeper: Open cmd under `C:\kafka` and execute `.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties`.
- Start Kafka Brokers: Open cmd under `C:\kafka` and execute `.\bin\windows\kafka-server-start.bat .\config\server.properties`.
- Start Kafka Brokers: Open cmd under `C:\kafka` and execute `.\bin\windows\kafka-server-start.bat .\config\server-1.properties`.
- Start Kafka Brokers: Open cmd under `C:\kafka` and execute `.\bin\windows\kafka-server-start.bat .\config\server-2.properties`.
- POST request to the producer controller `/v1/libraryevent/topic`. Refer thunder-collection under producer resources.

## Consumer Offset

- Spring allows commiting offsets based on the configurations. [Committing Offsets Docs.](https://docs.spring.io/spring-kafka/reference/html/#ooo-commits)
- In order to manually manage the offsets, an acknowledgement needs to be sent by the consumer after reading the records. Post this, consumer offsets are committed in the topic (__consumer_offsets).
- First, you need to set the container properties for manual ack in `config.LibraryEventsConsumerConfig.kafkaListenerContainerFactory()`.
- Second, you need to create a consumer which implements `AcknowledgingMessageListener`. Coded under `consumer.LibraryEventsConsumerManualOffsets.onMessage()`.
- IMPORTANT: We will be using batch acknowledge coded under `consumer.LibraryEventsConsumer.onMessage()` for this course, that is why config for manual ack are commented.
- Search for `UNCOMMENT TO USE MANUAL ACK` in the project to refer manual ack configs.

## Database (H2)

- Console URL: http://localhost:8081/h2-console/.
- All the associated properties are included in `application.properties`.
- The `entity` package contains classes from Producer service package `libraryeventsproducer.domain`.
- Flow: Consumer `LibraryEventsConsumer` > Service `LibraryEventsService` > Data `LibraryEventsRepository`.

## Error Handling

- `config.LibraryEventsConsumerConfig.kafkaListenerContainerFactory()` has `factory.setErrorHandler()` which helps in handling custom errors. 
- You can decide to persist the failed records inside the database for tracking purpose inside `factory.setErrorHandler()` as well.

## Retry

- The consumer APIs might fail to persist the record into database after a successful `onMessage()` from topic. This can happen due to multiple runtime exceptions while saving data into database (for example).
- In your `factory.setRetryTemplate()` you can configure configurations like `retryTemplate.setRetryPolicy()` and `simpleRetryPolicy.setMaxAttempts()`.
- Refer `config.LibraryEventsConsumerConfig` > `customRetryTemplate()` and `customRetryPolicy()` and the comments.

## Retry for specific exceptions

- The consumer APIs might fail to persist the record into database after a successful `onMessage()` from topic. This can happen due to multiple runtime exceptions while saving data into database (for example).
- We can configure Kafka to ONLY retry for failed records when a specific exception occurs. Retry can be ignored for other exceptions.
- Under `consumer.LibraryEventsConsumer`, we can choose to ignore IllegalArgumentException and only retry when RecoverableDataAccessException is encountered.
- To configure this we will use `customExceptionSpecificRetryPolicy()` instead of `customRetryPolicy()` under `config.LibraryEventsConsumerConfig`.
- Execute a POST requst from your producer API with "libraryEventId" = 0. This will catch the exception and retry.

## Recovery after retry exhaustion

- First thing to do is write the recovery callback logic inside `config.LibraryEventsConsumerConfig.kafkaListenerContainerFactory`. This is done using `factory.setRecoveryCallback()` which takes the retry context.
- Once the `RecoverableDataAccessException` is caught, the failed record is fetched from retry context and passed to the service layer via `libraryEventsService.handleRecovery()`.
- `service.LibraryEventsService.handleRecovery()` then publish the failed record back to the topic.
- Configurations for producer are included inside `application.properties` and code to publish records `service.LibraryEventsService.handleRecovery()` is same as that of producer API (using KafkaTemplate).
