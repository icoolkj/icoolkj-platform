package com.icoolkj.platform.infra.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.tenant.TenantQueryScope;
import com.icoolkj.platform.infra.convert.ThirdPartyCallLogConvert;
import com.icoolkj.platform.infra.domain.ThirdPartyCallLogEntity;
import com.icoolkj.platform.infra.dto.ThirdPartyCallLogPageQuery;
import com.icoolkj.platform.infra.mapper.ThirdPartyCallLogMapper;
import com.icoolkj.platform.infra.service.ThirdPartyCallLogService;
import com.icoolkj.platform.infra.vo.ThirdPartyCallLogVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/** 第三方调用日志服务实现 */
@Service
public class ThirdPartyCallLogServiceImpl implements ThirdPartyCallLogService {

    private final ThirdPartyCallLogMapper thirdPartyCallLogMapper;

    public ThirdPartyCallLogServiceImpl(ThirdPartyCallLogMapper thirdPartyCallLogMapper) {
        this.thirdPartyCallLogMapper = thirdPartyCallLogMapper;
    }

    @Override
    public PageResult<ThirdPartyCallLogVO> page(ThirdPartyCallLogPageQuery query) {
        TenantQueryScope.applyPageQuery(query);
        long total = thirdPartyCallLogMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<ThirdPartyCallLogEntity> list = thirdPartyCallLogMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        List<ThirdPartyCallLogVO> rows = list.stream().map(ThirdPartyCallLogConvert::toVo).toList();
        return PageResult.of(rows, total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public ThirdPartyCallLogVO getById(String id) {
        ThirdPartyCallLogEntity entity = thirdPartyCallLogMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.third_party_call_log");
        }
        TenantQueryScope.assertReadable(entity.getTenantId());
        return ThirdPartyCallLogConvert.toVo(entity);
    }

}
