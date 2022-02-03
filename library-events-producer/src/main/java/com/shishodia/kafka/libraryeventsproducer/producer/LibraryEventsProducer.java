package com.shishodia.kafka.libraryeventsproducer.producer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shishodia.kafka.libraryeventsproducer.domain.LibraryEvent;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventsProducer {

    /**
     * Integer and String type as the key is Integer and payload is String.
     * Refer `spring.kafka.producer.key-serializer/value-serializer` in application.properties.
     * (Key) LibraryEvent.libraryEventId is an integer.
     * (Payload) LibraryEvent.book is a string value (JSON).
     */
    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    /** For JSON conveersion of payload. */
    @Autowired
    ObjectMapper objectMapper;

    /** Async publish */
    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent); // JSON

        /** 
         * Set the default topic using `spring.kafka.template.default-topic` in application.properties. 
         * ListenableFuture sends the records to topic when batch is full or linger.ms value is met.
         */
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);

        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            /** Executed when publish is unsuccessful. */ 
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            /**
             * Executed when publish is successful.
             * `result` returns data about the partitions, topic, etc.
             */
            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
        
    }

    /** Async publish but with specific topic and ProducerRecord */
    public void sendLibraryEventTopic(LibraryEvent libraryEvent) throws JsonProcessingException {

        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent); // JSON

        /** 
         * `ProducerRecord()` can be used to configure topics, key, value, partitions, headers and timestamp.
         * This can then be passed inside `KafkaTemplate.send()`.
         */
        String topic = "library-events";
        ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, topic);
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);

        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            /** Executed when publish is unsuccessful. */ 
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            /**
             * Executed when publish is successful.
             * `result` returns data about the partitions, topic, etc.
             */
            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
        
    }

    /** Sync publish with timeout */
    public SendResult<Integer, String> sendLibraryEventSync(LibraryEvent libraryEvent) throws Exception {

        Integer key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent); // JSON
        
        /** Note that Listener is not returned and .get() is used. */
        SendResult<Integer, String> sendResult = null;
        try {
            /** It will timeout in 1 second if the response is not received. */
            sendResult = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            log.error("ExecutionException/InterruptedException/TimeoutException sending the message, exception is {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception sending the message, exception is {}", e.getMessage());
            throw e;
        }
        return sendResult;
    }

    /** `ProducerRecord()` can be used to configure topics, key, value, partitions, headers and timestamp. */
    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topic) {
        /** 
         * `ProducerRecord()` has many overloaded constructors. 
         * We are using ProducerRecord<T>(topic, partition, key, value, headers)
        */
        List<Header> recordHeaders = List.of(
            new RecordHeader("event-source", "barcode-scanner".getBytes()),
            new RecordHeader("event-source", "barcode-scanner".getBytes())
        );
        return new ProducerRecord<Integer,String>(topic, null, key, value, recordHeaders);
    }
    
    private void handleFailure(Integer key, String value, Throwable ex) {
        log.error("Error sending the message, exception is {}", ex.getMessage());
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error on failure: {}", throwable.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully for the key: {}, value: {}, partition: {}", key, value, result.getRecordMetadata().partition());
    }
    
}
