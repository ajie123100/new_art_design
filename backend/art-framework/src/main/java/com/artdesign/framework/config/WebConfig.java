package com.artdesign.framework.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    private final FileConfig fileConfig;

    public WebConfig(FileConfig fileConfig) {
        this.fileConfig = fileConfig;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadRoot = new File(fileConfig.getPath()).getAbsoluteFile();
        registry.addResourceHandler("/file/**")
                .addResourceLocations(ensureTrailingSlash(new File(uploadRoot, "file").toURI().toString()));
    }

    private String ensureTrailingSlash(String location) {
        return location.endsWith("/") ? location : location + "/";
    }
}
