# Jyotish platform subscription catalog

Additive seed for Super Admin → **Platform Subscription**. Does not alter School / CRM / shop plans.

## Plan codes (assign to tenant)

| Plan id | Code | Flags |
|---------|------|-------|
| `jyotish-starter` | `JYOTISH_STARTER` | `FEATURE_JYOTISH` |
| `jyotish-professional` | `JYOTISH_PROFESSIONAL` | + `FEATURE_JYOTISH_MATCHING`, `FEATURE_JYOTISH_REPORTS` |
| `jyotish-enterprise` | `JYOTISH_ENTERPRISE` | + `FEATURE_JYOTISH_AI` |

## Feature flags (jyotish-service)

| Flag | Gates |
|------|--------|
| `FEATURE_JYOTISH` | Kundali generate/read, transit |
| `FEATURE_JYOTISH_MATCHING` | Matching APIs |
| `FEATURE_JYOTISH_REPORTS` | PDF reports |
| `FEATURE_JYOTISH_AI` | `POST /ai/ask` |

## Enable in jyotish-service

```properties
jyotish.entitlement.enabled=true
jyotish.entitlement.base-url=http://<subscription-service-host>:8182
# optional: JYOTISH_ENTITLEMENT_FAIL_OPEN=true
```

Env: `JYOTISH_ENTITLEMENT_ENABLED=true`, `JYOTISH_SUBSCRIPTION_BASE_URL=...`

Local default remains `jyotish.entitlement.enabled=false` (`application-local.properties`).

## SQL

Same content as Flyway `V23__jyotish_standalone_catalog.sql` in school `subscription-service` (branch `feature/jyotish-entitlements`).
