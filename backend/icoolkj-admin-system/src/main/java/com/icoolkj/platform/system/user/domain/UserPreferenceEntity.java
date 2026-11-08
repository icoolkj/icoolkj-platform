package com.icoolkj.platform.system.user.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Maps to {@code sys_user_preference}. */
@Getter
@Setter
public class UserPreferenceEntity {

    private String tenantId;
    private String userId;
    private String prefKey;
    private String prefValue;
    private LocalDateTime updatedAt;
}
