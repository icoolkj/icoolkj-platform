# iCoolKJ Platform Backend

Maven multi-module backend for iCoolKJ Platform.

## Modules

| Module | Role |
|--------|------|
| `icoolkj-admin-common` | Constants, enums, exceptions, unified response |
| `icoolkj-admin-framework` | Security, JWT, Redis, MyBatis-Plus, tenant, AOP |
| `icoolkj-admin-system` | User, role, menu, tenant, dict, config, logs |
| `icoolkj-admin-infra` | File storage, async bus, third-party providers |
| `icoolkj-admin-generator` | Code generator |
| `icoolkj-admin-workflow` | Flowable workflow |
| `icoolkj-admin-oa` | OA instances / forms |
| `icoolkj-admin-biz` | Business extensions |
| `icoolkj-admin-api` | HTTP API bootstrap |
| `icoolkj-admin-worker` | Scheduled jobs |

## Build

Requires **JDK 17+** (prod runbook may use JDK 21).

```bash
cd backend
mvn clean install -DskipTests
```

**Flyway：** 唯一目录 `backend/database/migration/`（**V1–V2 only**）。见 [database/README.md](database/README.md)。

```bash
python scripts/verify_flyway_sql_source.py
```

## Documentation

- [docs/AI-CONTEXT.md](../docs/AI-CONTEXT.md)
- [docs/architecture/04-directory-structure.md](../docs/architecture/04-directory-structure.md)
- [docs/architecture/09-core-modules.md](../docs/architecture/09-core-modules.md)
- [docs/architecture/20-maven-pom-design.md](../docs/architecture/20-maven-pom-design.md)
- [docs/architecture/22-application-config.md](../docs/architecture/22-application-config.md)

## Stack

Java 17+, Spring Boot 3, Spring Security 6, MyBatis-Plus, Redis, JWT, Flowable
