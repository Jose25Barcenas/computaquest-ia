package com.computaquest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class ComputaQuestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComputaQuestApplication.class, args);
    }
}
