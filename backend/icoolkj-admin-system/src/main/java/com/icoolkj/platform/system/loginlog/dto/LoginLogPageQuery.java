package com.icoolkj.platform.system.loginlog.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** 登录日志表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoginLogPageQuery extends PageQuery {
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
