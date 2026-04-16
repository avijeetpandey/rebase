package com.avijeet.rebase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class RebaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(RebaseApplication.class, args);
    }

}
