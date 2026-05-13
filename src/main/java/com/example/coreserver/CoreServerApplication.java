package com.example.coreserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableJpaRepositories
@EnableWebSocket
//@EnableScheduling
public class CoreServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoreServerApplication.class, args);
    }
}
