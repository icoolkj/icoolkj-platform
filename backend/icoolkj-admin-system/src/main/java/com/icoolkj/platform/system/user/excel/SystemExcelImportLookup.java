package com.icoolkj.platform.system.user.excel;

import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.common.model.page.PageResult;
import com.icoolkj.platform.framework.datascope.DataScopeSpec;
import com.icoolkj.platform.framework.datascope.RowAccessSupport;
import com.icoolkj.platform.framework.excel.ExcelImportLookup;
import com.icoolkj.platform.framework.excel.ExcelImportValues;
import com.icoolkj.platform.system.dept.dto.DeptPageQuery;
import com.icoolkj.platform.system.post.dto.PostPageQuery;
import com.icoolkj.platform.system.role.dto.RolePageQuery;
import com.icoolkj.platform.system.dept.service.DeptService;
import com.icoolkj.platform.system.dict.service.DictDataService;
import com.icoolkj.platform.system.post.service.PostService;
import com.icoolkj.platform.system.role.service.RoleService;
import com.icoolkj.platform.system.dept.vo.DeptVO;
import com.icoolkj.platform.system.dict.vo.DictDataVO;
import com.icoolkj.platform.system.post.vo.PostVO;
import com.icoolkj.platform.system.role.vo.RoleVO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Dict dropdown labels + label/code resolve; dept/post/role name dropdown + resolve.
 */
@Service
@Primary
public class SystemExcelImportLookup implements ExcelImportLookup {

    private static final long REF_PAGE_SIZE = 500L;

    private final DictDataService dictDataService;
    private final DeptService deptService;
    private final PostService postService;
    private final RoleService roleService;
    private final RowAccessSupport rowAccessSupport;

    public SystemExcelImportLookup(DictDataService dictDataService,
                                   DeptService deptService,
                                   PostService postService,
                                   RoleService roleService,
                                   RowAccessSupport rowAccessSupport) {
        this.dictDataService = dictDataService;
        this.deptService = deptService;
        this.postService = postService;
        this.roleService = roleService;
        this.rowAccessSupport = rowAccessSupport;
    }

    @Override
    public List<String> dictLabels(String dictType) {
        if (!StringUtils.hasText(dictType)) {
            return List.of();
        }
        List<DictDataVO> items = dictDataService.listByDictType(dictType.trim());
        if (CollectionUtils.isEmpty(items)) {
            return List.of();
        }
        List<String> labels = new ArrayList<>(items.size());
        for (DictDataVO item : items) {
            if (item != null && StringUtils.hasText(item.getLabel())) {
                labels.add(item.getLabel().trim());
            }
        }
        return labels;
    }

    @Override
    public String resolveDictCode(String dictType, String rawLabelOrCode) {
        if (!StringUtils.hasText(rawLabelOrCode)) {
            return null;
        }
        String type = dictType == null ? "" : dictType.trim();
        List<DictDataVO> items = dictDataService.listByDictType(type);
        List<ExcelImportValues.LabelValue> pairs = new ArrayList<>();
        if (!CollectionUtils.isEmpty(items)) {
            for (DictDataVO item : items) {
                if (item == null) {
                    continue;
                }
                pairs.add(new ExcelImportValues.LabelValue(item.getLabel(), item.getValue()));
            }
        }
        return ExcelImportValues.resolveDictCode(type, pairs, rawLabelOrCode);
    }

    @Override
    public List<String> deptNames() {
        return uniqueNames(loadAssignableDeptEntries());
    }

    @Override
    public String resolveDeptId(String nameOrId) {
        String id = ExcelImportValues.resolveNameOrId(
                nameOrId,
                loadAssignableDeptEntries(),
                "error.excel.import_dept_not_found",
                "error.excel.import_dept_ambiguous");
        if (id != null) {
            rowAccessSupport.assertDataScope(DataScopeSpec.deptAsId(), null, id);
        }
        return id;
    }

    @Override
    public List<String> postNames() {
        return uniqueNames(loadPostEntries());
    }

    @Override
    public String resolvePostId(String nameOrId) {
        return ExcelImportValues.resolveNameOrId(
                nameOrId,
                loadPostEntries(),
                "error.excel.import_post_not_found",
                "error.excel.import_post_ambiguous");
    }

    @Override
    public List<String> roleNames() {
        return uniqueNames(loadRoleEntries());
    }

    @Override
    public String resolveRoleId(String nameOrId) {
        return ExcelImportValues.resolveNameOrId(
                nameOrId,
                loadRoleEntries(),
                "error.excel.import_role_not_found",
                "error.excel.import_role_ambiguous");
    }

    private static List<String> uniqueNames(List<Map.Entry<String, String>> entries) {
        LinkedHashMap<String, Boolean> names = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : entries) {
            names.putIfAbsent(e.getKey(), Boolean.TRUE);
        }
        return new ArrayList<>(names.keySet());
    }

    private List<Map.Entry<String, String>> loadAssignableDeptEntries() {
        return pageEntries(pageNo -> {
            DeptPageQuery query = new DeptPageQuery();
            query.setPageNo(pageNo);
            query.setPageSize(REF_PAGE_SIZE);
            query.setStatus(StatusEnum.NORMAL.getCode());
            PageResult<DeptVO> page = deptService.page(query);
            List<DeptVO> list = page.getList();
            if (CollectionUtils.isEmpty(list)) {
                return List.of();
            }
            List<Map.Entry<String, String>> batch = new ArrayList<>();
            for (DeptVO vo : list) {
                if (vo == null || !StringUtils.hasText(vo.getDeptName()) || !StringUtils.hasText(vo.getId())) {
                    continue;
                }
                batch.add(Map.entry(vo.getDeptName().trim(), vo.getId().trim()));
            }
            return batch;
        });
    }

    private List<Map.Entry<String, String>> loadPostEntries() {
        return pageEntries(pageNo -> {
            PostPageQuery query = new PostPageQuery();
            query.setPageNo(pageNo);
            query.setPageSize(REF_PAGE_SIZE);
            query.setStatus(StatusEnum.NORMAL.getCode());
            List<PostVO> list = postService.page(query).getList();
            if (CollectionUtils.isEmpty(list)) {
                return List.of();
            }
            List<Map.Entry<String, String>> batch = new ArrayList<>();
            for (PostVO vo : list) {
                if (vo == null || !StringUtils.hasText(vo.getPostName()) || !StringUtils.hasText(vo.getId())) {
                    continue;
                }
                batch.add(Map.entry(vo.getPostName().trim(), vo.getId().trim()));
            }
            return batch;
        });
    }

    private List<Map.Entry<String, String>> loadRoleEntries() {
        return pageEntries(pageNo -> {
            RolePageQuery query = new RolePageQuery();
            query.setPageNo(pageNo);
            query.setPageSize(REF_PAGE_SIZE);
            query.setStatus(StatusEnum.NORMAL.getCode());
            List<RoleVO> list = roleService.page(query).getList();
            if (CollectionUtils.isEmpty(list)) {
                return List.of();
            }
            List<Map.Entry<String, String>> batch = new ArrayList<>();
            for (RoleVO vo : list) {
                if (vo == null || !StringUtils.hasText(vo.getRoleName()) || !StringUtils.hasText(vo.getId())) {
                    continue;
                }
                batch.add(Map.entry(vo.getRoleName().trim(), vo.getId().trim()));
            }
            return batch;
        });
    }

    private List<Map.Entry<String, String>> pageEntries(
            Function<Long, List<Map.Entry<String, String>>> pageLoader) {
        List<Map.Entry<String, String>> entries = new ArrayList<>();
        long pageNo = 1;
        while (true) {
            List<Map.Entry<String, String>> batch = pageLoader.apply(pageNo);
            if (CollectionUtils.isEmpty(batch)) {
                break;
            }
            entries.addAll(batch);
            if (batch.size() < REF_PAGE_SIZE) {
                break;
            }
            pageNo++;
        }
        return entries;
    }
}
