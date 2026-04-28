# Database Migration

Training Service uses Flyway for `training_db` migrations.

Migration files live in:

```text
src/main/resources/db/migration
```

Local-only seed migrations live in:

```text
src/main/resources/db/seed/local
```

Migration file names follow Flyway versioned SQL naming:

```text
V{version}__{description}.sql
```

Examples:

```text
V1__create_training_core_tables.sql
V2__create_training_module_tables.sql
V3__insert_local_training_content.sql
```

Profile behavior:

```text
local: runs schema migrations and local seed migrations against MySQL using DB_* environment variables
test: runs schema migrations only against an in-memory H2 database in MySQL compatibility mode
```

Local seed migrations must insert only Training Service owned content data. Test fixtures belong under `src/test/resources/db/fixture` and must not depend on local seed data.
