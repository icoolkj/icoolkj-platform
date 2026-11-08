package com.icoolkj.platform.generator.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GeneratorRuntimeProperties.class)
public class GeneratorRuntimeConfiguration {
}
