# jyotish-service

Sugam Jyotish bounded context — Vedic astrology calculation + Kundali SaaS.

- **Standalone** vertical (CRM-style modular monolith)
- API prefix: `/api/v1/jyotish/**`
- Does **not** own shop ERP tables or hospital appointments
- Calculation engine is a pure Java package (`engine.*`) with **no** Spring/UI imports

## Phase 0–4 scope

| Endpoint | Purpose |
|----------|---------|
| `GET /api/v1/jyotish/status` | Service status (no tenant header) |
| `POST /api/v1/jyotish/workspaces/bootstrap` | Ensure tenant workspace |
| `GET /api/v1/jyotish/places?q=` | City search (lat/lon/tz) |
| `POST/GET/PUT/DELETE /api/v1/jyotish/profiles/**` | Birth profile CRUD + duplicate/archive |
| `POST /api/v1/jyotish/kundali/generate` | Generate D1 + persist D9 + Vimshottari eagerly |
| `GET /api/v1/jyotish/kundali/{id}` | Snapshot + planets + houses |
| `GET /api/v1/jyotish/kundali/{id}/planets` | Planets only |
| `GET /api/v1/jyotish/kundali/{id}/houses` | Houses only |
| `GET /api/v1/jyotish/kundali/{id}/charts` | List Vargas (READY / LAZY / COMING_SOON) |
| `GET /api/v1/jyotish/kundali/{id}/charts/{varga}` | D1 / D2 / D3 / D9 / D10 (lazy compute+store) |
| `GET /api/v1/jyotish/kundali/{id}/dasha` | Default Vimshottari timeline + current |
| `GET /api/v1/jyotish/kundali/{id}/dasha/{system}` | Named system (`VIMSHOTTARI`; others Coming Soon) |

Flyway **V1**: workspace + birth profile tables.  
Flyway **V2**: `kundali_snapshot`, `planetary_position`, `house_position`.  
Flyway **V3**: `divisional_chart`, `divisional_planet_position`, `divisional_house_position`.  
Flyway **V4**: `dasha_period`.

### Engine V1.2 (Phase 4)

- Sidereal Vedic D1 (Rashi), default Lahiri ayanamsa
- **Vargas framework** (`engine.varga`): D1/D2/D3/D9/D10
- **Dasha framework** (`engine.dasha`): register a `DashaCalculator`
- Implemented dasha: **Vimshottari** (MD → AD → PD from Moon nakshatra + balance)
- Catalog stubs: Yogini, Chara, Ashtottari → Coming Soon (API 501)
- Ephemeris: pure Java Meeus-style (`MeeusEphemeris`)
- Whole-sign houses; combust stubbed false

**Versioning choice:** bumped `calculation_engine_version` to **V1.2** because the algorithm surface expanded (dasha). Historical snapshots stay at whatever version was stamped at generate time.

**Compute strategy:** D9 + Vimshottari persisted **eagerly** on kundali generate. Other Vargas lazy on first GET. Missing dasha rows on older snapshots are computed+stored on first dasha GET.

```bash
mvn test
```

## Local run

```powershell
# One-time: create role + database
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -f scripts\create-jyotishdb.sql

cd D:\sugamFlow\sugamflow-astro\jyotish-service
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Defaults: `jdbc:postgresql://localhost:5432/jyotishdb` · user/pass **`jyotishdb`/`jyotishdb`**  
Override with `JYOTISH_DB_URL` / `JYOTISH_DB_USERNAME` / `JYOTISH_DB_PASSWORD`.

Port: **8097** · Eureka name: `jyotish-service`

Local profile **disables Eureka** by default.  
jyotish-ui proxies to `:8097`. Gateway: `GATEWAY_JYOTISH_URI=http://localhost:8097` (or `host.docker.internal:8097`).

Headers required (except `/status` and actuator):

```
X-Tenant-Id: <org-or-shop-id>
```

## Generate kundali (example)

```bash
curl -X POST http://localhost:8097/api/v1/jyotish/kundali/generate \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: JYOTISH-DEMO-01" \
  -d "{\"birthProfileId\":1}"

curl http://localhost:8097/api/v1/jyotish/kundali/1/dasha \
  -H "X-Tenant-Id: JYOTISH-DEMO-01"
```

## Entitlements

`jyotish.entitlement.enabled=false` in local profile.

When enabled, calls subscription-service feature flag `FEATURE_JYOTISH` (catalog TBD).
