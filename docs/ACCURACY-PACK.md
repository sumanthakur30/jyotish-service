# Accuracy pack (CalculationEngine V1.7)

Industry-parity regression anchors for sidereal D1 (Lahiri) against Swiss Ephemeris
**Moshier** mode. Meeus remains the **default** production provider; Swiss is optional.

## Default provider: MEEUS

- Config: `jyotish.ephemeris.provider=MEEUS` (or unset / `JYOTISH_EPHEMERIS_PROVIDER`).
- Truncated VSOP-style Meeus formulas — fine for UX and structural tests, **not** claimed as
  industry-gold for outer planets or Ascendant.

## Swiss Ephemeris (optional)

| Mode | Flag | Notes |
|------|------|--------|
| Moshier | `jyotish.ephemeris.swiss-use-files=false` (default) | Pure-Java port, no `.se1` files; used as Lahiri gold in tests |
| Full SE files | `swiss-use-files=true` + `swiss-ephe-path` | `SEFLG_SWIEPH` when `seas_*.se1` / related files are present |

Props / env:

```properties
jyotish.ephemeris.provider=SWISS
jyotish.ephemeris.swiss-jar-path=third_party/swiss-ephemeris/swisseph-2.01.00-02.jar
jyotish.ephemeris.swiss-ephe-path=third_party/swiss-ephemeris/ephe
jyotish.ephemeris.swiss-use-files=false
```

```bash
JYOTISH_EPHEMERIS_PROVIDER=SWISS
JYOTISH_SWISS_JAR_PATH=third_party/swiss-ephemeris/swisseph-2.01.00-02.jar
JYOTISH_SWISS_EPHE_PATH=third_party/swiss-ephemeris/ephe
JYOTISH_SWISS_USE_FILES=true
```

Download helpers: `scripts/download-swiss-jar.ps1`, `scripts/download-swiss-ephe.ps1`.
License: `third_party/swiss-ephemeris/LICENSE-DECISION.md`.

## Tolerances vs Swiss-Moshier Lahiri gold

Frozen sample: Delhi `1990-08-15 10:30 Asia/Kolkata` lat=`28.6139` lon=`77.2090`
(see `LahiriGoldenChartTest`).

| Body | Meeus vs gold | Swiss self-regression |
|------|---------------|------------------------|
| Sun | ±0.5° | ±0.05° |
| Moon | ±1.0° | ±0.05° |
| Rahu / Ketu | ±1.0° | ±0.05° |
| Mars, Mercury, Jupiter, Venus, Saturn | **Not asserted** under Meeus | ±0.05° |
| Ascendant | **Not asserted** under Meeus — ASC can differ substantially | ±0.05° |

Meeus outer-planet and house-cusp formulas are intentionally soft: failing CI on them would
over-claim accuracy. Swiss JAR present → all listed bodies (incl. ASC) must match gold within
±0.05°.

## How to enable Swiss profile

1. Place Thomas Mack JAR at `third_party/swiss-ephemeris/swisseph-2.01.00-02.jar`.
2. Set `jyotish.ephemeris.provider=SWISS` (local profile or env).
3. Leave `swiss-use-files=false` for Moshier, or point `swiss-ephe-path` + `swiss-use-files=true`
   for full SE files.
4. Confirm `GET /api/v1/jyotish/status` reports `ephemerisProvider=SWISS`.
