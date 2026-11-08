package com.icoolkj.platform.system.notice.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.system.notice.dto.NoticeBatchDeleteRequest;
import com.icoolkj.platform.system.notice.dto.NoticeCreateRequest;
import com.icoolkj.platform.system.notice.dto.NoticePageQuery;
import com.icoolkj.platform.system.notice.dto.NoticePublishedQuery;
import com.icoolkj.platform.system.notice.dto.NoticeUpdateRequest;
import com.icoolkj.platform.system.notice.vo.NoticeApproveDetailVO;
import com.icoolkj.platform.system.notice.vo.NoticeListVO;
import com.icoolkj.platform.system.notice.vo.NoticePublishedVO;
import com.icoolkj.platform.system.notice.vo.NoticeVO;
import org.springframework.core.io.Resource;

import java.util.List;

/** 通知公告表服务 */
public interface NoticeService {

    PageResult<NoticeListVO> page(NoticePageQuery query);

    NoticeVO getById(String id);

    List<NoticePublishedVO> listPublished(NoticePublishedQuery query);

    NoticePublishedVO getPublishedById(String id);

    String create(NoticeCreateRequest request);

    void update(NoticeUpdateRequest request);

    void deleteById(String id);

    void batchDelete(NoticeBatchDeleteRequest request);

    /** 有审批历史的单据归档关闭（不可删除） */
    void close(String id);

    /** 审批通过后上架（门户可见） */
    void publish(String id);

    /** 下架（门户不可见，审批状态不变） */
    void unpublish(String id);

    Resource loadContentFile(String fileId);

    /** 公告正文内嵌文件 MIME，用于视频/图片 inline 下载 */
    String resolveContentFileMediaType(String fileId);

    /** 提交公告审批（启动 Flowable 流程） */
    String submitApprove(String id);

    /** 审批中撤回 */
    void withdraw(String id);

    /** 审批页详情 */
    NoticeApproveDetailVO getApproveDetail(String id);
}
