package org.lzh.logbook.service2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.lzh")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
