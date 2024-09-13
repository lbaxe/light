package com.light;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@MapperScan("com.light.**.mapper")
@ServletComponentScan("com.light")
@SpringBootApplication(scanBasePackages = {"com.light", "com.light.**.*"})
public class LightApplication {
    public static void main(String[] args) {
        SpringApplication.run(LightApplication.class, args);
    }
}
