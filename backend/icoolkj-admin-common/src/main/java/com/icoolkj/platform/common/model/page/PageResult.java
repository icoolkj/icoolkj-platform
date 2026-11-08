package com.icoolkj.platform.common.model.page;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Paginated result payload for {@link com.icoolkj.platform.common.model.response.ApiResponse}.
 */
@Data
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> list = Collections.emptyList();
    private Long total = 0L;
    private Long pageNo = 1L;
    private Long pageSize = 20L;

    public PageResult() {
    }

    public PageResult(List<T> list, Long total, Long pageNo, Long pageSize) {
        this.list = list == null ? Collections.emptyList() : list;
        this.total = total == null ? 0L : total;
        this.pageNo = pageNo == null ? 1L : pageNo;
        this.pageSize = pageSize == null ? 20L : pageSize;
    }

    public static <T> PageResult<T> of(List<T> list, Long total, Long pageNo, Long pageSize) {
        return new PageResult<>(list, total, pageNo, pageSize);
    }

    public void setList(List<T> list) {
        this.list = list == null ? Collections.emptyList() : list;
    }
}
