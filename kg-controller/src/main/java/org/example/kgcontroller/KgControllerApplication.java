package org.example.kgcontroller;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class KgControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KgControllerApplication.class, args);
    }

}
