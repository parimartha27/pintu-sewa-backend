package com.skripsi.siap_sewa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(ServletContext servletContext) {
        Server server = new Server().url(servletContext.getContextPath());

        return new OpenAPI()
                .info(new Info()
                        .title("PINTU SEWA API")
                        .description("API documentation for PINTU SEWA application")
                        .version("1.0"))
                .servers(List.of(server));
    }
}