# MongoDB Migration Scripts

Place MongoDB migration scripts (`.js` files) in this directory. The Jenkins pipeline automatically detects and runs new scripts against the database during deployment.

## Naming convention

Use a numbered prefix with a double underscore and a short description:

```
V001__create_indexes.js
V002__seed_default_settings.js
V003__add_user_field.js
```

Scripts are executed in **alphabetical order**. The number prefix ensures correct sequencing.

## How it works

1. The Jenkins pipeline scans this directory for `.js` files.
2. For each script, it checks the `_schema_migrations` collection in MongoDB.
3. If the script has **not** been applied, it runs via `mongosh` against the database.
4. On success, the script name and timestamp are recorded in `_schema_migrations`.
5. On failure, the pipeline **stops immediately** (fail-fast).

Already-applied scripts are skipped (idempotent deploys).

## Writing a migration script

Scripts run inside `mongosh` connected to the `tobyresume` database. Example:

```javascript
// V001__create_indexes.js
// Create index on Hero collection for contentState lookups

db.hero.createIndex({ contentState: 1 }, { unique: true });
print("Created unique index on hero.contentState");
```

### Rules

- **One concern per script** — don't mix unrelated changes.
- **Idempotent when possible** — use `createIndex` (safe to re-run) instead of assuming state.
- **No destructive operations** without a backup plan — dropping collections, deleting data, etc.
- **Test locally first** — run against a local MongoDB before committing.

## Manual execution (without Jenkins)

```bash
# From repo root, with MongoDB container running:
docker exec -i tobyresume-mongo mongosh tobyresume < deploy/db-migrations/V001__create_indexes.js
```

## Tracking collection

The `_schema_migrations` collection stores applied migrations:

```json
{
  "name": "V001__create_indexes.js",
  "appliedAt": "2026-02-13T10:00:00.000Z"
}
```

Do **not** manually edit or delete records from this collection unless you know what you are doing.
