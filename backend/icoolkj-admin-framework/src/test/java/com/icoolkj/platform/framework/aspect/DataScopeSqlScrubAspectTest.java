package com.icoolkj.platform.framework.aspect;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.icoolkj.platform.common.model.page.PageQuery;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DataScopeSqlScrubAspectTest {

    @Test
    void clearsPageQueryAndMap() {
        PageQuery query = new PageQuery();
        query.setDataScopeSql(" OR 1=1");
        Map<String, Object> map = new HashMap<>();
        map.put("dataScopeSql", " OR 1=1");

        DataScopeSqlScrubAspect.clearClientSql(new Object[]{query, map});

        assertNull(query.getDataScopeSql());
        assertNull(map.get("dataScopeSql"));
    }
}
