package com.shishodia.kafka.libraryeventsconsumer.service;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shishodia.kafka.libraryeventsconsumer.entity.LibraryEvent;
import com.shishodia.kafka.libraryeventsconsumer.jpa.LibraryEventsRepository;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LibraryEventsService {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private LibraryEventsRepository libraryEventsRepository;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonMappingException, JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent: {}", libraryEvent);

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
    
}
