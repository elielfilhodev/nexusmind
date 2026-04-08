package com.nexusmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "com.nexusmind.infrastructure")
@EnableCaching
public class NexusMindApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusMindApplication.class, args);
    }
}
