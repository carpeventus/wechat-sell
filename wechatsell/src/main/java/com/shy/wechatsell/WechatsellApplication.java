package com.shy.wechatsell;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan(basePackages = "com.shy.wechatsell.mapper")
@EnableCaching
public class WechatsellApplication {
    public static void main(String[] args) {
        SpringApplication.run(WechatsellApplication.class, args);
    }
}
