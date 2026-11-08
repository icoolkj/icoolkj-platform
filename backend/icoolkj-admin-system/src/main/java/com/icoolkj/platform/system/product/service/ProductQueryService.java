package com.icoolkj.platform.system.product.service;

import com.icoolkj.platform.framework.security.model.LoginUser;
import com.icoolkj.platform.system.product.vo.ProductVO;

import java.util.List;

/** 产品宇宙查询（Console Shell） */
public interface ProductQueryService {

    /** 当前用户已授权菜单推导出的可见产品，按 sort_no 升序（业务靠前） */
    List<ProductVO> listMine(LoginUser loginUser, String locale);

    /** 管理端：全部启用产品 */
    List<ProductVO> listAllActive(String locale);
}
