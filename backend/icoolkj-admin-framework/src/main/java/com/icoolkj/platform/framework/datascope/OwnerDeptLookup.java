package com.icoolkj.platform.framework.datascope;

@FunctionalInterface
public interface OwnerDeptLookup {

    String findDeptIdByUserId(String userId);
}
