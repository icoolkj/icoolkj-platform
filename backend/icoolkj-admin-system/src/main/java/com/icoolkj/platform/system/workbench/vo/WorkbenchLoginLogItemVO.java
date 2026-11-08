package com.icoolkj.platform.system.workbench.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 工作台最近登录 */
@Data
public class WorkbenchLoginLogItemVO {
    private String username;
    private String ip;
    private String location;
    private String browser;
    private String os;
    private LocalDateTime loginAt;
}
