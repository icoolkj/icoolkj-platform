package com.icoolkj.platform.system.post.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.framework.mybatis.AuditFieldHelper;
import com.icoolkj.platform.system.post.convert.PostConvert;
import com.icoolkj.platform.system.post.domain.PostEntity;
import com.icoolkj.platform.system.post.dto.PostCreateRequest;
import com.icoolkj.platform.system.post.dto.PostPageQuery;
import com.icoolkj.platform.system.post.dto.PostUpdateRequest;
import com.icoolkj.platform.system.post.mapper.PostMapper;
import com.icoolkj.platform.system.post.service.PostService;
import com.icoolkj.platform.system.post.vo.PostVO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/** 岗位表服务实现 */
@Service
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    public PostServiceImpl(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @Override
    public PageResult<PostVO> page(PostPageQuery query) {
        long total = postMapper.countByQuery(query);
        if (total == 0) {
            return PageResult.of(Collections.emptyList(), 0L, query.getPageNo(), query.getPageSize());
        }
        List<PostEntity> list = postMapper.selectPage(query);
        if (CollectionUtils.isEmpty(list)) {
            return PageResult.of(Collections.emptyList(), total, query.getPageNo(), query.getPageSize());
        }
        return PageResult.of(list.stream().map(PostConvert::toVo).toList(), total, query.getPageNo(), query.getPageSize());
    }

    @Override
    public PostVO getById(String id) {
        PostEntity entity = postMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.post");
        }
        return PostConvert.toVo(entity);
    }

    @Override
    public String create(PostCreateRequest request) {
        assertPostCodeAvailable(request.getPostCode(), null);
        PostEntity entity = PostConvert.toEntity(request);
        entity.setId(IdGeneratorUtils.nextIdStr());
        postMapper.insert(entity);
        return String.valueOf(entity.getId());
    }

    @Override
    public void update(PostUpdateRequest request) {
        PostEntity entity = postMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.post");
        }
        assertPostCodeAvailable(request.getPostCode(), entity.getId());
        PostConvert.copy(request, entity);
        postMapper.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        PostEntity entity = postMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("error.entity.not_found", "entity.name.post");
        }
        AuditFieldHelper.markDeleted(entity);
        postMapper.updateById(entity);
    }

    private void assertPostCodeAvailable(String postCode, String excludeId) {
        if (!StringUtils.hasText(postCode)) {
            return;
        }
        PostEntity query = new PostEntity();
        query.setPostCode(postCode.trim());
        List<PostEntity> list = postMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        boolean duplicate = list.stream().anyMatch(item -> excludeId == null || !excludeId.equals(item.getId()));
        if (duplicate) {
            throw new BusinessException("error.post.code_exists");
        }
    }
}
