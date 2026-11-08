package com.icoolkj.platform.framework.config;

import com.icoolkj.platform.framework.aspect.DataScopeAspect;
import com.icoolkj.platform.framework.aspect.DataScopePointcut;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class DataScopeAopConfiguration {

    @Bean
    public Advisor dataScopeAdvisor(DataScopeAspect dataScopeAspect) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(new DataScopePointcut(), dataScopeAspect);
        advisor.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return advisor;
    }
}
