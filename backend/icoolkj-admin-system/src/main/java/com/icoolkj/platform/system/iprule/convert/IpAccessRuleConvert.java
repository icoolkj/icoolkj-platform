package com.icoolkj.platform.system.iprule.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.iprule.domain.IpAccessRuleEntity;
import com.icoolkj.platform.system.iprule.dto.IpAccessRuleCreateRequest;
import com.icoolkj.platform.system.iprule.dto.IpAccessRuleUpdateRequest;
import com.icoolkj.platform.system.iprule.vo.IpAccessRuleVO;

/** IP访问规则对象转换 */
public final class IpAccessRuleConvert {

    private IpAccessRuleConvert() {
    }

    public static IpAccessRuleVO toVo(IpAccessRuleEntity entity) {
        if (entity == null) {
            return null;
        }
        IpAccessRuleVO vo = new IpAccessRuleVO();
        vo.setId(entity.getId());
        vo.setRuleName(entity.getRuleName());
        vo.setIpAddress(entity.getIpAddress());
        vo.setMatchType(entity.getMatchType());
        vo.setRuleType(entity.getRuleType());
        vo.setApplyScope(entity.getApplyScope());
        vo.setPriorityNo(entity.getPriorityNo());
        vo.setStatus(entity.getStatus());
        vo.setStartAt(entity.getStartAt());
        vo.setEndAt(entity.getEndAt());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    public static IpAccessRuleEntity toEntity(IpAccessRuleCreateRequest request) {
        IpAccessRuleEntity entity = new IpAccessRuleEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setRuleName(request.getRuleName());
        entity.setIpAddress(request.getIpAddress());
        entity.setMatchType(request.getMatchType());
        entity.setRuleType(request.getRuleType());
        entity.setApplyScope(request.getApplyScope());
        entity.setPriorityNo(request.getPriorityNo());
        entity.setStatus(request.getStatus());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(IpAccessRuleUpdateRequest request, IpAccessRuleEntity entity) {
        entity.setRuleName(request.getRuleName());
        entity.setIpAddress(request.getIpAddress());
        entity.setMatchType(request.getMatchType());
        entity.setRuleType(request.getRuleType());
        entity.setApplyScope(request.getApplyScope());
        entity.setPriorityNo(request.getPriorityNo());
        entity.setStatus(request.getStatus());
        entity.setStartAt(request.getStartAt());
        entity.setEndAt(request.getEndAt());
        entity.setRemark(request.getRemark());
    }
}
