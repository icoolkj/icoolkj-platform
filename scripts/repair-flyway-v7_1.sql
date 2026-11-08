-- Run once if V7_1 migration failed due to corrupted UTF-8 seed file.
-- Then restart the application (Flyway will retry V7_1).

DELETE FROM flyway_schema_history
WHERE version = '7.1' AND success = 0;
