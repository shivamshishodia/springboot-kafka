package com.shishodia.kafka.libraryeventsproducer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shishodia.kafka.libraryeventsproducer.domain.LibraryEvent;

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
