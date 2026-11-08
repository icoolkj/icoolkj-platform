package com.icoolkj.platform.infra.storage;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileDownloadUrlResult {

    private final String url;
    private final boolean external;
}
