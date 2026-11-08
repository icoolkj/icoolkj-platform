package com.icoolkj.platform.infra.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ExcelExportAsyncResultVO extends AsyncTaskFileResultVO {

    public ExcelExportAsyncResultVO(String fileId, String fileName) {
        super(fileId, fileName);
    }
}
