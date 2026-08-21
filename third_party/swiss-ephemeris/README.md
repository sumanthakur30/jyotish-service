# Swiss Ephemeris (optional)

Thomas Mack’s **pure-Java** port of Swiss Ephemeris — used when
`jyotish.ephemeris.provider=SWISS`. Default runtime stays **MEEUS** (no JAR required).

## Why not Maven Central / JNI?

- No maintained Swiss Ephemeris artifact on Maven Central.
- `krymlov/swe-java-lib` (JitPack) depends on unpublished `swisseph:swe-api` and is awkward to wire.
- JNI (`libswe.so` / `.dll`) is heavier on Windows + multi-arch Docker for this first accuracy slice.

## Download (local / Docker)

```powershell
# From jyotish-service root
New-Item -ItemType Directory -Force -Path third_party\swiss-ephemeris | Out-Null
Invoke-WebRequest `
  -Uri "http://www.th-mack.de/download/swisseph-2.01.00-02.jar" `
  -OutFile "third_party\swiss-ephemeris\swisseph-2.01.00-02.jar"
```

Optional full SE data files (higher precision than built-in Moshier):

```powershell
.\scripts\download-swiss-ephe.ps1
# or: bash scripts/download-swiss-ephe.sh
```

Files land in `third_party/swiss-ephemeris/ephe/` (**gitignored** — do not commit `.se1`).
License decision matrix: [`LICENSE-DECISION.md`](./LICENSE-DECISION.md).

## Enable

```properties
jyotish.ephemeris.provider=SWISS
jyotish.ephemeris.swiss-jar-path=third_party/swiss-ephemeris/swisseph-2.01.00-02.jar
# Optional SE files (this branch):
jyotish.ephemeris.swiss-ephe-path=third_party/swiss-ephemeris/ephe
jyotish.ephemeris.swiss-use-files=true
```

Env equivalents: `JYOTISH_EPHEMERIS_PROVIDER`, `JYOTISH_SWISS_JAR_PATH`,
`JYOTISH_SWISS_EPHE_PATH`, `JYOTISH_SWISS_USE_FILES`.

Without the JAR (or with a bad path), startup with `provider=SWISS` fails with a clear
`EphemerisUnavailableException` — Meeus is never silently substituted.

When `swiss-use-files=true` but the directory is missing/empty, startup fails with a clear
path error (configure Moshier by leaving `swiss-use-files=false`).

## Docker sketch

```dockerfile
# After copying the app JAR:
COPY third_party/swiss-ephemeris/swisseph-2.01.00-02.jar /opt/swiss/swisseph.jar
# Prefer volume-mounting ephe/ at runtime (do not bake large .se1 into the image):
#   -v ./ephe:/opt/swiss/ephe:ro
ENV JYOTISH_EPHEMERIS_PROVIDER=SWISS
ENV JYOTISH_SWISS_JAR_PATH=/opt/swiss/swisseph.jar
ENV JYOTISH_SWISS_EPHE_PATH=/opt/swiss/ephe
ENV JYOTISH_SWISS_USE_FILES=true
```

No native library packaging — pure-Java JAR only. Default remains **MEEUS**.

## License

Swiss Ephemeris Free Edition is **AGPL**. Commercial / closed-source SaaS may need Astrodienst’s
dual license. See [`LICENSE-DECISION.md`](./LICENSE-DECISION.md) and
https://www.astro.com/swisseph/swephinfo_e.htm before production enablement.

The JAR and `.se1` files under this folder are **gitignored** — download locally; do not commit binaries.
