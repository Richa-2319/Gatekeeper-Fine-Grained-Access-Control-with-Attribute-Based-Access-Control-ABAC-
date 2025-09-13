// GatekeeperApplication.java
package com.gatekeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableKafka
@EnableAsync
public class GatekeeperApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatekeeperApplication.class, args);
    }
}