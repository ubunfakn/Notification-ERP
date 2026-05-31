package com.notification_hub.notif.config;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        log.info("Model Mapper Bean initialized");
        return new ModelMapper();
    }
}