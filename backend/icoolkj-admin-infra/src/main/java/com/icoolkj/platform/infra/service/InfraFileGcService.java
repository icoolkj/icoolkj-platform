package com.icoolkj.platform.infra.service;

import com.icoolkj.platform.infra.vo.InfraFileGcResult;

/** 软删文件物理回收（worker 定时任务调用） */
public interface InfraFileGcService {

    /** 回收 deleted=1 且超过保留期的文件（分批，可多次调用直到返回 processed=0） */
    InfraFileGcResult purgeExpiredBatch();
}
