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

Task 2.1 only configures the migration tool. Schema migrations are added by later schema tasks.
