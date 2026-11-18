# Watch Face Format prototype

Standalone, code-free watch face (Watch Face Format version 4, Wear OS 6+).

`res/raw/watchface.xml` is generated from the dial geometry of the Wear app: 12 o'clock is
the midpoint between Maghrib and the next Fajr, 6 o'clock is Dhuhr, and six prayer arcs
show their own progress. Arcs and labels are drawn with `PartDraw`/`PartText`, the fills are
`clamp(([SECONDS_IN_DAY] - start) / duration, 0, 1)` expressions.

## Limitations of this prototype

- Prayer times are hardcoded (Fajr 04:30, sunrise 06:10, Dhuhr 13:15, Asr 17:00,
  Maghrib 19:45, Ishaa 21:15) and the labels are German.
- The Hijri date and the countdown line are placeholders.
- Complications render `SHORT_TEXT` only.

## Next steps

- Ship the times through Watch Face Push: the phone app pushes the face with today's times as
  `[REFERENCE.*]` values and the expressions compute the angles from them.
  See https://developer.android.com/training/wearables/watch-face-push
- Localise the strings and render the Hijri date and countdown from pushed values.

## Validation

```bash
java -jar wff-validator.jar 4 src/main/res/raw/watchface.xml
```
