package com.icoolkj.platform.system.role.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoleMenuAssignRequest {

    @NotNull
    private List<String> menuIds;

}
