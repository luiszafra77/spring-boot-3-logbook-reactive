package org.lzh.logbook.service1.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/service1")
@Slf4j
public class Service1Controller {
  private final WebClient service2WebClient;

  public Service1Controller(WebClient service2WebClient) {
    this.service2WebClient = service2WebClient;
  }

  @GetMapping
  public Mono<ResponseEntity<String>> callToService2() {
    log.info("Service calling to service2...");
    return service2WebClient
        .get()
        .uri("/service2")
        .retrieve()
        .bodyToMono(String.class)
        .map(ResponseEntity::ok);
  }
}
