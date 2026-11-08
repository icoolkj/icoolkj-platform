package com.icoolkj.platform.generator.constant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.generator.vo.GenTableDesignVO;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * 平台支持的代码生成业务模块（与 GenInfoForm、Maven 模块一致）。
 * <p>注册表来源：classpath {@code generator/modules.json}（配置驱动，禁止在 Java 里 List.of 堆模块）。
 *
 * <ul>
 *   <li>backendModule → Java 包 {@code com.icoolkj.platform.{backendModule}}</li>
 *   <li>apiModule → 前端 api/views/i18n 目录 + 路由前缀 + 权限前缀首段</li>
 *   <li>tablePrefix → 建表命名前缀（同一库，按前缀区分归属）</li>
 * </ul>
 */
public final class GenModuleCatalog {

    private static final String MODULES_RESOURCE = "generator/modules.json";

    public record ModuleSpec(
            String apiModule,
            String labelKey,
            String tablePrefix,
            String backendModule,
            String baseType,
            String routePrefix,
            boolean includeTenantColumn
    ) {
    }

    private static final List<ModuleSpec> MODULES = loadModules();
    private static final Map<String, ModuleSpec> BY_API =
            MODULES.stream().collect(Collectors.toUnmodifiableMap(
                    spec -> spec.apiModule().toLowerCase(Locale.ROOT),
                    Function.identity(),
                    (left, right) -> left));

    private GenModuleCatalog() {
    }

    public static List<ModuleSpec> all() {
        return MODULES;
    }

    public static ModuleSpec require(String apiModule) {
        String key = StringUtils.defaultIfBlank(apiModule, "system").trim().toLowerCase(Locale.ROOT);
        ModuleSpec spec = BY_API.get(key);
        if (spec == null) {
            throw new BusinessException("error.gen.table_design.invalid_module", apiModule);
        }
        return spec;
    }

    public static void enrichDesign(GenTableDesignVO design) {
        ModuleSpec spec = require(design.getApiModule());
        design.setApiModule(spec.apiModule());
        design.setBackendModule(spec.backendModule());
        design.setBaseType(spec.baseType());
        design.setRoutePrefix(spec.routePrefix());
    }

    public static void validateTablePrefix(GenTableDesignVO design) {
        ModuleSpec spec = require(design.getApiModule());
        if (StringUtils.isNotBlank(design.getTableName())
                && !design.getTableName().startsWith(spec.tablePrefix())) {
            throw new BusinessException("error.gen.table_design.table_prefix_mismatch",
                    spec.tablePrefix(), design.getTableName());
        }
    }

    /** @deprecated use {@link #enrichDesign} + {@link #validateTablePrefix} */
    @Deprecated
    public static void applyToDesign(GenTableDesignVO design) {
        enrichDesign(design);
        validateTablePrefix(design);
    }

    public static String normalizeTableName(String apiModule, String tableName) {
        ModuleSpec spec = require(apiModule);
        String name = StringUtils.defaultString(tableName).trim().toLowerCase(Locale.ROOT);
        if (StringUtils.isBlank(name)) {
            return name;
        }
        if (!name.startsWith(spec.tablePrefix())) {
            name = spec.tablePrefix() + name.replaceAll("^(sys_|infra_|biz_|oa_)", "");
        }
        return name;
    }

    public static boolean includeTenantColumn(String apiModule) {
        return require(apiModule).includeTenantColumn();
    }

    private static List<ModuleSpec> loadModules() {
        try (InputStream in = GenModuleCatalog.class.getClassLoader().getResourceAsStream(MODULES_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + MODULES_RESOURCE);
            }
            List<ModuleSpec> loaded = new ObjectMapper().readValue(in, new TypeReference<>() {
            });
            if (loaded == null || loaded.isEmpty()) {
                throw new IllegalStateException("Empty generator module catalog: " + MODULES_RESOURCE);
            }
            return List.copyOf(loaded);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load " + MODULES_RESOURCE, ex);
        }
    }
}
