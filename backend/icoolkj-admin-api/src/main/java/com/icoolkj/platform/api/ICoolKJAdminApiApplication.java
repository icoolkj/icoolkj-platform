package com.icoolkj.platform.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.icoolkj.platform")
public class ICoolKJAdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ICoolKJAdminApiApplication.class, args);
    }
}
