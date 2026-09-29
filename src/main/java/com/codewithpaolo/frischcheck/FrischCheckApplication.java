package com.codewithpaolo.frischcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FrischCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrischCheckApplication.class, args);
    }

}
