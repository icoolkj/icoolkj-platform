package com.icoolkj.platform.system.brandpack.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.constant.TenantConstants;
import com.icoolkj.platform.common.enums.StatusEnum;
import com.icoolkj.platform.framework.tenant.TenantScopeRunner;
import com.icoolkj.platform.infra.domain.InfraFileCatalogItemEntity;
import com.icoolkj.platform.infra.mapper.InfraFileCatalogItemMapper;
import com.icoolkj.platform.infra.support.ChromeRolesJsonHelper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 按 brandPack/view.json 的 fileCatalogs 拉目录槽位，用 chrome_roles + assets 解析运行时用途。
 * 停用槽位（{@link StatusEnum#DISABLE}）不参与。
 */
@Component
public class BrandPackChromeResolver {

    private static final String VIEW_RESOURCE = "i18n/gen/brandPack/view.json";

    private final InfraFileCatalogItemMapper catalogItemMapper;
    private final ObjectMapper objectMapper;
    private volatile List<String> catalogCodes;

    public BrandPackChromeResolver(InfraFileCatalogItemMapper catalogItemMapper, ObjectMapper objectMapper) {
        this.catalogItemMapper = catalogItemMapper;
        this.objectMapper = objectMapper;
    }

    /** role → fileId（仅启用槽位；同 role 按 chrome_roles 优先级） */
    public Map<String, String> resolveChromeFileIds(Map<String, String> assets) {
        Map<String, String> safeAssets = assets == null ? Collections.emptyMap() : assets;
        List<RoleCandidate> candidates = loadEnabledCandidates();
        Map<String, List<RoleCandidate>> byRole = new LinkedHashMap<>();
        for (RoleCandidate c : candidates) {
            for (Map.Entry<String, Integer> role : c.roles().entrySet()) {
                byRole.computeIfAbsent(role.getKey(), k -> new ArrayList<>()).add(
                        new RoleCandidate(c.profileCode(), Map.of(role.getKey(), role.getValue())));
            }
        }
        Map<String, String> chrome = new LinkedHashMap<>();
        for (Map.Entry<String, List<RoleCandidate>> e : byRole.entrySet()) {
            String role = e.getKey();
            List<RoleCandidate> list = e.getValue();
            list.sort(Comparator.comparingInt(c -> c.roles().getOrDefault(role, Integer.MAX_VALUE)));
            for (RoleCandidate c : list) {
                String fileId = safeAssets.get(c.profileCode());
                if (StringUtils.hasText(fileId)) {
                    chrome.put(role, fileId.trim());
                    break;
                }
            }
        }
        return chrome;
    }

    /** role → label（从 labels[profileCode]，按同 role 优先级） */
    public Map<String, String> resolveChromeLabels(Map<String, String> labels) {
        Map<String, String> safeLabels = labels == null ? Collections.emptyMap() : labels;
        List<RoleCandidate> candidates = loadEnabledCandidates();
        Map<String, List<RoleCandidate>> byRole = new LinkedHashMap<>();
        for (RoleCandidate c : candidates) {
            for (Map.Entry<String, Integer> role : c.roles().entrySet()) {
                byRole.computeIfAbsent(role.getKey(), k -> new ArrayList<>()).add(
                        new RoleCandidate(c.profileCode(), Map.of(role.getKey(), role.getValue())));
            }
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<RoleCandidate>> e : byRole.entrySet()) {
            String role = e.getKey();
            List<RoleCandidate> list = e.getValue();
            list.sort(Comparator.comparingInt(c -> c.roles().getOrDefault(role, Integer.MAX_VALUE)));
            for (RoleCandidate c : list) {
                String label = safeLabels.get(c.profileCode());
                if (StringUtils.hasText(label)) {
                    out.put(role, label.trim());
                    break;
                }
            }
        }
        return out;
    }

    public String fileIdForRole(Map<String, String> assets, String role) {
        if (!StringUtils.hasText(role)) {
            return null;
        }
        return resolveChromeFileIds(assets).get(role.trim());
    }

    public String labelForRole(Map<String, String> labels, String role) {
        if (!StringUtils.hasText(role)) {
            return null;
        }
        return resolveChromeLabels(labels).get(role.trim());
    }

    /**
     * 启用中的目录槽位（不要求配置 chrome_roles；供登录页等按目录驱动展示）。
     * catalogCode → 有序 profileCode 列表。
     */
    public Map<String, List<String>> listEnabledProfileCodesByCatalog() {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (String catalogCode : catalogCodes()) {
            List<InfraFileCatalogItemEntity> items = TenantScopeRunner.runIgnoringTenant(() -> {
                List<InfraFileCatalogItemEntity> rows =
                        catalogItemMapper.selectByTenantAndCatalog(
                                TenantConstants.PLATFORM_TENANT_ID, catalogCode);
                return rows == null ? List.<InfraFileCatalogItemEntity>of() : rows;
            });
            List<InfraFileCatalogItemEntity> enabled = new ArrayList<>();
            for (InfraFileCatalogItemEntity item : items) {
                if (item == null || !StringUtils.hasText(item.getProfileCode())) {
                    continue;
                }
                if (StatusEnum.isDisabled(item.getStatus())) {
                    continue;
                }
                enabled.add(item);
            }
            enabled.sort(Comparator.comparingInt(i -> i.getSortNo() == null ? 0 : i.getSortNo()));
            List<String> codes = new ArrayList<>(enabled.size());
            for (InfraFileCatalogItemEntity item : enabled) {
                codes.add(item.getProfileCode().trim());
            }
            out.put(catalogCode, List.copyOf(codes));
        }
        return out;
    }

    private List<RoleCandidate> loadEnabledCandidates() {
        List<RoleCandidate> out = new ArrayList<>();
        for (String catalogCode : catalogCodes()) {
            List<InfraFileCatalogItemEntity> items = TenantScopeRunner.runIgnoringTenant(() -> {
                List<InfraFileCatalogItemEntity> rows =
                        catalogItemMapper.selectByTenantAndCatalog(
                                TenantConstants.PLATFORM_TENANT_ID, catalogCode);
                return rows == null ? List.<InfraFileCatalogItemEntity>of() : rows;
            });
            for (InfraFileCatalogItemEntity item : items) {
                if (item == null || !StringUtils.hasText(item.getProfileCode())) {
                    continue;
                }
                if (StatusEnum.isDisabled(item.getStatus())) {
                    continue;
                }
                Map<String, Integer> roles = ChromeRolesJsonHelper.parse(item.getChromeRoles());
                if (roles.isEmpty()) {
                    continue;
                }
                out.add(new RoleCandidate(item.getProfileCode().trim(), roles));
            }
        }
        return out;
    }

    private List<String> catalogCodes() {
        List<String> cached = catalogCodes;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (catalogCodes != null) {
                return catalogCodes;
            }
            catalogCodes = loadCatalogCodesFromView();
            return catalogCodes;
        }
    }

    private List<String> loadCatalogCodesFromView() {
        try {
            ClassPathResource resource = new ClassPathResource(VIEW_RESOURCE);
            try (InputStream in = resource.getInputStream()) {
                JsonNode root = objectMapper.readTree(in);
                JsonNode catalogs = root.get("fileCatalogs");
                if (catalogs == null || !catalogs.isArray()) {
                    return List.of();
                }
                List<String> codes = new ArrayList<>();
                for (JsonNode node : catalogs) {
                    JsonNode code = node.get("code");
                    if (code != null && StringUtils.hasText(code.asText())) {
                        codes.add(code.asText().trim());
                    }
                }
                return List.copyOf(codes);
            }
        } catch (Exception ex) {
            return List.of();
        }
    }

    private record RoleCandidate(String profileCode, Map<String, Integer> roles) {
    }
}
