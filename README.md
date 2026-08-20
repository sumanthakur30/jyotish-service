# jyotish-service

Sugam Jyotish bounded context — Vedic astrology calculation + Kundali SaaS.

- **Standalone** vertical (CRM-style modular monolith)
- API prefix: `/api/v1/jyotish/**`
- Does **not** own shop ERP tables or hospital appointments
- Calculation engine is a pure Java package (`engine.*`) with **no** Spring/UI imports

## Phase 0–2 scope

| Endpoint | Purpose |
|----------|---------|
| `GET /api/v1/jyotish/status` | Service status (no tenant header) |
| `POST /api/v1/jyotish/workspaces/bootstrap` | Ensure tenant workspace |
| `GET /api/v1/jyotish/places?q=` | City search (lat/lon/tz) |
| `POST/GET/PUT/DELETE /api/v1/jyotish/profiles/**` | Birth profile CRUD + duplicate/archive |
| `POST /api/v1/jyotish/kundali/generate` | Generate D1 from profile id or inline birth |
| `GET /api/v1/jyotish/kundali/{id}` | Snapshot + planets + houses |
| `GET /api/v1/jyotish/kundali/{id}/planets` | Planets only |
| `GET /api/v1/jyotish/kundali/{id}/houses` | Houses only |

Flyway **V1**: workspace + birth profile tables.  
Flyway **V2**: `kundali_snapshot`, `planetary_position`, `house_position` (`calculation_engine_version=V1.0`).

### Engine V1.0

- Sidereal Vedic D1 (Rashi), default Lahiri ayanamsa
- Ephemeris: pure Java Meeus-style (`MeeusEphemeris`) — see class Javadoc for Swiss Eph upgrade path
- Whole-sign houses; combust stubbed false (Coming Soon)

```bash
mvn -Dtest=CalculationEngineTest test
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
```

## Entitlements

`jyotish.entitlement.enabled=false` in local profile.

When enabled, calls subscription-service feature flag `FEATURE_JYOTISH` (catalog TBD).
