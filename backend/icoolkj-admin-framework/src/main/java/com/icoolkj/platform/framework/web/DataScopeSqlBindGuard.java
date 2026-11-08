package com.icoolkj.platform.framework.web;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
public class DataScopeSqlBindGuard {

    @InitBinder
    public void disallowClientDataScopeSql(WebDataBinder binder) {
        binder.setDisallowedFields("dataScopeSql");
    }
}
