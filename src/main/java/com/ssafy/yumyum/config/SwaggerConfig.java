package com.ssafy.yumyum.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "YumYumCoach API",
        description = "YumYumCoach REST API documentation. Demo account: demo@yamyam.com / Demo1234!",
        version = "v1"
    ),
    security = @SecurityRequirement(name = "sessionAuth")
)
@SecurityScheme(
    name = "sessionAuth",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.COOKIE,
    paramName = "JSESSIONID",
    description = "Session cookie authentication. Use Auth API login with demo@yamyam.com / Demo1234!, then execute secured requests."
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
