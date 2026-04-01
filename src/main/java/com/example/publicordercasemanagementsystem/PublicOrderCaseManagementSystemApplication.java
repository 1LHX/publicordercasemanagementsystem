package com.example.publicordercasemanagementsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.publicordercasemanagementsystem.mapper")
public class PublicOrderCaseManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublicOrderCaseManagementSystemApplication.class, args);
    }

}
