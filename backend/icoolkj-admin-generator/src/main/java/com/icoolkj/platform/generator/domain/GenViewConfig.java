package com.icoolkj.platform.generator.domain;

import lombok.Data;

import java.time.LocalDateTime;

/** gen_view_config — low-code CRUD UI layout JSON per table. */
@Data
public class GenViewConfig {

    private String id;
    private String tableId;
    /** JSON string: query / table / form / meta */
    private String viewJson;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
