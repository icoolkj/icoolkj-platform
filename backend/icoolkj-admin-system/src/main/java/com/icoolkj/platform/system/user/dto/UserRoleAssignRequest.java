package com.icoolkj.platform.system.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleAssignRequest {

    @NotNull
    private List<String> roleIds;

}
