# Database Migration

Training Service uses Flyway for `training_db` migrations.

Migration files live in:

```text
src/main/resources/db/migration
```

Migration file names follow Flyway versioned SQL naming:

```text
V{version}__{description}.sql
```

Examples:

```text
V1__create_training_core_tables.sql
V2__create_training_module_tables.sql
```

Profile behavior:

```text
local: runs Flyway against MySQL using DB_* environment variables
test: runs Flyway against an in-memory H2 database in MySQL compatibility mode
```

Task 2.1 configures the migration tool only. Actual schema files are added by later schema tasks.
