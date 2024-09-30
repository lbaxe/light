package com.light.mapper.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.light.**.mapper")
public class MapperConfig {

}
