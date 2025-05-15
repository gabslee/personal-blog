package com.personalblog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.personalblog.config.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class PersonalBlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonalBlogApplication.class, args);
    }
} 