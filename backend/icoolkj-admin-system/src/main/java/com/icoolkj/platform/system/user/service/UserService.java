package com.icoolkj.platform.system.user.service;

import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.framework.excel.ExcelImportResult;
import com.icoolkj.platform.system.user.dto.UserCreateRequest;
import com.icoolkj.platform.system.user.dto.UserPageQuery;
import com.icoolkj.platform.system.user.dto.UserResetPasswordRequest;
import com.icoolkj.platform.system.user.dto.UserUpdateRequest;
import com.icoolkj.platform.system.user.excel.UserImportRow;
import com.icoolkj.platform.system.user.vo.UserVO;

import java.util.List;

/** 用户表服务 */
public interface UserService {

    @DataScope(deptAlias = "u", userAlias = "u", deptColumn = "dept_id", userColumn = "id")
    PageResult<UserVO> page(UserPageQuery query);

    UserVO getById(String id);

    String create(UserCreateRequest request);

    /**
     * All-or-nothing import: validates every non-blank row first; on any error returns
     * {@link ExcelImportResult#hasErrors()} with success=0 and writes nothing.
     * Uses system initial password; {@code status}/{@code gender} accept dict codes.
     */
    ExcelImportResult importRows(List<UserImportRow> rows);

    void update(UserUpdateRequest request);

    void deleteById(String id);

    /** 重置密码：未传新密码时使用 {@code security.password.initial}，并强制下次登录改密。 */
    void resetPassword(String id, UserResetPasswordRequest request);

    /** Clears DB lock + Redis fail counters and kicks sessions. */
    void unlock(String id);
}
