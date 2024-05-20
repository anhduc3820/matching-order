package com.matchingorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MatchingOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchingOrderApplication.class, args);
    }

}
