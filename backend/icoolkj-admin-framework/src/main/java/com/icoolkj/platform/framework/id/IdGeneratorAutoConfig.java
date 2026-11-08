package com.icoolkj.platform.framework.id;

import com.icoolkj.platform.common.id.IdGenerator;
import com.icoolkj.platform.common.id.SnowflakeIdGenerator;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IdGeneratorProperties.class)
public class IdGeneratorAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public IdGenerator idGenerator(IdGeneratorProperties properties) {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(
                properties.getWorkerId(),
                properties.getDatacenterId()
        );
        IdGeneratorUtils.bind(generator);
        return generator;
    }
}
