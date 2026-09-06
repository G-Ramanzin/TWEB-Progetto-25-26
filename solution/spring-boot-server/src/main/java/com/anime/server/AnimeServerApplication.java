package com.anime.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Spring Boot server.
 * <p>
 * This server is responsible for the <b>static</b> subset of the anime
 * dataset (anime details, characters, people and their relationships),
 * stored in PostgreSQL. It exposes a documented REST API consumed by
 * the central Express gateway.
 */
@SpringBootApplication
public class AnimeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnimeServerApplication.class, args);
    }
}
