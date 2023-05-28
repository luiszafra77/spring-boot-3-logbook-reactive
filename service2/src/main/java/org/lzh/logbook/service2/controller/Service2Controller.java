package org.lzh.logbook.service2.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/service2")
@Slf4j
public class Service2Controller {

  @GetMapping
  public Mono<ResponseEntity<String>> done() {
    log.info("Received");
    return Mono.just("done!").map(ResponseEntity::ok);
  }
}
