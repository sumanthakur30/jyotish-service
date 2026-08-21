#!/usr/bin/env bash
# Download minimal Swiss Ephemeris .se1 files (do not commit). See LICENSE-DECISION.md.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
EPHE="$ROOT/third_party/swiss-ephemeris/ephe"
BASE="https://www.astro.com/ftp/swisseph/ephe"
mkdir -p "$EPHE"
for name in seas_18.se1 semo_18.se1 sepl_18.se1; do
  dest="$EPHE/$name"
  if [[ -f "$dest" ]]; then
    echo "Skip (exists): $name"
    continue
  fi
  echo "Downloading $BASE/$name ..."
  if ! curl -fsSL -o "$dest" "$BASE/$name"; then
    echo "WARN: failed $name — download manually from https://www.astro.com/swisseph/" >&2
    rm -f "$dest"
  else
    echo "Saved $dest"
  fi
done
echo "Enable: JYOTISH_EPHEMERIS_PROVIDER=SWISS JYOTISH_SWISS_USE_FILES=true JYOTISH_SWISS_EPHE_PATH=$EPHE"
