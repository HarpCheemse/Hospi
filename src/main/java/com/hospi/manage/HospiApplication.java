package com.hospi.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HospiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HospiApplication.class, args);
    }
}