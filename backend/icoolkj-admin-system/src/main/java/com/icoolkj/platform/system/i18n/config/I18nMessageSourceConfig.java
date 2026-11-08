package com.icoolkj.platform.system.i18n.config;

import com.icoolkj.platform.system.i18n.DatabaseMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class I18nMessageSourceConfig {

    @Bean
    @Primary
    public MessageSource messageSource(DatabaseMessageSource databaseMessageSource) {
        return databaseMessageSource;
    }

    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
        factory.setValidationMessageSource(messageSource);
        return factory;
    }
}
