package com.icoolkj.platform.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.icoolkj.platform")
@EnableScheduling
public class ICoolKJAdminWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ICoolKJAdminWorkerApplication.class, args);
    }
}
