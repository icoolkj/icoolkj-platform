package com.icoolkj.platform.framework.config;

import com.icoolkj.platform.common.jackson.FlexibleLocalDateTimeDeserializer;
import com.icoolkj.platform.common.jackson.FlexibleLocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class JacksonAutoConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeJacksonCustomizer() {
        return builder -> {
            builder.serializerByType(LocalDateTime.class, new FlexibleLocalDateTimeSerializer());
            builder.deserializerByType(LocalDateTime.class, new FlexibleLocalDateTimeDeserializer());
        };
    }
}
