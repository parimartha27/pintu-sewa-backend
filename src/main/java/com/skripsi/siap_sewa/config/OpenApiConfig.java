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
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("SIAP SEWA API")
                        .description("API documentation for SIAP SEWA application")
                        .version("1.0"));
    }

    @Bean
    public OpenAPI openAPI(ServletContext servletContext) {
        Server server = new Server().url(servletContext.getContextPath());
        return new OpenAPI()
                .servers(List.of(server));
    }
}
