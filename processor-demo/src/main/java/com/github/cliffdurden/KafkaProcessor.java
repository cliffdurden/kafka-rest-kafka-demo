package com.github.cliffdurden;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.*;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

@Slf4j
@Service
public class KafkaProcessor implements BiConsumer<List<ConsumerRecord<String, String>>, Acknowledgment> {

    private final WebClient webClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String outputTopic;

    public KafkaProcessor(WebClient webClient,
                          KafkaTemplate<String, String> kafkaTemplate,
                          @Value("${app.output.topic}") String outputTopic) {
        this.webClient = webClient;
        this.kafkaTemplate = kafkaTemplate;
        this.outputTopic = outputTopic;
    }

    @KafkaListener(topics = "${app.input.topic}")
    public void accept(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        try {
            Boolean isSuccess = Flux.fromIterable(records)
                    .flatMap(incomingRecord -> webClient.get()
                            .uri("/data/{key}", incomingRecord.key())
                            .retrieve()
                            .bodyToFlux(String.class)
                            .map(result -> incomingRecord.value() + "/" + result))
                    .map(downStreamMessage -> kafkaTemplate.send(outputTopic, downStreamMessage))
                    .map(this::getSyncResult)
                    .blockLast(Duration.ofSeconds(10));

            if (isSuccess != null && isSuccess) {
                ack.acknowledge();
            }
        } catch (Exception e) {
            log.error("Can not process batch.", e);
        }
    }

    @NotNull
    private Boolean getSyncResult(ListenableFuture<SendResult<String, String>> sendResult) {
        try {
            sendResult.get(); // block until send data to kafka
            return Boolean.TRUE;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
