package com.icoolkj.platform.system.backup.service;

import com.icoolkj.platform.system.backup.dto.DatabaseBackupConfigUpdateRequest;
import com.icoolkj.platform.system.backup.vo.DatabaseBackupConfigAdminVO;

/** Database backup settings stored in sys_config (platform scope). */
public interface DatabaseBackupConfigAdminService {

    DatabaseBackupConfigAdminVO getForAdmin();

    void update(DatabaseBackupConfigUpdateRequest request);
}
