package com.artdesign.framework.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system")
                .pathsToMatch("/api/user/**", "/api/role/**", "/api/dept/**", "/api/menu/**",
                        "/api/dict/**", "/api/config/**", "/api/notice/**", "/api/post/**", "/api/file/**")
                .build();
    }

    @Bean
    public GroupedOpenApi monitorApi() {
        return GroupedOpenApi.builder()
                .group("monitor")
                .pathsToMatch("/api/monitor/**", "/api/job/**", "/api/jobLog/**", "/api/logininfor/**", "/api/operlog/**")
                .build();
    }
}
