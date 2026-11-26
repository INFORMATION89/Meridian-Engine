# MER-0.2 Storage and cache contract

- **Status:** Design proposal for M0.2 review; no persistent schema or retention policy is approved by this document.
- **Baseline:** storage-optional engine; SQLDelight/SQLite is a candidate implementation behind an engine-owned boundary.
- **Authority:** `PLAN.md`, `RULES.md`, ADR-0003, `M0_2_CONTRACT_DESIGN.md`, and the owner-approved privacy/product decisions.

## 1. Goals and non-goals

Define the boundary between meteorological domain logic and persistence, cache semantics, offline/stale results, lifecycle, deletion, migrations, and privacy. The engine must remain useful with storage disabled and must not require a particular database.

M0.2 defines contracts and deterministic tests. It does not implement production cache policy, provider refresh, forecast archives, model learning, or a production database schema.

## 2. Storage is optional

- No-storage is the default and must be a fully supported operating mode.
- Storage is enabled only through explicit consumer configuration and explicit provision/selection of a storage implementation.
- The public core contract must not expose SQLDelight-generated types, SQLite connections, JDBC, Android Context, filesystem paths, or platform database handles.
- Core domain logic depends on storage interfaces, never on a concrete database module.
- SQLDelight/SQLite remains the current candidate, not an irrevocable dependency. A host-provided adapter must remain possible if it can satisfy the same contract.
- Meridian must not silently create a database, request filesystem permissions, or persist user/location/weather data merely because an engine instance was constructed.

## 3. Data classes and separation

Keep three logically separate data lifecycles; do not conflate them into one unlimited history table:

1. **Operational cache:** short-lived material used to reduce repeated requests and support explicitly permitted stale/offline fallback.
2. **Verification and learning records:** forecast snapshots paired with later observations, with lineage and evaluation eligibility. These records support reproducible verification and future evidence-based calibration; they are not ordinary response cache entries.
3. **Climatological/archive data:** longer historical reference datasets with their own source licence, provenance, retention, and storage requirements.

M0.2 must define interfaces and metadata separation only. The concrete retention windows, learning eligibility, and archive acquisition policy are deferred to M0.3 or the relevant later specification and require review before persistent collection begins. No unlimited retention is implied. A previously discussed recent verification window (for example, 30 days) is not silently promoted to an approved storage commitment here.

## 4. Cache entry semantics

A cache entry must preserve at least:

- stable request/capability identity and schema version;
- payload identity and value provenance;
- source/model identity and any contributing sources;
- issue time, observation time, fetch time, valid time/interval;
- freshness metadata, expiry/fresh-until policy, and whether it is stale;
- transformation/correction and algorithm/configuration versions where applicable;
- data quality and uncertainty metadata;
- safe attribution/licence metadata where required.

Cache identity must include all request dimensions that can change the meaning of a result (for example, location/grid identity, variables, temporal range, units or normalization version, and source/configuration policy). Do not key only by a rounded location if that could conflate materially different cells or station representations.

Freshness and validity are different: a forecast may be valid for a future interval while the retrieved representation is stale; an observation may be old but still a valid historical observation. Do not overwrite original times when serving from cache.

## 5. Stale and offline behaviour

- Offline fallback is explicit consumer policy, not an automatic promise that every query succeeds offline.
- If stale data is returned, mark it stale and expose its age/fetch time and original provenance.
- A stale entry must never be relabelled as current or have its issue/fetch/observation timestamps refreshed merely because it was read.
- If no usable cache entry exists, return a typed unavailable/offline outcome rather than a fabricated value.
- If fresh network data and stale cache data coexist, the result must identify which values came from which path and why.
- A partial fresh result may coexist with cached fallback for independent components only if each component's source and freshness remain explicit.

## 6. Retention, deletion, and privacy

- Retention is purpose-specific and bounded by a separately approved policy; no indefinite accumulation by default.
- Consumers must be able to clear operational cache and delete the engine-owned persisted records within the scope of the storage adapter.
- Deletion semantics must cover associated metadata, indexes, derived copies, and pending writes; document any platform limitations.
- Schema migration must not silently expand retention or change the purpose of previously collected data.
- Storage must not be used for telemetry or external sharing. Data contribution remains off by default and requires separate explicit opt-in, outside this contract.
- Precise location and user-linked identifiers should be minimized. Do not log persisted payloads or expose raw database content through diagnostics.
- Backup/restore behaviour and OS-level backup exclusion are consumer/platform packaging decisions that must be documented before production storage is enabled.

## 7. Failure and transaction semantics

- Storage errors are typed and distinguish unavailable, read, write, corruption, migration, quota/space, and closed-adapter failures where the platform can identify them.
- A failed write must not report success or leave a partially committed logical record.
- Multi-record operations that must be consistent use a transaction or equivalent atomic boundary.
- Corrupt/unreadable cache entries are isolated; they must not crash unrelated queries or be returned as valid data.
- Storage failure must not be silently treated as a cache miss if doing so would hide data loss or violate an explicit consumer requirement. The result may continue without storage only when the operation's semantics permit it and the failure is reported.
- Closing the engine closes only storage resources that the engine owns; externally supplied adapters follow their declared ownership contract.

## 8. Versioning and migration

- Persisted schema version is separate from public API/library version.
- Every schema change has a deterministic migration path, explicit compatibility range, and tests from each supported prior schema.
- Migration must be atomic where supported; interrupted migration must leave the database recoverable or fail safely without returning mixed-version data.
- Unknown future schema versions must not be destructively downgraded.
- Migration failure must preserve the original data and provide a typed failure/recovery path; no silent database deletion or reset.
- Schema migrations do not automatically imply a public API breaking change, and vice versa.

## 9. Deterministic verification

M0.2 tests use temporary/in-memory adapters and local fixtures to verify:

1. engine works with storage absent;
2. storage is not opened unless explicitly enabled;
3. cache round-trip preserves provenance and all time/freshness metadata;
4. stale entries remain stale after retrieval;
5. cache key separation prevents incompatible requests from colliding;
6. cache clear and record deletion are scoped and deterministic;
7. write transactions are atomic under injected failure;
8. corrupt records do not poison unrelated entries;
9. migration from the supported prior schema preserves values and metadata;
10. unknown newer schema versions are rejected non-destructively;
11. engine/adapter close respects ownership and is idempotent.

Tests must be deterministic and must not contact weather providers or external storage services.

## 10. Decisions intentionally deferred

- Concrete production driver and supported schema version.
- Operational cache TTLs and stale-while-revalidate policy.
- Retention windows for verification/learning records and climatological archives.
- Whether a recent verification window is 30 days or another evidence-backed duration.
- Backup policy and encryption-at-rest requirements for each consumer/platform.
- Any user-data contribution or sync workflow.

These require evidence and, where they alter privacy, retention, or distribution commitments, explicit owner approval.

## 11. Acceptance

- [ ] Storage-disabled operation is complete and tested.
- [ ] Storage is explicit, optional, and behind an engine-owned interface.
- [ ] Operational cache, verification history, and climatological archives are separate.
- [ ] Freshness, valid time, fetch time, provenance, and uncertainty survive cache round-trip.
- [ ] Stale/offline outcomes are explicit and never misrepresented as current.
- [ ] Retention is bounded by a separate approved policy; no unlimited history.
- [ ] Clear/delete and migration behaviour is documented and deterministic.
- [ ] Public API exposes no concrete database/platform implementation types.