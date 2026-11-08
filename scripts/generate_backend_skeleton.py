# -*- coding: utf-8 -*-
"""Generate iCoolKJ Platform backend Java skeleton classes."""
from __future__ import annotations

import os
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA_ROOT = "src/main/java/com/icoolkj/platform"

MODULES = {
    "api": ROOT / "backend/icoolkj-admin-api" / JAVA_ROOT / "api",
    "worker": ROOT / "backend/icoolkj-admin-worker" / JAVA_ROOT / "worker",
    "common": ROOT / "backend/icoolkj-admin-common" / JAVA_ROOT / "common",
    "framework": ROOT / "backend/icoolkj-admin-framework" / JAVA_ROOT / "framework",
    "system": ROOT / "backend/icoolkj-admin-system" / JAVA_ROOT / "system",
    "infra": ROOT / "backend/icoolkj-admin-infra" / JAVA_ROOT / "infra",
    "biz": ROOT / "backend/icoolkj-admin-biz" / JAVA_ROOT / "biz",
}

# (module, subpackage, class_name, kind)
# kind: application | class | interface | enum | annotation | constants | exception
ENTRIES: list[tuple[str, str, str, str]] = [
    # api
    ("api", "", "ICoolKJAdminApiApplication", "application"),
    ("api", "config", "OpenApiConfig", "class"),
    ("api", "config", "WebMvcConfig", "class"),
    ("api", "config", "JacksonConfig", "class"),
    ("api", "controller.auth", "AuthController", "class"),
    ("api", "controller.auth", "ProfileController", "class"),
    ("api", "controller.system", "UserController", "class"),
    ("api", "controller.system", "RoleController", "class"),
    ("api", "controller.system", "MenuController", "class"),
    ("api", "controller.system", "DeptController", "class"),
    ("api", "controller.system", "PostController", "class"),
    ("api", "controller.system", "DictTypeController", "class"),
    ("api", "controller.system", "DictItemController", "class"),
    ("api", "controller.system", "ConfigController", "class"),
    ("api", "controller.system", "NoticeController", "class"),
    ("api", "controller.system", "IpAccessRuleController", "class"),
    ("api", "controller.system", "LoginLogController", "class"),
    ("api", "controller.system", "OperationLogController", "class"),
    ("api", "controller.tenant", "TenantController", "class"),
    ("api", "controller.infra", "FileController", "class"),
    ("api", "controller.infra", "AsyncTaskController", "class"),
    ("api", "controller.biz", "ProjectController", "class"),
    ("api", "controller.biz", "CustomerController", "class"),
    # worker (scheduled jobs — see icoolkj-admin-worker module)
    ("worker", "", "ICoolKJAdminWorkerApplication", "application"),
    ("worker", "config", "WorkerConfig", "class"),
    ("worker", "config", "WorkerBackupProperties", "class"),
    ("worker", "backup", "DatabaseBackupService", "class"),
    ("worker", "job", "DatabaseBackupJob", "class"),
    ("worker", "job", "AuditLogRetentionJob", "class"),
    # common constants
    ("common", "constant", "CommonConstants", "constants"),
    ("common", "constant", "SecurityConstants", "constants"),
    ("common", "constant", "CacheKeyConstants", "constants"),
    ("common", "constant", "TenantConstants", "constants"),
    ("common", "constant", "PermissionConstants", "constants"),
    # common enums
    ("common", "enums", "StatusEnum", "enum"),
    ("common", "enums", "YesNoEnum", "enum"),
    ("common", "enums", "MenuTypeEnum", "enum"),
    ("common", "enums", "DataScopeEnum", "enum"),
    ("common", "enums", "LoginStatusEnum", "enum"),
    ("common", "enums", "OperationStatusEnum", "enum"),
    ("common", "enums", "IpRuleTypeEnum", "enum"),
    ("common", "enums", "ApplyScopeEnum", "enum"),
    ("common", "enums", "UserTypeEnum", "enum"),
    ("common", "enums", "AsyncTaskStatusEnum", "enum"),
    ("common", "enums", "StorageProviderTypeEnum", "enum"),
    # common exceptions
    ("common", "exception", "BusinessException", "exception"),
    ("common", "exception", "UnauthorizedException", "exception"),
    ("common", "exception", "ForbiddenException", "exception"),
    ("common", "exception", "TenantException", "exception"),
    ("common", "exception", "IpAccessDeniedException", "exception"),
    ("common", "exception", "FileStorageException", "exception"),
    # common model
    ("common", "model.response", "ApiResponse", "class"),
    ("common", "model.page", "PageQuery", "class"),
    ("common", "model.page", "PageResult", "class"),
    ("common", "model.dto", "IdRequest", "class"),
    ("common", "model.vo", "OptionVO", "class"),
    ("common", "model.vo", "LabelValueVO", "class"),
    # common util
    ("common", "util", "JsonUtils", "class"),
    ("common", "util", "DateTimeUtils", "class"),
    ("common", "util", "SecurityUtils", "class"),
    ("common", "util", "ServletUtils", "class"),
    ("common", "util", "IpUtils", "class"),
    ("common", "util", "MaskUtils", "class"),
    ("common", "util", "TreeBuildUtils", "class"),
    ("common", "util", "IdGeneratorUtils", "class"),
    # framework annotation
    ("framework", "annotation", "OperationLog", "annotation"),
    ("framework", "annotation", "TenantIgnore", "annotation"),
    ("framework", "annotation", "DataScope", "annotation"),
    ("framework", "annotation", "Idempotent", "annotation"),
    ("framework", "annotation", "SensitiveField", "annotation"),
    # framework aspect
    ("framework", "aspect", "OperationLogAspect", "class"),
    ("framework", "aspect", "DataScopeAspect", "class"),
    ("framework", "aspect", "IdempotentAspect", "class"),
    # framework auth
    ("framework", "auth", "AuthService", "interface"),
    ("framework", "auth", "AuthServiceImpl", "class"),
    ("framework", "auth", "PermissionService", "interface"),
    ("framework", "auth", "PermissionServiceImpl", "class"),
    ("framework", "auth", "DataPermissionService", "interface"),
    ("framework", "auth", "DataPermissionServiceImpl", "class"),
    # framework cache
    ("framework", "cache", "CacheService", "interface"),
    ("framework", "cache", "RedisCacheServiceImpl", "class"),
    ("framework", "cache", "CacheKeyBuilder", "class"),
    ("framework", "cache", "CacheNames", "constants"),
    ("framework", "cache", "CacheTtlProperties", "class"),
    # framework config
    ("framework", "config", "FrameworkAutoConfiguration", "class"),
    ("framework", "config", "RedisConfig", "class"),
    ("framework", "config", "JacksonAutoConfig", "class"),
    ("framework", "config", "ThreadPoolConfig", "class"),
    ("framework", "config", "Knife4jConfig", "class"),
    # framework exception
    ("framework", "exception", "GlobalExceptionHandler", "class"),
    ("framework", "exception", "ExceptionCodeTranslator", "class"),
    # framework id
    ("framework", "id", "IdGenerator", "interface"),
    ("framework", "id", "SnowflakeIdGenerator", "class"),
    ("framework", "id", "IdGeneratorAutoConfig", "class"),
    # framework jwt
    ("framework", "jwt", "JwtTokenService", "class"),
    ("framework", "jwt", "JwtTokenParser", "class"),
    ("framework", "jwt", "JwtProperties", "class"),
    ("framework", "jwt", "JwtClaimsConstants", "constants"),
    ("framework", "jwt", "JwtTokenPair", "class"),
    # framework logging
    ("framework", "logging", "TraceIdFilter", "class"),
    ("framework", "logging", "AccessLogFilter", "class"),
    ("framework", "logging", "SensitiveDataMasker", "class"),
    ("framework", "logging", "LogContext", "class"),
    ("framework", "logging", "LogRecordFactory", "class"),
    # framework mybatis
    ("framework", "mybatis", "BaseEntity", "class"),
    ("framework", "mybatis", "TenantBaseEntity", "class"),
    ("framework", "mybatis", "MybatisPlusConfig", "class"),
    ("framework", "mybatis", "MetaObjectHandlerImpl", "class"),
    ("framework", "mybatis", "DatabaseDialectResolver", "class"),
    ("framework", "mybatis", "LogicDeleteConstants", "constants"),
    # framework redis
    ("framework", "redis", "RedisOperator", "class"),
    ("framework", "redis", "RedisLockService", "class"),
    ("framework", "redis", "RedisMessagePublisher", "class"),
    # framework security
    ("framework", "security", "SecurityConfig", "class"),
    ("framework", "security", "SecurityContextHelper", "class"),
    ("framework", "security", "PasswordEncoderConfig", "class"),
    ("framework", "security.filter", "JwtAuthenticationFilter", "class"),
    ("framework", "security.filter", "LoginTenantFilter", "class"),
    ("framework", "security.handler", "AuthenticationEntryPointImpl", "class"),
    ("framework", "security.handler", "AccessDeniedHandlerImpl", "class"),
    ("framework", "security.handler", "LogoutSuccessHandlerImpl", "class"),
    ("framework", "security.model", "SecurityUserDetails", "class"),
    ("framework", "security.model", "LoginUser", "class"),
    ("framework", "security.model", "LoginSession", "class"),
    ("framework", "security.model", "LoginResult", "class"),
    ("framework", "security.service", "SecurityUserDetailsService", "class"),
    ("framework", "security.service", "LoginAuthenticationService", "class"),
    ("framework", "security.service", "LogoutService", "class"),
    ("framework", "security.service", "LoginSessionService", "interface"),
    ("framework", "security.service", "RedisLoginSessionServiceImpl", "class"),
    ("framework", "security.service", "LoginFailService", "class"),
    ("framework", "security.token", "TokenBlacklistService", "class"),
    ("framework", "security.token", "TokenRefreshService", "class"),
    # framework tenant
    ("framework", "tenant", "TenantContext", "class"),
    ("framework", "tenant", "TenantContextHolder", "class"),
    ("framework", "tenant", "TenantResolveService", "interface"),
    ("framework", "tenant", "TenantResolveServiceImpl", "class"),
    ("framework", "tenant", "TenantWebInterceptor", "class"),
    ("framework", "tenant", "TenantLineHandlerImpl", "class"),
    ("framework", "tenant", "TenantMybatisInterceptorConfig", "class"),
    ("framework", "tenant", "TenantProperties", "class"),
    # framework web
    ("framework", "web", "RequestBodyCachingFilter", "class"),
    ("framework", "web", "WebResponseAdvice", "class"),
    ("framework", "web", "CurrentRequestHolder", "class"),
]

SYSTEM_DOMAINS = [
    "tenant", "user", "role", "menu", "dept", "post", "dict", "config",
    "notice", "iprule", "loginlog", "operationlog",
]

SYSTEM_CLASSES: dict[str, list[tuple[str, str]]] = {
    "tenant": [
        ("TenantEntity", "class"), ("TenantMapper", "interface"), ("TenantService", "interface"),
        ("TenantServiceImpl", "class"), ("TenantQueryService", "interface"),
        ("TenantQueryServiceImpl", "class"), ("TenantCreateRequest", "class"),
        ("TenantUpdateRequest", "class"), ("TenantPageQuery", "class"),
        ("TenantVO", "class"), ("TenantConvert", "class"),
    ],
    "user": [
        ("UserEntity", "class"), ("UserMapper", "interface"), ("UserService", "interface"),
        ("UserServiceImpl", "class"), ("UserQueryService", "interface"),
        ("UserQueryServiceImpl", "class"), ("UserRoleService", "interface"),
        ("UserRoleServiceImpl", "class"), ("UserPostService", "interface"),
        ("UserPostServiceImpl", "class"), ("UserCreateRequest", "class"),
        ("UserUpdateRequest", "class"), ("UserResetPasswordRequest", "class"),
        ("UserPageQuery", "class"), ("UserVO", "class"), ("UserDetailVO", "class"),
        ("UserConvert", "class"),
    ],
    "role": [
        ("RoleEntity", "class"), ("RoleMapper", "interface"), ("RoleService", "interface"),
        ("RoleServiceImpl", "class"), ("RoleMenuService", "interface"),
        ("RoleMenuServiceImpl", "class"), ("RoleDeptService", "interface"),
        ("RoleDeptServiceImpl", "class"), ("RoleCreateRequest", "class"),
        ("RoleUpdateRequest", "class"), ("RolePageQuery", "class"), ("RoleVO", "class"),
        ("RoleDetailVO", "class"), ("RoleConvert", "class"),
    ],
    "menu": [
        ("MenuEntity", "class"), ("MenuMapper", "interface"), ("MenuService", "interface"),
        ("MenuServiceImpl", "class"), ("MenuQueryService", "interface"),
        ("MenuQueryServiceImpl", "class"), ("MenuTreeBuilder", "class"),
        ("MenuCreateRequest", "class"), ("MenuUpdateRequest", "class"),
        ("MenuTreeVO", "class"), ("RouteVO", "class"), ("MenuConvert", "class"),
    ],
    "dept": [
        ("DeptEntity", "class"), ("DeptMapper", "interface"), ("DeptService", "interface"),
        ("DeptServiceImpl", "class"), ("DeptTreeBuilder", "class"),
        ("DeptCreateRequest", "class"), ("DeptUpdateRequest", "class"),
        ("DeptTreeVO", "class"), ("DeptConvert", "class"),
    ],
    "post": [
        ("PostEntity", "class"), ("PostMapper", "interface"), ("PostService", "interface"),
        ("PostServiceImpl", "class"), ("PostCreateRequest", "class"),
        ("PostUpdateRequest", "class"), ("PostPageQuery", "class"),
        ("PostVO", "class"), ("PostConvert", "class"),
    ],
    "dict": [
        ("DictTypeEntity", "class"), ("DictItemEntity", "class"),
        ("DictTypeMapper", "interface"), ("DictItemMapper", "interface"),
        ("DictService", "interface"), ("DictServiceImpl", "class"),
        ("DictQueryService", "interface"), ("DictQueryServiceImpl", "class"),
        ("DictCacheService", "interface"), ("DictCacheServiceImpl", "class"),
        ("DictTypeCreateRequest", "class"), ("DictTypeUpdateRequest", "class"),
        ("DictItemCreateRequest", "class"), ("DictItemUpdateRequest", "class"),
        ("DictTypeVO", "class"), ("DictItemVO", "class"), ("DictConvert", "class"),
    ],
    "config": [
        ("ConfigEntity", "class"), ("ConfigMapper", "interface"),
        ("ConfigService", "interface"), ("ConfigServiceImpl", "class"),
        ("ConfigQueryService", "interface"), ("ConfigQueryServiceImpl", "class"),
        ("TenantConfigResolver", "class"), ("ConfigCacheService", "interface"),
        ("ConfigCacheServiceImpl", "class"), ("ConfigCreateRequest", "class"),
        ("ConfigUpdateRequest", "class"), ("ConfigVO", "class"), ("ConfigConvert", "class"),
    ],
    "notice": [
        ("NoticeEntity", "class"), ("NoticeMapper", "interface"),
        ("NoticeService", "interface"), ("NoticeServiceImpl", "class"),
        ("NoticeCreateRequest", "class"), ("NoticeUpdateRequest", "class"),
        ("NoticePageQuery", "class"), ("NoticeVO", "class"), ("NoticeConvert", "class"),
    ],
    "iprule": [
        ("IpAccessRuleEntity", "class"), ("IpAccessRuleMapper", "interface"),
        ("IpRuleService", "interface"), ("IpRuleServiceImpl", "class"),
        ("IpRuleMatchService", "interface"), ("IpRuleMatchServiceImpl", "class"),
        ("IpRuleCacheService", "interface"), ("IpRuleCacheServiceImpl", "class"),
        ("LoginSecurityPolicyService", "interface"),
        ("LoginSecurityPolicyServiceImpl", "class"),
        ("IpAccessRuleCreateRequest", "class"), ("IpAccessRuleUpdateRequest", "class"),
        ("IpAccessRuleVO", "class"), ("IpRuleConvert", "class"),
    ],
    "loginlog": [
        ("LoginLogEntity", "class"), ("LoginLogMapper", "interface"),
        ("LoginLogService", "interface"), ("LoginLogServiceImpl", "class"),
        ("LoginLogPageQuery", "class"), ("LoginLogVO", "class"), ("LoginLogConvert", "class"),
    ],
    "operationlog": [
        ("OperationLogEntity", "class"), ("OperationLogMapper", "interface"),
        ("OperationLogService", "interface"), ("OperationLogServiceImpl", "class"),
        ("OperationLogPageQuery", "class"), ("OperationLogVO", "class"),
        ("OperationLogConvert", "class"),
    ],
}

INFRA_ENTRIES: list[tuple[str, str, str]] = [
    ("storage", "FileStorageService", "interface"),
    ("storage", "FileMetadataService", "interface"),
    ("storage", "FileUploadRequest", "class"),
    ("storage", "FileUploadResult", "class"),
    ("storage", "FileDownloadUrlRequest", "class"),
    ("storage", "FileDownloadUrlResult", "class"),
    ("storage", "FileDeleteRequest", "class"),
    ("storage", "InfraFileEntity", "class"),
    ("storage", "InfraFileMapper", "interface"),
    ("storage", "InfraFileService", "interface"),
    ("storage", "InfraFileServiceImpl", "class"),
    ("storage", "InfraFileConvert", "class"),
    ("storage.minio", "MinioClientFactory", "class"),
    ("storage.minio", "MinioStorageProperties", "class"),
    ("storage.minio", "MinioFileStorageServiceImpl", "class"),
    ("storage.local", "LocalFileStorageServiceImpl", "class"),
    ("async", "AsyncTaskBus", "interface"),
    ("async", "AsyncTaskPublisher", "interface"),
    ("async", "AsyncTaskConsumer", "interface"),
    ("async", "AsyncTaskExecutor", "interface"),
    ("async", "AsyncTaskHandler", "interface"),
    ("async", "AsyncTaskRegistry", "class"),
    ("async", "AsyncTaskMessage", "class"),
    ("async", "AsyncTaskResult", "class"),
    ("async", "AsyncTaskEntity", "class"),
    ("async", "AsyncTaskLogEntity", "class"),
    ("async", "AsyncTaskMapper", "interface"),
    ("async", "AsyncTaskLogMapper", "interface"),
    ("async", "AsyncTaskService", "interface"),
    ("async", "AsyncTaskServiceImpl", "class"),
    ("async", "AsyncTaskLogService", "interface"),
    ("async", "AsyncTaskLogServiceImpl", "class"),
    ("async.redis", "RedisStreamAsyncTaskBusImpl", "class"),
    ("async.redis", "RedisStreamTaskPublisher", "class"),
    ("async.redis", "RedisStreamTaskConsumer", "class"),
    ("async.scheduler", "AsyncTaskRetryScheduler", "class"),
    ("async.handler", "DefaultAsyncTaskExecutor", "class"),
    ("provider.sms", "SmsProvider", "interface"),
    ("provider.sms", "SmsProviderRouter", "class"),
    ("provider.sms", "SmsSendRequest", "class"),
    ("provider.sms", "SmsSendResult", "class"),
    ("provider.sms", "AliyunSmsProviderImpl", "class"),
    ("provider.sms", "HuaweiSmsProviderImpl", "class"),
    ("provider.mail", "MailProvider", "interface"),
    ("provider.mail", "MailSendRequest", "class"),
    ("provider.mail", "MailSendResult", "class"),
    ("provider.mail", "DefaultMailProviderImpl", "class"),
    ("provider.ocr", "OcrProvider", "interface"),
    ("provider.ocr", "OcrRequest", "class"),
    ("provider.ocr", "OcrResult", "class"),
    ("provider.ocr", "BaiduOcrProviderImpl", "class"),
    ("provider.sign", "ElectronicSignProvider", "interface"),
    ("provider.sign", "SignRequest", "class"),
    ("provider.sign", "SignResult", "class"),
    ("provider.sign", "EsignProviderImpl", "class"),
    ("log", "ApiAccessLogEntity", "class"),
    ("log", "ThirdPartyCallLogEntity", "class"),
    ("log", "ApiAccessLogMapper", "interface"),
    ("log", "ThirdPartyCallLogMapper", "interface"),
    ("log", "ApiAccessLogService", "interface"),
    ("log", "ApiAccessLogServiceImpl", "class"),
    ("log", "ThirdPartyCallLogService", "interface"),
    ("log", "ThirdPartyCallLogServiceImpl", "class"),
]

BIZ_ENTRIES: dict[str, list[tuple[str, str]]] = {
    "project": [
        ("ProjectEntity", "class"), ("ProjectMapper", "interface"),
        ("ProjectService", "interface"), ("ProjectServiceImpl", "class"),
        ("ProjectCreateRequest", "class"), ("ProjectUpdateRequest", "class"),
        ("ProjectPageQuery", "class"), ("ProjectVO", "class"), ("ProjectConvert", "class"),
    ],
    "customer": [
        ("CustomerEntity", "class"), ("CustomerMapper", "interface"),
        ("CustomerService", "interface"), ("CustomerServiceImpl", "class"),
        ("CustomerCreateRequest", "class"), ("CustomerUpdateRequest", "class"),
        ("CustomerPageQuery", "class"), ("CustomerVO", "class"), ("CustomerConvert", "class"),
    ],
}


def package_name(module: str, subpkg: str) -> str:
    base = f"com.icoolkj.platform.{module}"
    return base if not subpkg else f"{base}.{subpkg}"


def render(module: str, subpkg: str, name: str, kind: str) -> str:
    pkg = package_name(module, subpkg)
    lines = [f"package {pkg};", ""]
    if kind == "application":
        scan = "com.icoolkj.platform"
        lines += [
            "import org.springframework.boot.SpringApplication;",
            "import org.springframework.boot.autoconfigure.SpringBootApplication;",
            "",
            "@SpringBootApplication(scanBasePackages = \"" + scan + "\")",
            f"public class {name} {{",
            "",
            "    public static void main(String[] args) {",
            f"        SpringApplication.run({name}.class, args);",
            "    }",
            "}",
            "",
        ]
    elif kind == "interface":
        lines += [f"public interface {name} {", "", "}", ""]
    elif kind == "enum":
        lines += [f"public enum {name} {{", "", "    PLACEHOLDER", "", "}", ""]
    elif kind == "annotation":
        lines += [
            "import java.lang.annotation.Documented;",
            "import java.lang.annotation.ElementType;",
            "import java.lang.annotation.Retention;",
            "import java.lang.annotation.RetentionPolicy;",
            "import java.lang.annotation.Target;",
            "",
            "@Documented",
            "@Retention(RetentionPolicy.RUNTIME)",
            "@Target({ElementType.METHOD, ElementType.TYPE})",
            f"public @interface {name} {{",
            "",
            "    String value() default \"\";",
            "}",
            "",
        ]
    elif kind == "constants":
        lines += [
            f"public final class {name} {{",
            "",
            f"    private {name}() {{",
            "    }",
            "}",
            "",
        ]
    elif kind == "exception":
        lines += [
            f"public class {name} extends RuntimeException {{",
            "",
            f"    public {name}(String message) {{",
            "        super(message);",
            "    }",
            "",
            f"    public {name}(String message, Throwable cause) {{",
            "        super(message, cause);",
            "    }",
            "}",
            "",
        ]
    else:
        lines += [f"public class {name} {{", "", "}", ""]
    return "\n".join(lines)


def write_class(module: str, subpkg: str, name: str, kind: str) -> None:
    base = MODULES[module]
    pkg_path = subpkg.replace(".", os.sep) if subpkg else ""
    dir_path = base / pkg_path if pkg_path else base
    dir_path.mkdir(parents=True, exist_ok=True)
    file_path = dir_path / f"{name}.java"
    file_path.write_text(render(module, subpkg, name, kind), encoding="utf-8", newline="\n")


def main() -> None:
    count = 0
    for module, subpkg, name, kind in ENTRIES:
        write_class(module, subpkg, name, kind)
        count += 1
    for domain, classes in SYSTEM_CLASSES.items():
        for name, kind in classes:
            write_class("system", domain, name, kind)
            count += 1
    for subpkg, name, kind in INFRA_ENTRIES:
        write_class("infra", subpkg, name, kind)
        count += 1
    for domain, classes in BIZ_ENTRIES.items():
        for name, kind in classes:
            write_class("biz", domain, name, kind)
            count += 1
    print(f"Generated {count} Java skeleton classes.")


if __name__ == "__main__":
    main()
