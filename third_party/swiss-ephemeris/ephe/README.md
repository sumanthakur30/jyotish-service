# Swiss Ephemeris data directory

Place Astrodienst `.se1` files here (gitignored). Download with:

```powershell
.\scripts\download-swiss-ephe.ps1
```

Then set `jyotish.ephemeris.swiss-ephe-path=third_party/swiss-ephemeris/ephe` and
`jyotish.ephemeris.swiss-use-files=true`. See `../LICENSE-DECISION.md`.
