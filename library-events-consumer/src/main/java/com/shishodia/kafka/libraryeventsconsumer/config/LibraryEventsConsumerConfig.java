package com.shishodia.kafka.libraryeventsconsumer.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.shishodia.kafka.libraryeventsconsumer.service.LibraryEventsService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableKafka
@Slf4j
public class LibraryEventsConsumerConfig {

    @Autowired
    private LibraryEventsService libraryEventsService;

    /** 
     * For the manual acknowledgement to work, you need to set `factory.getContainerProperties().setAckMode(AckMode.MANUAL)` below.
     * Also, you will need a seperate consumer included inside `consumer.LibraryEventsConsumerManualOffsets`.
     */
    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        log.info("Custom kafka listener container settings.");
        configurer.configure(factory, kafkaConsumerFactory);

        /** UNCOMMENT TO USE MANUAL ACK */
        // import org.springframework.kafka.listener.ContainerProperties.AckMode;
        // factory.getContainerProperties().setAckMode(AckMode.MANUAL);

        /** UNCOMMENT TO HANDLE CUSTOM ERRORS */
        // factory.setErrorHandler(((thrownException, data) -> {
        //     // You can persist the failed records as well.
        //     log.info("Exception in consumerConfig is {} and the record is {}", thrownException.getMessage(), data);
        // }));

        /** RETRY: Use the following to configure retry template. */
        factory.setRetryTemplate(customRetryTemplate());

        /** RECOVERY: If the retries get exhausted, set up a recovery logic.  
         * Input to setRecoveryCallback() is a RetryContext [Retry is input to Recovery].
        */
        factory.setRecoveryCallback((context) -> {
            if (context.getLastThrowable().getCause() instanceof RecoverableDataAccessException) {
                // Invoke recovery logic.
                log.info("Inside the recoverable logic");

                /**
                 * The retry context comes with several key value pairs.
                 * One of the key value pairs is the failed record itself in JSON string form.
                 * The record attribute name (key) is `record`. 
                 */
                Arrays.asList(context.attributeNames()).forEach(
                    attributeName -> {
                        log.info("Attribute name is : {}", attributeName);
                        log.info("Attribute value is : {}", context.getAttribute(attributeName));
                    }
                );
                @SuppressWarnings("unchecked")
                ConsumerRecord<Integer, String> failedConsumerRecord = (ConsumerRecord<Integer, String>) context.getAttribute("record");
                // Now you can push this record back to the `library-events` topic.

                // CAUTION: CIRCULAR LOOP POSSIBLE HENCE COMMENTED.
                // libraryEventsService.handleRecovery(failedConsumerRecord);
            } else {
                log.info("Inside the non-recoverable logic");
                throw new RuntimeException(context.getLastThrowable().getMessage());
            }
            return null;
        });

        return factory;
    }

    private RetryTemplate customRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // You can set your own retry policy.
        retryTemplate.setRetryPolicy(customRetryPolicy());

        // [Exception specific retry policy]
        //retryTemplate.setRetryPolicy(customExceptionSpecificRetryPolicy());
        
        // You can set a backoff policy between retries. Here it is set to 1 second.
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(1000);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        return retryTemplate;
    }

    // This retry policy sets the maximum attempts to 3.
    private RetryPolicy customRetryPolicy() {
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy();
        simpleRetryPolicy.setMaxAttempts(3);
        return simpleRetryPolicy;
    }

    /**
     * This retry policy sets the maximum attempts to 3.
     * IllegalArgumentException will be ignored.
     * RecoverableDataAccessException will be retried.
     */
    private RetryPolicy customExceptionSpecificRetryPolicy() {
        Map<Class<? extends Throwable>, Boolean> exceptionMap = new HashMap<>();
        exceptionMap.put(IllegalArgumentException.class, false);
        exceptionMap.put(RecoverableDataAccessException.class, true);
        SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(3, exceptionMap);
        return simpleRetryPolicy;
    }

}
