package com.icoolkj.platform.system.user.service;

import java.util.List;

public interface UserRoleService {

    List<String> listRoleIdsByUser(String userId);

    void assignRoles(String userId, List<String> roleIds);

}
