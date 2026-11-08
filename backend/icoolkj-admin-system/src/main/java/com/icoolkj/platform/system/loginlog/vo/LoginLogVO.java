package com.icoolkj.platform.system.loginlog.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 登录日志表列表/详情 */
@Data
public class LoginLogVO {
    private String id;
    private String username;
    private String loginType;
    private String loginStatus;
    private String message;
    private String ip;
    private String location;
    private String userAgent;
    private String browser;
    private String os;
    private LocalDateTime loginAt;
}
