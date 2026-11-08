package com.icoolkj.platform.system.loginlog.convert;

import com.icoolkj.platform.system.loginlog.domain.LoginLogEntity;
import com.icoolkj.platform.system.loginlog.vo.LoginLogVO;

/** 登录日志表对象转换（只读查询） */
public final class LoginLogConvert {

    private LoginLogConvert() {
    }

    public static LoginLogVO toVo(LoginLogEntity entity) {
        if (entity == null) {
            return null;
        }
        LoginLogVO vo = new LoginLogVO();
        vo.setId(entity.getId());
        vo.setUsername(entity.getUsername());
        vo.setLoginType(entity.getLoginType());
        vo.setLoginStatus(entity.getLoginStatus());
        vo.setMessage(entity.getMessage());
        vo.setIp(entity.getIp());
        vo.setLocation(entity.getLocation());
        vo.setUserAgent(entity.getUserAgent());
        vo.setBrowser(entity.getBrowser());
        vo.setOs(entity.getOs());
        vo.setLoginAt(entity.getLoginAt());
        return vo;
    }
}
