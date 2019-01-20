package org.services.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TsServiceTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TsServiceTestApplication.class, args);
    }
}
