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

## Verified on device

Rendered on a Wear OS 4 emulator (384x384) with KVM: the face is listed in the picker,
the date, ring, labels and countdown render correctly.

Things learned while testing:

- `<Parameter>` elements must all come **after** the format string (`%s, %s. %s %s` with all
  parameters at the end). Interleaved parameters render as `Error`.
- A whitespace-only `<Font>` is dropped; put the space at the start of the next span.
- Wear OS 4 only understands format version 1, so the face has to be built with
  `format.version=1`, `minSdk 33` and `targetSdk <= 33` to boot there. Wear OS 6 needs version 4.
- `GOAL_PROGRESS` does not exist in version 1 and has to be dropped for such a test build.

## Validation

```bash
java -jar wff-validator.jar 4 src/main/res/raw/watchface.xml
```
