# Test Fixtures

Test fixtures belong under `src/test/resources/db/fixture`.

Local seed data is intentionally separate under `src/main/resources/db/seed/local` and must not be required by tests. Tests should create only the fixture rows they need for their own case.
