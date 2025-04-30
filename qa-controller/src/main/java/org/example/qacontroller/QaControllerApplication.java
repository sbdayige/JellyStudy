package org.example.qacontroller;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@SpringBootApplication
public class QaControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(QaControllerApplication.class, args);
    }

}
