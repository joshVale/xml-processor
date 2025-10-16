package com.example.app2batchprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class App2BatchProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(App2BatchProcessorApplication.class, args);
    }

}
