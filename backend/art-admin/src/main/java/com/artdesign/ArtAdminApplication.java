package com.artdesign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.artdesign.system.mapper")
@SpringBootApplication
public class ArtAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArtAdminApplication.class, args);
    }
}
