package com.shishodia.kafka.libraryeventsconsumer.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import lombok.extern.slf4j.Slf4j;

/** UNCOMMENT TO USE MANUAL ACK */
//@Component
@Slf4j
public class LibraryEventsConsumerManualOffsets implements AcknowledgingMessageListener<Integer, String> {
    
    /**
     * While producing we are using ProducerRecord.
     * While consuming we use same data type in ConsumerRecord i.e. <Integer, String>
     * KafkaListener will provide acknowledgement only when the records are read.
     */
    @Override
    @KafkaListener(topics = {"library-events"})
    public void onMessage(ConsumerRecord<Integer, String> consumerRecord, Acknowledgment acknowledgment) {
        log.info("ConsumerRecord: {}", consumerRecord);
        acknowledgment.acknowledge();
    }
    
}
