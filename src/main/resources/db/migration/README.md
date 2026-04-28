# Training DB Migrations

Flyway SQL migrations for `training_db` live in this directory.

Naming convention:

```text
V{version}__{description}.sql
```

Examples:

```text
V1__create_training_core_tables.sql
V2__create_training_module_tables.sql
```

Local-only seed migrations live under `src/main/resources/db/seed/local` and are enabled only by the `local` profile.
