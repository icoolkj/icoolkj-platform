package com.icoolkj.platform.framework.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Mapper 统一扫描（{@code com.icoolkj.**.mapper}）。
 * <p>
 * 仅扫描各模块 {@code *.mapper} 包下的接口，避免误注册 Service 等同名接口。
 */
@Configuration
@MapperScan("com.icoolkj.platform.**.mapper")
public class MybatisApplicationConfig {
}
