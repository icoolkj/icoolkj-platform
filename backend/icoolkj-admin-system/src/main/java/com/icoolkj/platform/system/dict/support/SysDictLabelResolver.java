package com.icoolkj.platform.system.dict.support;

import com.icoolkj.platform.common.spi.DictLabelResolver;
import com.icoolkj.platform.system.dict.service.DictDataService;
import com.icoolkj.platform.system.dict.vo.DictDataVO;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/** 从运行态字典（tenant + 平台）解析 dict_label */
@Component
public class SysDictLabelResolver implements DictLabelResolver {

    private final DictDataService dictDataService;

    public SysDictLabelResolver(DictDataService dictDataService) {
        this.dictDataService = dictDataService;
    }

    @Override
    public String resolveLabel(String dictType, String dictValue) {
        if (!StringUtils.hasText(dictType) || !StringUtils.hasText(dictValue)) {
            return null;
        }
        List<DictDataVO> items = dictDataService.listByDictType(dictType.trim());
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        String value = dictValue.trim();
        for (DictDataVO item : items) {
            if (item != null && value.equals(item.getValue()) && StringUtils.hasText(item.getLabel())) {
                return item.getLabel().trim();
            }
        }
        return null;
    }
}
