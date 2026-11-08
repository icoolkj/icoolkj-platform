package com.icoolkj.platform.generator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Generator defaults from generator.yml.
 */
@Configuration
@PropertySource(value = "classpath:generator.yml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "gen")
public class GenConfig {

    private static String author;
    private static String projectRoot;
    private static String tablePrefix;
    private static String defaultBackendModule;
    private static String defaultApiModule;
    private static String defaultBaseType;

    public void setAuthor(String author) {
        GenConfig.author = author;
    }

    public void setProjectRoot(String projectRoot) {
        GenConfig.projectRoot = projectRoot;
    }

    public void setTablePrefix(String tablePrefix) {
        GenConfig.tablePrefix = tablePrefix;
    }

    public void setDefaultBackendModule(String defaultBackendModule) {
        GenConfig.defaultBackendModule = defaultBackendModule;
    }

    public void setDefaultApiModule(String defaultApiModule) {
        GenConfig.defaultApiModule = defaultApiModule;
    }

    public void setDefaultBaseType(String defaultBaseType) {
        GenConfig.defaultBaseType = defaultBaseType;
    }

    public static String getAuthor() {
        return author == null ? "icoolkj" : author;
    }

    public static String getProjectRoot() {
        return projectRoot == null ? ".." : projectRoot;
    }

    public static String getTablePrefix() {
        return tablePrefix == null ? "sys_,infra_" : tablePrefix;
    }

    public static String getDefaultBackendModule() {
        return defaultBackendModule == null ? "system" : defaultBackendModule;
    }

    public static String getDefaultApiModule() {
        return defaultApiModule == null ? "system" : defaultApiModule;
    }

    public static String getDefaultBaseType() {
        return defaultBaseType == null ? "tenant" : defaultBaseType;
    }
}
