# Manglik cancellation rules (V1.7)

Applied when Mars is in a classical Manglik house (1, 2, 4, 7, 8, 12 whole-sign). Without a
matching house placement, cancellations are not evaluated.

## Single-chart rules (`ManglikAnalyzer.assess`)

| Code | Rule |
|------|------|
| `MARS_OWN_SIGN` | Mars in Aries (0) or Scorpio (7) |
| `MARS_EXALTED` | Mars in Capricorn (9) |
| `MARS_IN_LEO` | Mars in Leo (4) — common optional cancellation |
| `JUPITER_WITH_MARS` | Jupiter same sign as Mars |

When any single-chart rule applies, status becomes `CANCELLED`, `cancelled=true`, and
`cancellationsComingSoon=false`.

## Matching-pair rule (`ManglikMatchingCalculator`)

| Code | Rule |
|------|------|
| `MUTUAL_MANGLIK` | Both persons have Manglik **placement** (Mars in relevant houses). Applied even if one side was already cancelled by a single-chart rule — classical “both Manglik” exception for matching purpose. |

## Not implemented

Other traditional exceptions (benefic aspects, Mars in specific Navamsha, etc.) remain out of
scope and are not silently invented.
