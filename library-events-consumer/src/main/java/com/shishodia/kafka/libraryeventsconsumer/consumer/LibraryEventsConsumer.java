package com.shishodia.kafka.libraryeventsconsumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.shishodia.kafka.libraryeventsconsumer.service.LibraryEventsService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventsConsumer {

    @Autowired
    private LibraryEventsService libraryEventsService;
    
    /**
     * KafkaListener will poll multiple records but will log them one by one.
     * While producing we are using ProducerRecord.
     * While consuming we use same data type in ConsumerRecord i.e. <Integer, String>
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) throws JsonMappingException, JsonProcessingException {
        log.info("ConsumerRecord: {}", consumerRecord);
        libraryEventsService.processLibraryEvent(consumerRecord);
    }

}
