package com.shishodia.kafka.libraryeventsconsumer.service;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shishodia.kafka.libraryeventsconsumer.entity.LibraryEvent;
import com.shishodia.kafka.libraryeventsconsumer.jpa.LibraryEventsRepository;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LibraryEventsService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    private LibraryEventsRepository libraryEventsRepository;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonMappingException, JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent: {}", libraryEvent);

        /**
         * We are simulating a failure to check for exception specific retries.
         * If the producer sends event id as 0, then an error will be thrown (which will then be retried).
         */ 
        if (libraryEvent.getLibraryEventId() != null && libraryEvent.getLibraryEventId() == 0) {
            throw new RecoverableDataAccessException("Temporary network issue.");
        }

        switch(libraryEvent.getLibraryEventType()) {
            case NEW:
                // Save ops.
                save(libraryEvent);
                break;
            case UPDATE:
                // Validate and save ops.
                validate(libraryEvent);
                save(libraryEvent);
                break;
            default:
                log.info("Invalid library event type.");
        }
    }

    private void validate(LibraryEvent libraryEvent) {
        if (libraryEvent.getLibraryEventType() == null) {
            throw new IllegalArgumentException("Library event id is missing.");
        }
        Optional<LibraryEvent> libraryEventOptional = libraryEventsRepository.findById(libraryEvent.getLibraryEventId());
        if (!libraryEventOptional.isPresent()) {
            throw new IllegalArgumentException("Not a valid library event id.");
        }
        log.info("Validation successful for the library event: {}", libraryEventOptional.get());
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
        log.info("Successfully saved the library event: {}", libraryEvent);
    }

    /**
     * Method to push the failed record back to `library-event` topic.
     * Basically you are publishing the record again to the topic.
     * SAME CODE AS PRODUCER API.
     */
    public void handleRecovery(ConsumerRecord<Integer, String> consumerRecord) {
        Integer key = consumerRecord.key();
        String value = consumerRecord.value();
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
