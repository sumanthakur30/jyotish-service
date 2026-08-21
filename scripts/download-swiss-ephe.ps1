# Download minimal Swiss Ephemeris .se1 data files into third_party/swiss-ephemeris/ephe/
# Files are large and AGPL — do NOT commit. See third_party/swiss-ephemeris/LICENSE-DECISION.md.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$EpheDir = Join-Path $Root "third_party\swiss-ephemeris\ephe"
# Astrodienst public FTP mirror (HTTP). Minimal planetary + moon + asteroid main files.
$Base = "https://www.astro.com/ftp/swisseph/ephe"
$Files = @(
  "seas_18.se1",
  "semo_18.se1",
  "sepl_18.se1"
)

New-Item -ItemType Directory -Force -Path $EpheDir | Out-Null
foreach ($name in $Files) {
  $dest = Join-Path $EpheDir $name
  if (Test-Path $dest) {
    Write-Host "Skip (exists): $name"
    continue
  }
  $uri = "$Base/$name"
  Write-Host "Downloading $uri ..."
  try {
    Invoke-WebRequest -Uri $uri -OutFile $dest
    Write-Host "Saved $dest"
  } catch {
    Write-Warning "Failed $name : $($_.Exception.Message)"
    Write-Warning "Manual download: https://www.astro.com/swisseph/ — place .se1 under $EpheDir"
  }
}

Write-Host ""
Write-Host "Enable full SE files with:"
Write-Host "  jyotish.ephemeris.provider=SWISS"
Write-Host "  jyotish.ephemeris.swiss-jar-path=third_party/swiss-ephemeris/swisseph-2.01.00-02.jar"
Write-Host "  jyotish.ephemeris.swiss-ephe-path=third_party/swiss-ephemeris/ephe"
Write-Host "  jyotish.ephemeris.swiss-use-files=true"
Write-Host "Review AGPL / commercial license before production (LICENSE-DECISION.md)."
