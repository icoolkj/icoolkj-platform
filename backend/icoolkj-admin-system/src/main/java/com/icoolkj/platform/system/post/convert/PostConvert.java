package com.icoolkj.platform.system.post.convert;

import com.icoolkj.platform.framework.tenant.TenantFieldHelper;
import com.icoolkj.platform.system.post.domain.PostEntity;
import com.icoolkj.platform.system.post.dto.PostCreateRequest;
import com.icoolkj.platform.system.post.dto.PostUpdateRequest;
import com.icoolkj.platform.system.post.vo.PostVO;

/** 岗位表对象转换 */
public final class PostConvert {

    private PostConvert() {
    }

    public static PostVO toVo(PostEntity entity) {
        if (entity == null) {
            return null;
        }
        PostVO vo = new PostVO();
        vo.setId(entity.getId());
        vo.setPostName(entity.getPostName());
        vo.setPostCode(entity.getPostCode());
        vo.setSortNo(entity.getSortNo());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static PostEntity toEntity(PostCreateRequest request) {
        PostEntity entity = new PostEntity();
        entity.setTenantId(TenantFieldHelper.resolveTenantId(null));
        entity.setPostName(request.getPostName());
        entity.setPostCode(request.getPostCode());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
        return entity;
    }

    public static void copy(PostUpdateRequest request, PostEntity entity) {
        entity.setPostName(request.getPostName());
        entity.setPostCode(request.getPostCode());
        entity.setSortNo(request.getSortNo());
        entity.setStatus(request.getStatus());
        entity.setRemark(request.getRemark());
    }
}
