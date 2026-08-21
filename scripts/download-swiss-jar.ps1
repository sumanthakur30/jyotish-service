# Download Thomas Mack Swiss Ephemeris JAR (optional accuracy track).
# Default runtime stays MEEUS — this only fetches the opt-in JAR.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$DestDir = Join-Path $Root "third_party\swiss-ephemeris"
$Jar = Join-Path $DestDir "swisseph-2.01.00-02.jar"
$Uri = "http://www.th-mack.de/download/swisseph-2.01.00-02.jar"

New-Item -ItemType Directory -Force -Path $DestDir | Out-Null
if (Test-Path $Jar) {
  Write-Host "Already present: $Jar"
  exit 0
}
Write-Host "Downloading $Uri ..."
Invoke-WebRequest -Uri $Uri -OutFile $Jar
Write-Host "Saved $Jar"
Write-Host "Enable with:"
Write-Host "  jyotish.ephemeris.provider=SWISS"
Write-Host "  jyotish.ephemeris.swiss-jar-path=third_party/swiss-ephemeris/swisseph-2.01.00-02.jar"
