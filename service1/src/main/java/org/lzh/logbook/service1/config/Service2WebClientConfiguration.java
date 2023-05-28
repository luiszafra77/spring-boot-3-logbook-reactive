package org.lzh.logbook.service1.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class Service2WebClientConfiguration {

  @Bean
  public WebClient service2WebClient(WebClient.Builder builder) {
    return builder.baseUrl("http://localhost:8081").build();
  }
}
