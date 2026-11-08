package com.icoolkj.platform.system.post.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.post.dto.PostCreateRequest;
import com.icoolkj.platform.system.post.dto.PostPageQuery;
import com.icoolkj.platform.system.post.dto.PostUpdateRequest;
import com.icoolkj.platform.system.post.vo.PostVO;

/** 岗位表服务 */
public interface PostService {

    PageResult<PostVO> page(PostPageQuery query);

    PostVO getById(String id);

    String create(PostCreateRequest request);

    void update(PostUpdateRequest request);

    void deleteById(String id);
}
