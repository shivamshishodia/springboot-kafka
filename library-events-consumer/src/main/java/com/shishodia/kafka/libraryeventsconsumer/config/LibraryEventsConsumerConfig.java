package com.shishodia.kafka.libraryeventsconsumer.config;

import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;

@Configuration
@EnableKafka
public class LibraryEventsConsumerConfig {

    /** 
     * For the manual acknowledgement to work, you need to set `factory.getContainerProperties().setAckMode(AckMode.MANUAL)` below.
     * Also, you will need a seperate consumer included inside `consumer.LibraryEventsConsumerManualOffsets`.
     */
    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        /** UNCOMMENT TO USE MANUAL ACK */
        factory.getContainerProperties().setAckMode(AckMode.MANUAL);
        return factory;
    }

}
