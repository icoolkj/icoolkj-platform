package com.icoolkj.platform.system.config.dto;

import com.icoolkj.platform.common.model.page.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/** 系统参数表分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ConfigPageQuery extends PageQuery {
    private String configName;
    private String configKey;
    private String configValue;
    private String configType;
    private String remark;
    /** 按键前缀过滤，如 security.login. → LIKE 'security.login.%' */
    private String configKeyPrefix;
    /** 逗号分隔前缀列表；命中任一前缀的键排除（用于「其他」分类） */
    private String configKeyExcludePrefixes;
    /** 由服务层解析 {@link #configKeyExcludePrefixes} 后供 Mapper 使用 */
    private List<String> configKeyExcludePrefixList;
}
