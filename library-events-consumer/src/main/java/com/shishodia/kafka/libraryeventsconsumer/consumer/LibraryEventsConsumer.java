package com.shishodia.kafka.libraryeventsconsumer.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventsConsumer {
    
    /**
     * KafkaListener will poll multiple records but will log them one by one.
     * While producing we are using ProducerRecord.
     * While consuming we use same data type in ConsumerRecord i.e. <Integer, String>
     */
    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord) {
        log.info("ConsumerRecord: {}", consumerRecord);
    }

}
