package com.icoolkj.platform.system.dict.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Runtime dict type option for codegen / form builders (read-only). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictTypeOptionVO {

    private String dictType;
    private String dictName;
}
