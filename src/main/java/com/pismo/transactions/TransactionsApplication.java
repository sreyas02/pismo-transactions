package com.pismo.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 * Spring Boot auto-discovers all @Component, @Service, @Repository, @RestController
 * beans under this package and wires them via constructor injection (DIP).
 */
@SpringBootApplication
public class TransactionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionsApplication.class, args);
    }
}
