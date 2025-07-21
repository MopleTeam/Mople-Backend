package com.mople;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication
public class MopleApplication {

    public static void main(String[] args) {
        SpringApplication.run(MopleApplication.class, args);
    }
}
