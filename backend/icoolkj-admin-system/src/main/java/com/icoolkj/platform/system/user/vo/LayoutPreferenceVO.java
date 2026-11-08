package com.icoolkj.platform.system.user.vo;

import lombok.Data;

/** Shell layout preferences stored as JSON under {@code ui.layout}. */
@Data
public class LayoutPreferenceVO {

    /** dark | light */
    private String sidebarTheme;

    private Boolean breadcrumb;

    private Boolean tagsView;

    /** dialog | drawer */
    private String formMode;

    private Boolean watermark;

    private Boolean menuUniqueOpened;

    /** Show page title + description on list pages */
    private Boolean pageHeader;

    /** light | primary — top shell bar appearance */
    private String headerStyle;
}
