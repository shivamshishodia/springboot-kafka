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

## Producer Approach #1 (Async)

- `KafkaTemplate` is created under `producer.LibraryEventsProducer.sendLibraryEvent()`.
- `KafkaTemplate.sendDefault()` is overloaded and is async in nature. Learn about all the methods. Docs: https://docs.spring.io/spring-kafka/reference/html/#kafka-template.
- Run you Spring boot code after starting Zookeeper, Multiple Brokers (configs at root) and the Consumer.
- Start Zookeeper: Open cmd under `C:\kafka` and execute `.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties`.
- Start Kafka Brokers: Open cmd under `C:\kafka` and execute `.\bin\windows\kafka-server-start.bat .\config\server.properties`.
- Start Kafka Brokers: Open cmd under `C:\kafka` and execute `.\bin\windows\kafka-server-start.bat .\config\server-1.properties`.
- Start Kafka Brokers: Open cmd under `C:\kafka` and execute `.\bin\windows\kafka-server-start.bat .\config\server-2.properties`.
- Instantiate Console Consumer: `.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic library-events`.
- POST request to the controller `/v1/libraryevent/async`. Refer thunder-collection under resources.
- Values returned by the listener.

```
acks = -1
batch.size = 16384
bootstrap.servers = [localhost:9092, localhost:9093, localhost:9094]
buffer.memory = 33554432
client.dns.lookup = use_all_dns_ips
client.id = producer-1
compression.type = none
connections.max.idle.ms = 540000
delivery.timeout.ms = 120000
enable.idempotence = true
interceptor.classes = []
key.serializer = class org.apache.kafka.common.serialization.IntegerSerializer
linger.ms = 0
max.block.ms = 60000
max.in.flight.requests.per.connection = 5
max.request.size = 1048576
metadata.max.age.ms = 300000
metadata.max.idle.ms = 300000
metric.reporters = []
metrics.num.samples = 2
metrics.recording.level = INFO
metrics.sample.window.ms = 30000
partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
receive.buffer.bytes = 32768
reconnect.backoff.max.ms = 1000
reconnect.backoff.ms = 50
request.timeout.ms = 30000
retries = 2147483647
retry.backoff.ms = 100
sasl.client.callback.handler.class = null
sasl.jaas.config = null
sasl.kerberos.kinit.cmd = /usr/bin/kinit
sasl.kerberos.min.time.before.relogin = 60000
sasl.kerberos.service.name = null
sasl.kerberos.ticket.renew.jitter = 0.05
sasl.kerberos.ticket.renew.window.factor = 0.8
sasl.login.callback.handler.class = null
sasl.login.class = null
sasl.login.refresh.buffer.seconds = 300
sasl.login.refresh.min.period.seconds = 60
sasl.login.refresh.window.factor = 0.8
sasl.login.refresh.window.jitter = 0.05
sasl.mechanism = GSSAPI
security.protocol = PLAINTEXT
security.providers = null
send.buffer.bytes = 131072
socket.connection.setup.timeout.max.ms = 30000
socket.connection.setup.timeout.ms = 10000
ssl.cipher.suites = null
ssl.enabled.protocols = [TLSv1.2, TLSv1.3]
ssl.endpoint.identification.algorithm = https
ssl.engine.factory.class = null
ssl.key.password = null
ssl.keymanager.algorithm = SunX509
ssl.keystore.certificate.chain = null
ssl.keystore.key = null
ssl.keystore.location = null
ssl.keystore.password = null
ssl.keystore.type = JKS
ssl.protocol = TLSv1.3
ssl.provider = null
ssl.secure.random.implementation = null
ssl.trustmanager.algorithm = PKIX
ssl.truststore.certificates = null
ssl.truststore.location = null
ssl.truststore.password = null
ssl.truststore.type = JKS
transaction.timeout.ms = 60000
transactional.id = null
value.serializer = class org.apache.kafka.common.serialization.StringSerializer
```

## Producer Approach #2 (Sync with timeout)

- `KafkaTemplate` is created under `producer.LibraryEventsProducer.sendLibraryEventSync()`.
- `KafkaTemplate.sendDefault().get()` is overloaded and is sync in nature. We can configure the timeout and pass it inside `.get()` as parameter.
- All the run commands remain the same as above.
- POST request to the controller `/v1/libraryevent/sync`. Refer thunder-collection under resources.

## Producer Approach #3 (Specific Topic with ProducerRecord and Headers)

- Single instance of `KafkaTemplate.send()` can be used to send messages to multiple individual topics.
- `ProducerRecord()` can be used to configure topics, key, value, partitions, headers and timestamp.
- `KafkaTemplate.send()` accepts `ProducerRecord()` and is used under `producer.LibraryEventsProducer.sendLibraryEventTopic()`.
- All the run commands remain the same as above.
- POST request to the controller `/v1/libraryevent/topic`. Refer thunder-collection under resources.
- Event Metadata: `KafkaHeaders()` are only applicable using `ProducerRecord()`. Refer `producer.LibraryEventsProducer.buildProducerRecord()` for more.
