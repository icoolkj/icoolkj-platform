package com.icoolkj.platform.system.datascope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.icoolkj.platform.common.enums.DataScopeEnum;
import com.icoolkj.platform.framework.annotation.DataScope;
import com.icoolkj.platform.system.datascope.handler.AllDataScopeHandler;
import com.icoolkj.platform.system.datascope.handler.CustomDeptDataScopeHandler;
import com.icoolkj.platform.system.datascope.handler.DeptAndChildDataScopeHandler;
import com.icoolkj.platform.system.datascope.handler.DeptDataScopeHandler;
import com.icoolkj.platform.system.datascope.handler.SelfDataScopeHandler;
import com.icoolkj.platform.system.role.domain.RoleDeptEntity;
import com.icoolkj.platform.system.role.mapper.RoleDeptMapper;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

class DataScopeSqlParityTest {

    @Test
    void registryFailsAtConstructionWhenHandlerMissing() {
        assertThrows(IllegalStateException.class,
                () -> new DataScopePredicateHandlerRegistry(List.of(new SelfDataScopeHandler())));
    }

    @Test
    void handlersRegisteredForAllScopes() {
        DataScopePredicateHandlerRegistry registry = fullRegistry();
        for (DataScopeEnum scope : DataScopeEnum.values()) {
            assertEquals(scope, registry.require(scope).scope());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "u1, d1, true",
            "u1, '', true",
            "'', d1, false",
            "u2, d1, false"
    })
    void selfSqlAndMatchAgreeOnUserColumn(String ownerUser, String ownerDept, boolean expected) {
        boolean match = DataScopeMatchSupport.matchesSelf(
                true, true, blankToNull(ownerUser), blankToNull(ownerDept), "u1", "d9");
        assertEquals(expected, match);
        DataScope ann = ann("user_id", "dept_id");
        String sql = DataScopeSqlSupport.selfPredicate(ann, "u1", "d9");
        assertTrue(sql.contains("user_id"));
        assertTrue(sql.contains("'u1'"));
        // SQL is user_id only — empty owner dept_id still matches when user_id equals operator
        assertFalse(sql.contains("dept_id"));
    }

    @Test
    void deptSqlNeverMatchesEmptyOwnerDeptSemantics() {
        assertFalse(DataScopeMatchSupport.matchesDept(true, true, "u1", null, "u2", "d1"));
        DataScope ann = ann("user_id", "dept_id");
        String sql = DataScopeSqlSupport.eqLiteral(ann.deptAlias(), ann.deptColumn(), "d1");
        assertTrue(sql.contains("dept_id = 'd1'"));
        // SQL equality cannot match blank owner dept_id → same deny as MatchSupport
    }

    @Test
    void deptAndChildSqlAlignedWithAncestorsMatch() {
        DataScope ann = ann("user_id", "dept_id");
        String sql = DataScopeSqlSupport.deptAndChildren(ann, "t1", "d-op");
        assertTrue(sql.contains("FIND_IN_SET('d-op', ancestors)"));
        assertTrue(sql.contains("dept_id = 'd-op'"));

        assertTrue(DataScopeMatchSupport.matchesDeptAndChild(
                true, true, "u-other", "d-child", "0,d-op", "u-op", "d-op"));
        assertFalse(DataScopeMatchSupport.matchesDeptAndChild(
                true, true, "u-other", "d-child", "0,other", "u-op", "d-op"));
        assertFalse(DataScopeMatchSupport.matchesDeptAndChild(
                true, true, "u-other", null, "0,d-op", "u-op", "d-op"));

        DeptAndChildDataScopeHandler handler = new DeptAndChildDataScopeHandler();
        Optional<String> predicate = handler.buildPredicate(
                new DataScopePredicateContext(ann, "t1", "u-op", "d-op", "r1"));
        assertTrue(predicate.isPresent());
        assertTrue(predicate.get().contains("FIND_IN_SET"));
    }

    @Test
    void customEmptyRoleDeptsFallsBackToSelfInHandlerAndMatch() {
        RoleDeptMapper roleDeptMapper = Mockito.mock(RoleDeptMapper.class);
        when(roleDeptMapper.selectList(any(RoleDeptEntity.class))).thenReturn(List.of());
        CustomDeptDataScopeHandler handler = new CustomDeptDataScopeHandler(roleDeptMapper);
        DataScope ann = ann("user_id", "dept_id");
        Optional<String> predicate = handler.buildPredicate(
                new DataScopePredicateContext(ann, "t1", "u-op", "d-op", "r1"));
        assertTrue(predicate.isPresent());
        assertTrue(predicate.get().contains("user_id = 'u-op'"));

        assertNull(DataScopeMatchSupport.matchesCustomOrFallbackSelf("d-op", List.of()));
        assertEquals(Boolean.FALSE, DataScopeMatchSupport.matchesCustomOrFallbackSelf(null, List.of("d1")));
        assertEquals(Boolean.TRUE, DataScopeMatchSupport.matchesCustomOrFallbackSelf("d1", List.of("d1")));
    }

    @Test
    void multiRoleOrMergeJoinsPredicatesLikeListSql() {
        // Mirrors SystemDataPermissionServiceImpl: OR-join non-empty predicates; ALL short-circuits to ""
        DataScope ann = ann("user_id", "dept_id");
        SelfDataScopeHandler self = new SelfDataScopeHandler();
        DeptDataScopeHandler dept = new DeptDataScopeHandler();
        AllDataScopeHandler all = new AllDataScopeHandler();

        Optional<String> selfPred = self.buildPredicate(
                new DataScopePredicateContext(ann, "t1", "u-op", "d-op", "r-self"));
        Optional<String> deptPred = dept.buildPredicate(
                new DataScopePredicateContext(ann, "t1", "u-op", "d-op", "r-dept"));
        assertTrue(selfPred.isPresent());
        assertTrue(deptPred.isPresent());

        Set<String> predicates = new LinkedHashSet<>();
        predicates.add(selfPred.get());
        predicates.add(deptPred.get());
        String merged = " AND (" + String.join(" OR ", predicates) + ")";
        assertTrue(merged.contains(" OR "));
        assertTrue(merged.contains("user_id = 'u-op'"));
        assertTrue(merged.contains("dept_id = 'd-op'"));

        assertTrue(all.buildPredicate(
                new DataScopePredicateContext(ann, "t1", "u-op", "d-op", "r-all")).isEmpty());
    }

    private static DataScopePredicateHandlerRegistry fullRegistry() {
        return new DataScopePredicateHandlerRegistry(List.of(
                new AllDataScopeHandler(),
                new SelfDataScopeHandler(),
                new DeptDataScopeHandler(),
                new DeptAndChildDataScopeHandler(),
                new CustomDeptDataScopeHandler(Mockito.mock(RoleDeptMapper.class))));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static DataScope ann(String userColumn, String deptColumn) {
        return new DataScope() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DataScope.class;
            }

            @Override
            public String value() {
                return "";
            }

            @Override
            public String deptAlias() {
                return "";
            }

            @Override
            public String userAlias() {
                return "";
            }

            @Override
            public String deptColumn() {
                return deptColumn;
            }

            @Override
            public String userColumn() {
                return userColumn;
            }

            @Override
            public String sqlField() {
                return "dataScopeSql";
            }
        };
    }
}
