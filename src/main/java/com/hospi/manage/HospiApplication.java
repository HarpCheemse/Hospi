package com.hospi.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Entry point for the Hospi hotel management Spring Boot application. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class HospiApplication {
    /**
     * Launch the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(HospiApplication.class, args);
    }
}