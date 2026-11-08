package com.icoolkj.platform.infra.vo;

import lombok.Data;

@Data
public class DatabaseBackupFileVO {

    /** Same as fileName; used by GenCrudPage row identity. */
    private String id;
    private String fileName;
    private long sizeBytes;
    /** Human-readable size for table display. */
    private String sizeText;
    private String modifiedAt;
    private boolean compressed;
}
