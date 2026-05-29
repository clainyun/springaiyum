package com.ssafy.yumyum.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "YumYumCoach API",
        description = "YumYumCoach REST API 문서",
        version = "v1"
    )
)
public class SwaggerConfig {

    @Bean
    GroupedOpenApi yumyumApi() {
        return GroupedOpenApi.builder()
            .group("yumyum-api")
            .pathsToMatch("/api/v1/**", "/batch/**")
            .build();
    }
}
