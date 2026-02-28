package com.quietusai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.quietusai.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class QuietusBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuietusBackendApplication.class, args);
    }
}
