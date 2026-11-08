package com.icoolkj.platform.generator.util;

import org.apache.velocity.app.Velocity;

import java.util.Properties;

public final class VelocityInitializer {

    private VelocityInitializer() {
    }

    public static void initVelocity() {
        Properties props = new Properties();
        props.setProperty("resource.loader.file.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("resource.default_encoding", "UTF-8");
        Velocity.init(props);
    }
}
