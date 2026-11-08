package com.icoolkj.platform.infra.provider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "icoolkj.provider")
public class ProviderProperties {

    private Sms sms = new Sms();
    private Mail mail = new Mail();
    private Ocr ocr = new Ocr();
    private Sign sign = new Sign();

    @Data
    public static class Sms {

        private boolean enabled = false;
        private String defaultChannel = "aliyun";
        private Aliyun aliyun = new Aliyun();
        private Huawei huawei = new Huawei();
    }

    @Data
    public static class Aliyun {

        private String accessKeyId;
        private String accessKeySecret;
        private String signName;
    }

    @Data
    public static class Huawei {

        private String appKey;
        private String appSecret;
        private String sender;
    }

    @Data
    public static class Mail {

        private boolean enabled = false;
        private String host;
        private int port = 465;
        private String username;
        private String password;
        private String protocol = "smtps";
        private String defaultFrom;
    }

    @Data
    public static class Ocr {

        private boolean enabled = false;
        private String defaultChannel = "baidu";
        private Baidu baidu = new Baidu();
    }

    @Data
    public static class Baidu {

        private String apiKey;
        private String secretKey;
    }

    @Data
    public static class Sign {

        private boolean enabled = false;
        private String defaultChannel = "esign";
        private Esign esign = new Esign();
    }

    @Data
    public static class Esign {

        private String appId;
        private String appSecret;
        private String host;
    }
}
