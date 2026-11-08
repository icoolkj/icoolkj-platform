package com.icoolkj.platform.system.iprule.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.iprule.convert.IpAccessRuleConvert;
import com.icoolkj.platform.system.iprule.domain.IpAccessRuleEntity;
import com.icoolkj.platform.system.iprule.dto.IpAccessRuleCreateRequest;
import com.icoolkj.platform.system.iprule.dto.IpAccessRulePageQuery;
import com.icoolkj.platform.system.iprule.dto.IpAccessRuleUpdateRequest;
import com.icoolkj.platform.system.iprule.mapper.IpAccessRuleMapper;
import com.icoolkj.platform.system.iprule.service.IpAccessRuleService;
import com.icoolkj.platform.system.iprule.vo.IpAccessRuleVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/** IP访问规则服务实现 */
@Service
public class IpAccessRuleServiceImpl implements IpAccessRuleService {

    private final IpAccessRuleMapper ipAccessRuleMapper;

    public IpAccessRuleServiceImpl(IpAccessRuleMapper ipAccessRuleMapper) {
        this.ipAccessRuleMapper = ipAccessRuleMapper;
    }

    @Override
    public PageResult<IpAccessRuleVO> page(IpAccessRulePageQuery query) {
        long total = ipAccessRuleMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<IpAccessRuleEntity> list = ipAccessRuleMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        return PageResult.of(list.stream().map(IpAccessRuleConvert::toVo).toList(), total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public IpAccessRuleVO getById(String id) {
        IpAccessRuleEntity entity = ipAccessRuleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.ip_rule");
        }
        return IpAccessRuleConvert.toVo(entity);
    }

    @Override
    public String create(IpAccessRuleCreateRequest request) {
        IpAccessRuleEntity entity = IpAccessRuleConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        ipAccessRuleMapper.insert(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public void update(IpAccessRuleUpdateRequest request) {
        IpAccessRuleEntity entity = ipAccessRuleMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.ip_rule");
        }
        IpAccessRuleConvert.copy(request, entity);
        ipAccessRuleMapper.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        IpAccessRuleEntity entity = ipAccessRuleMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.ip_rule");
        }
        AuditFieldHelper.markDeleted(entity);
        ipAccessRuleMapper.updateById(entity);
    }
}
