package com.datapig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.datapig.entity") // Adjust this if needed
@EnableJpaRepositories(basePackages = "com.datapig.repository") // Adjust if needed
@ComponentScan({ "com.datapig.*" })
public class MainApplication implements CommandLineRunner {


    @Override
    public void run(String... args) {

    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
 