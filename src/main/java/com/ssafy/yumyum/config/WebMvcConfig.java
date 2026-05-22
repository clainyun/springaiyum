package com.ssafy.yumyum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Force the legacy Ant-style matcher so springdoc can register Swagger UI mappings safely.
        configurer.setPathMatcher(new AntPathMatcher());
        configurer.setPatternParser(null);
    }
}
