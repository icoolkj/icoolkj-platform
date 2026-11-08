package com.icoolkj.platform.generator.service.impl;

import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.common.util.IdGeneratorUtils;
import com.icoolkj.platform.generator.domain.GenTable;
import com.icoolkj.platform.generator.domain.GenViewConfig;
import com.icoolkj.platform.generator.mapper.GenViewConfigMapper;
import com.icoolkj.platform.generator.service.GenViewConfigService;
import com.icoolkj.platform.generator.util.GenClasspathBaselineLoader;
import com.icoolkj.platform.generator.util.GenViewBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class GenViewConfigServiceImpl implements GenViewConfigService {

    private final GenViewConfigMapper genViewConfigMapper;

    public GenViewConfigServiceImpl(GenViewConfigMapper genViewConfigMapper) {
        this.genViewConfigMapper = genViewConfigMapper;
    }

    @Override
    public GenViewConfig getByTableId(String tableId) {
        return genViewConfigMapper.selectByTableId(tableId);
    }

    @Override
    public GenViewConfig getRuntimeByBusiness(String businessName, String apiModule) {
        if (!StringUtils.hasText(businessName) || !StringUtils.hasText(apiModule)) {
            return null;
        }
        return genViewConfigMapper.selectByBusinessAndModule(businessName.trim(), apiModule.trim());
    }

    @Override
    @Transactional
    public void save(GenViewConfig row) {
        if (StringUtils.hasText(row.getId())) {
            genViewConfigMapper.update(row);
        } else {
            row.setId(IdGeneratorUtils.nextIdStr());
            genViewConfigMapper.insert(row);
        }
    }

    @Override
    @Transactional
    public void syncFromTable(GenTable table) {
        if (table == null || !StringUtils.hasText(table.getId())) {
            return;
        }
        String fresh = GenViewBuilder.buildJson(table, table.getColumns());
        GenViewConfig existing = genViewConfigMapper.selectByTableId(table.getId());
        if (existing == null) {
            GenViewConfig row = new GenViewConfig();
            row.setId(IdGeneratorUtils.nextIdStr());
            row.setTableId(table.getId());
            row.setViewJson(fresh);
            genViewConfigMapper.insert(row);
        } else {
            existing.setViewJson(GenViewBuilder.mergeJson(existing.getViewJson(), fresh));
            genViewConfigMapper.update(existing);
        }
    }

    @Override
    @Transactional
    public void deleteByTableId(String tableId) {
        genViewConfigMapper.deleteByTableId(tableId);
    }

    @Override
    public String getGitBaselineJson(String businessName) {
        return GenClasspathBaselineLoader.loadViewJson(businessName)
                .orElseThrow(() -> new BusinessException("error.gen.baseline_not_found", businessName, "view.json"));
    }

    @Override
    @Transactional
    public void restoreGitBaseline(String tableId, String businessName) {
        String baseline = getGitBaselineJson(businessName);
        GenViewConfig existing = genViewConfigMapper.selectByTableId(tableId);
        if (existing == null) {
            GenViewConfig row = new GenViewConfig();
            row.setId(IdGeneratorUtils.nextIdStr());
            row.setTableId(tableId);
            row.setViewJson(baseline);
            genViewConfigMapper.insert(row);
        } else {
            existing.setViewJson(baseline);
            genViewConfigMapper.update(existing);
        }
    }
}
