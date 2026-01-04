package com.gderuki.taskr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskrApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskrApplication.class, args);
    }

}
