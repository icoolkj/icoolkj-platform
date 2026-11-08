-- One-shot cleanup for existing DBs that still have platform baseline rows in sys_i18n_message.
-- After Git i18n/platform migration, default copy lives on classpath; DB rows are overrides only.
-- New installs (V3 rewritten) do not seed these keys — this script is for already-migrated environments.
-- Usage: mysql -u ... -p your_db < scripts/clear-platform-i18n-db-baseline.sql
-- Then refresh i18n cache in admin UI (or restart) so Redis drops old maps.

set names utf8mb4;

delete from sys_i18n_message
 where message_key like 'error.%'
    or message_key like 'entity.name.%'
    or message_key like 'validation.%'
    or message_key like 'notify.%'
    or message_key like 'oa.instance.%';
