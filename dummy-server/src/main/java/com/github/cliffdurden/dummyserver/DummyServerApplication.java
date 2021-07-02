package com.github.cliffdurden.dummyserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Locale;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Slf4j
@SpringBootApplication
public class DummyServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DummyServerApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions
                .route()
                .GET("/data/{key}", r -> ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(Mono
                                .just(r.pathVariable("key").toLowerCase(Locale.ROOT))
                                .doOnNext(val -> {
                                    if ("0".equals(val)) {
                                        log.info("in: {}", val);
                                    }
                                }), String.class))
                .build();
    }

}
