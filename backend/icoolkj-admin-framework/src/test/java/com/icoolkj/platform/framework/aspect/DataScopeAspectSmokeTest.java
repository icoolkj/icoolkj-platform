package com.icoolkj.platform.framework.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icoolkj.platform.common.model.page.PageQuery;
import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.framework.auth.DataPermissionService;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataScopeAspectSmokeTest.Config.class)
class DataScopeAspectSmokeTest {

    @Autowired
    private SampleService sampleService;

    @Test
    void interfaceDataScopeInjectsSqlUnderCglibProxy() {
        assertTrue(AopUtils.isAopProxy(sampleService));
        assertTrue(AopUtils.isCglibProxy(sampleService));

        SamplePageQuery query = new SamplePageQuery();
        sampleService.page(query);

        assertEquals(" AND smoke = 1", query.getDataScopeSql());
    }

    interface SampleService {
        @DataScope(userColumn = "user_id", deptColumn = "")
        void page(SamplePageQuery query);
    }

    static class SampleServiceImpl implements SampleService {
        @Override
        public void page(SamplePageQuery query) {
            // body unused; advisor must inject before invoke
        }
    }

    static class SamplePageQuery extends PageQuery {
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class Config {

        @Bean
        DataPermissionService dataPermissionService() {
            return new DataPermissionService() {
                @Override
                public String buildDataScopeSql(DataScope dataScope) {
                    return " AND smoke = 1";
                }

                @Override
                public boolean isReadable(DataScopeSpec spec, String ownerUserId, String ownerDeptId) {
                    return true;
                }

                @Override
                public void assertReadable(DataScopeSpec spec, String ownerUserId, String ownerDeptId) {
                }
            };
        }

        @Bean
        DataScopeAspect dataScopeAspect(DataPermissionService dataPermissionService) {
            return new DataScopeAspect(dataPermissionService);
        }

        @Bean
        Advisor dataScopeAdvisor(DataScopeAspect dataScopeAspect) {
            DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(new DataScopePointcut(), dataScopeAspect);
            advisor.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
            return advisor;
        }

        @Bean
        SampleService sampleService() {
            return new SampleServiceImpl();
        }
    }
}
