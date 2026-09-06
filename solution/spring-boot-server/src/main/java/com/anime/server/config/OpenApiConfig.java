package com.anime.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger/OpenAPI metadata shown in the generated documentation. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI animeOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Anime Explorer - Spring Boot API")
                .description("REST API for the static anime data subset "
                        + "(details, characters, people) stored in PostgreSQL. "
                        + "Consumed by the central Express gateway.")
                .version("1.0.0"));
    }
}
