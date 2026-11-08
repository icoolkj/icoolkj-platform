# i18n seed sources (NOT used at runtime)

See **[docs/i18n-standard.md](../docs/i18n-standard.md)** for the full i18n standard.

Frontend UI copy is loaded from `sys_i18n_message` via API at runtime.

These files are **seed sources only** for generating Flyway SQL:

```bash
node scripts/generate-i18n-seed.mjs
```

After changing copy:

1. Edit messages in **系统管理 → 文案管理**, or update files here and regenerate SQL for new environments.
2. Click **刷新缓存** in 文案管理 (also reloads the current browser session).

**Encoding:** `messages_*.properties` are **UTF-8** (not ISO-8859-1). In IntelliJ: Settings → Editor → File Encodings → **Default encoding for properties files = UTF-8**, and disable **Transparent native-to-ascii conversion** (or you will see mojibake for Chinese).
