package org.example.kgservice;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class KgServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KgServiceApplication.class, args);
    }

}
