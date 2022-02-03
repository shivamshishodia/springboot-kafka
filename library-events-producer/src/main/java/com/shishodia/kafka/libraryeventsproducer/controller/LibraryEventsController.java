package com.shishodia.kafka.libraryeventsproducer.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shishodia.kafka.libraryeventsproducer.domain.LibraryEvent;
import com.shishodia.kafka.libraryeventsproducer.domain.LibraryEventType;
import com.shishodia.kafka.libraryeventsproducer.producer.LibraryEventsProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class LibraryEventsController {

    @Autowired
    private LibraryEventsProducer libraryEventsProducer;

    @PostMapping(path = "/v1/libraryevent/async")
    public ResponseEntity<LibraryEvent> postLibraryEventAsync(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException {
        // Invoke Kafka producers
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        libraryEventsProducer.sendLibraryEvent(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PostMapping(path = "/v1/libraryevent/sync")
    public ResponseEntity<LibraryEvent> postLibraryEventSync(@RequestBody LibraryEvent libraryEvent) throws Exception {
        // Invoke Kafka producers
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        SendResult<Integer, String> sendResults = libraryEventsProducer.sendLibraryEventSync(libraryEvent);
        log.info("Sync publish successful {}.", sendResults.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PostMapping(path = "/v1/libraryevent/topic")
    public ResponseEntity<LibraryEvent> postLibraryEventTopic(@RequestBody LibraryEvent libraryEvent) throws Exception {
        // Invoke Kafka producers
        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        libraryEventsProducer.sendLibraryEventTopic(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
    
}
