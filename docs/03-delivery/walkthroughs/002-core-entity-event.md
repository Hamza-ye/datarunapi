# Walkthrough — Step 2: Core Module — Entity + EntityType + Event

> **Date:** 2026-03-26
> **Step:** 2 of 7 (Slice 1)

## What Changed

### New: `platform-core` entities & persistence

| Entity | Table | PK | Description |
|--------|-------|----|-------------|
| `EntityType` | `platform_entity_type` | UUID | Registry of entity kinds (facility, item, etc.) |
| `PlatformEntity` | `platform_entity` | UUID | Identity record with JSONB `attributes` |
| `EntityReference` | `platform_entity_reference` | UUID | Links entities to submissions/events |
| `PlatformEvent` | `platform_event` | UUID | Immutable audit log, JSONB `payload` |

### New: services & web layer

| Class | Module | Purpose |
|-------|--------|---------|
| `EventPublisher` | `platform-core` | Synchronous event emission within transactions |
| `UidGenerator` | `platform-core` | UUID v4 generation (independent of legacy ULID) |
| `EntityTypeController` | `platform-core` | CRUD for entity types |
| `EntityController` | `platform-core` | CRUD for entities + automatic event emission |
| `EventController` | `platform-core` | Read-only event queries |

### New: Liquibase migrations

| Changeset | File | Tables |
|-----------|------|--------|
| `platform-001` | `001-create-entity-tables.xml` | `platform_entity_type`, `platform_entity` |
| `platform-002` | `002-create-entity-reference.xml` | `platform_entity_reference` |
| `platform-003` | `003-create-event.xml` | `platform_event` |

Aggregated via `app/src/main/resources/config/liquibase/app-master.xml` which includes both legacy `master.xml` and `platform-core-master.xml`.

### Modified: Spring Boot configuration

| File | Change | Why |
|------|--------|-----|
| `DataRunApiApp.java` | Added `@EntityScan(basePackages = {"org.nmcpye.datarun", "org.nmcpye.platform"})` | `@EntityScan` is not additive — single declaration must cover all JPA packages |
| `DatabaseConfiguration.java` | Scoped `@EnableJpaRepositories` to `org.nmcpye.datarun` only | Legacy repos use `BaseJpaRepositoryImpl`; platform repos use default `SimpleJpaRepository` |
| `LiquibaseConfiguration.java` | Reads `change-log` path from `LiquibaseProperties` | Previously hardcoded legacy master path, blocking the new aggregated `app-master.xml` |
| `application-dev.yml` / `application-prod.yml` | Set `spring.liquibase.change-log` to `classpath:config/liquibase/app-master.xml` | Points Liquibase to the aggregated changelog |

### New: `PlatformCoreJpaConfiguration`

Dedicated JPA config in `platform-core` with `@EnableJpaRepositories(basePackages = "org.nmcpye.platform")`. Uses default `SimpleJpaRepository` (no `BaseJpaRepositoryImpl` dependency).

## Design Decisions

| Decision | Rationale |
|----------|-----------|
| `PlatformEntity` name (not `Entity`) | Avoids collision with `jakarta.persistence.Entity` annotation |
| UUID v4 (not ULID) | Moving away from legacy ULID dependency |
| Synchronous event publishing | Events are within the same transaction as the mutation — guarantees audit integrity |
| Split `@EnableJpaRepositories` | Platform repos extend standard `JpaRepository`; legacy repos require hypersistence `BaseJpaRepository` — incompatible base classes |
| No `@EntityScan` on platform config | `@EntityScan` is **not additive** — declaring it on a sub-config overrides the global scan |

## Verification

### ✅ Compilation — PASSED
```
Reactor Summary for Data Run Api 6.3.1:
  Data Run Api ....................................... SUCCESS
  Data Run Legacy .................................... SUCCESS [  1.715 s]
  Platform Core ...................................... SUCCESS [  0.044 s]
  Platform Capture ................................... SUCCESS [  0.050 s]
  Platform Flow ...................................... SUCCESS [  0.044 s]
  Data Run App ....................................... SUCCESS [  2.077 s]
  BUILD SUCCESS
```

### ✅ Application startup — PASSED
```
Started DataRunApiApp in 19.306 seconds (process running for 19.745)
Application 'datRunApi' is running! Access URLs:
  Local:     http://localhost:8080/
  Profile(s): [dev, api-docs]
```

### ⚠️ ArchUnit test — BLOCKED
Network-restricted environment prevents surefire plugin download. Test exists and compiles.

## Gotchas Discovered

1. **`@EntityScan` is not additive** — if any `@Configuration` declares it, the auto-configured entity scan is replaced. Must use a single `@EntityScan` covering all packages.
2. **`@EnableJpaRepositories` with `repositoryBaseClass`** — applies to ALL repos in the scanned packages. If different modules need different base classes, split into separate `@EnableJpaRepositories` with distinct `basePackages`.
3. **`spring-boot:run -pl app`** — always run `mvn install -DskipTests` first in a multi-module build so the app module picks up fresh jars from reactor modules.
