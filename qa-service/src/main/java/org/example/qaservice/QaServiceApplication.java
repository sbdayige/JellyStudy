package org.example.qaservice;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableDubbo
@ComponentScan(basePackages = {
        "org.example.qaservice",
        "org.example.kgservice",
        "org.example.aiservice"
})
public class QaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QaServiceApplication.class, args);
    }

}
