package com.light;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import cn.hutool.crypto.digest.DigestUtil;

@EnableAsync
@ServletComponentScan
@SpringBootApplication
public class LightApplication {
    public static void main(String[] args) {
        // SpringApplication.run(LightApplication.class, args);
        System.out.println(DigestUtil.md5Hex("e10adc3949ba59abbe56e057f20f883e"));

    }
}
