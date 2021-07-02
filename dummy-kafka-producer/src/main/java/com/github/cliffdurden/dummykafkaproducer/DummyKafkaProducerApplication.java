package com.github.cliffdurden.dummykafkaproducer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

@Slf4j
@SpringBootApplication
public class DummyKafkaProducerApplication implements CommandLineRunner {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String outputTopic;

    public DummyKafkaProducerApplication(@Value("${app.output.topic}") String outputTopic,
                                         KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.outputTopic = outputTopic;
    }

    public static void main(String[] args) {
        SpringApplication.run(DummyKafkaProducerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        long l = 0;
        while (true) {
            String key = String.valueOf(l % 100);
            String value = UUID.randomUUID().toString();
//            log.trace("key: {}, val: {}", key, value);
            kafkaTemplate.send(outputTopic, key, value);
            l++;
            if (l % 100_000_000 == 0) {
                log.info("------- one million -------");
            }
        }
    }
}
