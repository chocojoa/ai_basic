package com.basic.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.basic.project.mapper")
public class BasicProjectApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BasicProjectApplication.class, args);
    }
}