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

Optional full SE data files (higher precision than built-in Moshier): download from
[Astrodienst Swiss Ephemeris](https://www.astro.com/swisseph/) into e.g.
`third_party/swiss-ephemeris/ephe/`.

## Enable

```properties
jyotish.ephemeris.provider=SWISS
jyotish.ephemeris.swiss-jar-path=third_party/swiss-ephemeris/swisseph-2.01.00-02.jar
# Optional SE files:
# jyotish.ephemeris.swiss-ephe-path=third_party/swiss-ephemeris/ephe
# jyotish.ephemeris.swiss-use-files=true
```

Env equivalents: `JYOTISH_EPHEMERIS_PROVIDER`, `JYOTISH_SWISS_JAR_PATH`,
`JYOTISH_SWISS_EPHE_PATH`, `JYOTISH_SWISS_USE_FILES`.

Without the JAR (or with a bad path), startup with `provider=SWISS` fails with a clear
`EphemerisUnavailableException` — Meeus is never silently substituted.

## Docker sketch

```dockerfile
# After copying the app JAR:
COPY third_party/swiss-ephemeris/swisseph-2.01.00-02.jar /opt/swiss/swisseph.jar
# optional: COPY ephe/ /opt/swiss/ephe/
ENV JYOTISH_EPHEMERIS_PROVIDER=SWISS
ENV JYOTISH_SWISS_JAR_PATH=/opt/swiss/swisseph.jar
```

No native library packaging in this slice — pure-Java JAR only.

## License

Swiss Ephemeris Free Edition is **AGPL**. Commercial / closed-source SaaS may need Astrodienst’s
dual license. Review https://www.astro.com/swisseph/swephinfo_e.htm before production enablement.

The JAR under this folder is **gitignored** — download locally; do not commit binaries.
