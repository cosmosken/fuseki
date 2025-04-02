package com.bu.dong.fuseki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.bu.dong.fuseki"})
public class FusekiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FusekiApplication.class, args);
    }

}