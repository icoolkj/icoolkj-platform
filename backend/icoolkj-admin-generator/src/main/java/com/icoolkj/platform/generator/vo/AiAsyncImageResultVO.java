package com.icoolkj.platform.generator.vo;

import com.icoolkj.platform.infra.vo.AsyncTaskFileResultVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiAsyncImageResultVO extends AsyncTaskFileResultVO {

    private String invokeId;
    private String prompt;
    private String model;
    private String providerName;
    private String size;
    private Integer n;
    private List<AiImageItemVO> items = new ArrayList<>();

    @Data
    public static class AiImageItemVO {
        private String fileId;
        private String url;
        private String fileName;
    }
}
