# jyotish-service

Sugam Jyotish bounded context — Vedic astrology calculation + Kundali SaaS.

- **Standalone** vertical (CRM-style modular monolith)
- API prefix: `/api/v1/jyotish/**`
- Does **not** own shop ERP tables or hospital appointments
- Calculation engine is a pure Java package (`engine.*`) with **no** Spring/UI imports

## Phase 0–10 scope

| Endpoint | Purpose |
|----------|---------|
| `GET /api/v1/jyotish/status` | Service status (no tenant header) |
| `POST /api/v1/jyotish/workspaces/bootstrap` | Ensure tenant workspace |
| `GET /api/v1/jyotish/places?q=` | City search (lat/lon/tz) |
| `POST/GET/PUT/DELETE /api/v1/jyotish/profiles/**` | Birth profile CRUD + duplicate/archive |
| `POST /api/v1/jyotish/kundali/generate` | Generate D1 + persist D9 + Vimshottari + yogas eagerly |
| `GET /api/v1/jyotish/kundali/{id}` | Snapshot + planets + houses |
| `GET /api/v1/jyotish/kundali/{id}/planets` | Planets only |
| `GET /api/v1/jyotish/kundali/{id}/houses` | Houses only |
| `GET /api/v1/jyotish/kundali/{id}/charts` | List Vargas (READY / LAZY / COMING_SOON) |
| `GET /api/v1/jyotish/kundali/{id}/charts/{varga}` | D1 / D2 / D3 / D9 / D10 (lazy compute+store) |
| `GET /api/v1/jyotish/kundali/{id}/dasha` | Default Vimshottari timeline + current |
| `GET /api/v1/jyotish/kundali/{id}/dasha/{system}` | Named system (`VIMSHOTTARI`; others Coming Soon) |
| `GET /api/v1/jyotish/kundali/{id}/yogas` | Yoga results (+ optional `?category=`) |
| `POST /api/v1/jyotish/matching` | Ashta Koota + Manglik for two birth profiles |
| `GET /api/v1/jyotish/matching/{id}` | Stored matching session |
| `GET /api/v1/jyotish/kundali/{id}/transit?date=` | Gochar for date (default: now / today) |
| `POST /api/v1/jyotish/transit` | Gochar with `{ kundaliId, date?, time? }` |
| `POST /api/v1/jyotish/reports` | Generate PDF (`BASIC_KUNDALI` / `MATCHING` / optional `DASHA_SUMMARY` / `TRANSIT`) |
| `GET /api/v1/jyotish/reports/{id}` | Report metadata |
| `GET /api/v1/jyotish/reports/{id}/download` | PDF bytes |
| `GET /api/v1/jyotish/clients/dashboard` | CRM counts (clients + today’s appointments) |
| `POST/GET/PUT/DELETE /api/v1/jyotish/clients` | Astrologer client CRUD (+ `?q=` search) |
| `POST/GET/PUT/DELETE /api/v1/jyotish/appointments` | Appointments (+ filters `clientId`, `fromDate`, `toDate`, `status`) |
| `POST /api/v1/jyotish/ai/ask` | AI Jyotish Assistant `{ kundaliId, question, topic? }` |

Flyway **V1**: workspace + birth profile tables.  
Flyway **V2**: `kundali_snapshot`, `planetary_position`, `house_position`.  
Flyway **V3**: `divisional_chart`, `divisional_planet_position`, `divisional_house_position`.  
Flyway **V4**: `dasha_period`.  
Flyway **V5**: `yoga_result`.  
Flyway **V6**: `matching_session`, `matching_koota_score`.  
Flyway **V7**: `transit_snapshot`, `transit_planet_position`.  
Flyway **V8**: `kundali_report` (metadata; PDF files under `./data/reports`).  
Flyway **V9**: `jyotish_client`, `jyotish_client_birth_profile`, `jyotish_appointment`.  
Flyway **V10**: `jyotish_ai_ask` (AI ask audit/meter).

### Engine V1.5 + Phase 8–10 (+ Swiss ephemeris SPI)

- Sidereal Vedic D1 (Rashi), default Lahiri ayanamsa
- **Ephemeris:** `jyotish.ephemeris.provider=MEEUS` (default) or `SWISS` (optional Thomas Mack pure-Java JAR — see `third_party/swiss-ephemeris/README.md`)
- **Vargas / Dasha / Yoga / Matching / Transit** frameworks as in Phase 3–7
- **Reports:** OpenPDF from stored snapshots — no recalculation; engine version stays **V1.5**
- **CRM:** clients + appointments (tenant-isolated; not hospital appointment-service)
- **AI:** `LlmProvider` HEURISTIC default (optional HTTP); context from verified snapshot only — never invents ephemeris
- PDF storage: `jyotish.reports.storage-dir` (default `./data/reports`)
- Status: `GET /api/v1/jyotish/status` includes `ephemerisProvider`

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

## AI ask (example)

```bash
curl -X POST http://localhost:8097/api/v1/jyotish/ai/ask \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: JYOTISH-DEMO-01" \
  -d "{\"kundaliId\":1,\"question\":\"What does my current dasha suggest for career?\",\"topic\":\"career\"}"
```

Topics: `general`, `career`, `marriage`, `finance`, `health`, `education`, `family`, `spirituality`.

Default provider: `jyotish.ai.provider=HEURISTIC` (no remote). Optional HTTP: set `JYOTISH_AI_PROVIDER=HTTP` + `JYOTISH_AI_HTTP_URL`. Entitlement stub flag `FEATURE_JYOTISH_AI` (not enforced yet).

## Swiss Ephemeris (optional accuracy)

Default stays **MEEUS**. To enable Swiss (Thomas Mack pure-Java JAR):

1. Download JAR — see `third_party/swiss-ephemeris/README.md`
2. Set:

```properties
jyotish.ephemeris.provider=SWISS
jyotish.ephemeris.swiss-jar-path=third_party/swiss-ephemeris/swisseph-2.01.00-02.jar
```

Or env: `JYOTISH_EPHEMERIS_PROVIDER=SWISS` + `JYOTISH_SWISS_JAR_PATH=...`.  
Status endpoint reports active provider. AGPL license applies to the Swiss JAR — review before production.

## Reports (example)

```bash
curl -X POST http://localhost:8097/api/v1/jyotish/reports \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: JYOTISH-DEMO-01" \
  -d "{\"type\":\"BASIC_KUNDALI\",\"kundaliId\":1}"

curl -OJ "http://localhost:8097/api/v1/jyotish/reports/1/download" \
  -H "X-Tenant-Id: JYOTISH-DEMO-01"

curl -X POST http://localhost:8097/api/v1/jyotish/reports \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: JYOTISH-DEMO-01" \
  -d "{\"type\":\"MATCHING\",\"matchingId\":1}"
```

## Transit (example)

```bash
curl "http://localhost:8097/api/v1/jyotish/kundali/1/transit?date=2026-08-20" \
  -H "X-Tenant-Id: JYOTISH-DEMO-01"

curl -X POST http://localhost:8097/api/v1/jyotish/transit \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: JYOTISH-DEMO-01" \
  -d "{\"kundaliId\":1,\"date\":\"2026-08-20\"}"
```

## Matching (example)

```bash
curl -X POST http://localhost:8097/api/v1/jyotish/matching \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: JYOTISH-DEMO-01" \
  -d "{\"profileIdA\":1,\"profileIdB\":2}"

curl "http://localhost:8097/api/v1/jyotish/matching/1" \
  -H "X-Tenant-Id: JYOTISH-DEMO-01"
```

## Entitlements

`jyotish.entitlement.enabled=false` in local profile.

When enabled, calls subscription-service feature flag `FEATURE_JYOTISH` (catalog TBD).  
AI remote / SaaS gate stub: `FEATURE_JYOTISH_AI` (`jyotish.ai.entitlement-flag`).
