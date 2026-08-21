# Swiss Ephemeris data files (.se1) — packaging notes

Thomas Mack JAR (already documented in README.md) can run in **Moshier** mode without
`.se1` files. Full Swiss Ephemeris files improve long-term planetary accuracy.

## License decision (not legal advice)

| Option | When to use | Notes |
|--------|-------------|--------|
| **Keep MEEUS** | Default SaaS / closed source | No AGPL Swiss dependency |
| **Swiss JAR + Moshier** | Higher accuracy, still AGPL Free Edition | Review AGPL obligations for distribution |
| **Swiss JAR + `.se1` files** | Highest accuracy (this branch) | Same AGPL Free Edition; dual license from Astrodienst if closed-source SaaS cannot comply |
| **Astrodienst commercial** | Closed-source / SaaS that cannot use AGPL | Contact Astrodienst; do not assume Free Edition covers production SaaS |

Authoritative license text: https://www.astro.com/swisseph/swephinfo_e.htm

**SugamFlow default remains `MEEUS`.** Swiss (with or without files) is opt-in via config.

## Directory layout (gitignored)

```
third_party/swiss-ephemeris/
  swisseph-2.01.00-02.jar     # download script / manual
  ephe/                       # .se1 data files (do NOT commit — large)
    seas_18.se1
    semo_18.se1
    sepl_18.se1
    ...
  README.md
  LICENSE-DECISION.md         # this file
```

## Enable locally

```properties
jyotish.ephemeris.provider=SWISS
jyotish.ephemeris.swiss-jar-path=third_party/swiss-ephemeris/swisseph-2.01.00-02.jar
jyotish.ephemeris.swiss-ephe-path=third_party/swiss-ephemeris/ephe
jyotish.ephemeris.swiss-use-files=true
```

Env:

```bash
export JYOTISH_EPHEMERIS_PROVIDER=SWISS
export JYOTISH_SWISS_JAR_PATH=third_party/swiss-ephemeris/swisseph-2.01.00-02.jar
export JYOTISH_SWISS_EPHE_PATH=third_party/swiss-ephemeris/ephe
export JYOTISH_SWISS_USE_FILES=true
```

Download helpers: `scripts/download-swiss-jar.ps1`, `scripts/download-swiss-ephe.ps1`.

## Docker

Prefer a **volume mount** for `ephe/` (do not bake multi‑MB `.se1` into the image):

```yaml
volumes:
  - ./third_party/swiss-ephemeris/ephe:/opt/swiss/ephe:ro
environment:
  JYOTISH_EPHEMERIS_PROVIDER: SWISS
  JYOTISH_SWISS_JAR_PATH: /opt/swiss/swisseph.jar
  JYOTISH_SWISS_EPHE_PATH: /opt/swiss/ephe
  JYOTISH_SWISS_USE_FILES: "true"
```

Kundali snapshots stamp `ephemerisProvider` (+ `swissUsingFiles`) into `input_json` meta when generated.
