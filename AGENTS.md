# Agent notes

Notes for automated agents working on this repository.

## Build

- JDK 17 and Android SDK 37 (`compileSdk 37`).
- `bash ci_build.sh wear` publishes `open-prayer-times` to the local Maven repository, creates the
  missing `secrets.xml` files and builds the Wear app.
- `./gradlew` is not executable in this checkout: always call `bash ./gradlew ...`.
- Without `wear/src/main/res/values/secrets.xml` (empty `GOOGLE_API_KEY`) the build fails in
  `QiblaMap.kt`.
- Long-running builds: prefer `background: true` and a generous timeout; the first build downloads
  dependencies and takes several minutes.

## Wear emulator

- `/dev/kvm` must be readable and writable (`sudo setfacl -m u:opencode:rw /dev/kvm`), otherwise the
  emulator falls back to software emulation, which takes 30+ minutes to boot.
- The Wear OS 7 image (`system-images;android-37.0;android-wear-signed;x86_64`) does not boot on this
  host (nested KVM without AVX). Wear OS 4 (`system-images;android-33;android-wear;x86_64`) boots in
  about a minute and is enough for layout checks with Watch Face Format version 1.
- Create and run:
  ```bash
  echo no | avdmanager create avd -n wear33 -k "system-images;android-33;android-wear;x86_64" -d wearos_small_round
  setsid nohup emulator -avd wear33 -no-window -no-audio -no-boot-anim -no-snapshot \
    -gpu swiftshader_indirect -memory 1536 -cores 4 > /tmp/opencode/emu.log 2>&1 &
  adb devices                      # the first free port, usually emulator-5556 next to another AVD
  ```
- Watch face picker: `adb shell input swipe 192 192 192 192 2500`, then
  `adb shell uiautomator dump /sdcard/ui.xml` to get element bounds before tapping.
- Kill emulators with patterns that do not match your own shell command line, for example
  `pkill -f "[q]emu-system"`.

## Sharing the screen with the user

- The user reaches this machine at `192.168.178.5`. **Always bind servers to `0.0.0.0`**, not to
  `127.0.0.1`.
- Browser control of the emulator: [ws-scrcpy](https://github.com/NetrisTV/ws-scrcpy)
  ```bash
  git clone --depth 1 https://github.com/NetrisTV/ws-scrcpy.git /tmp/opencode/ws-scrcpy
  cd /tmp/opencode/ws-scrcpy && npm install && npm run dist
  printf 'runGoogTracker: true\nrunApplTracker: false\nserver:\n  - secure: false\n    port: 8002\n' > config.yaml
  cd dist && WS_SCRCPY_CONFIG=/tmp/opencode/ws-scrcpy/config.yaml \
    setsid nohup node index.js > /tmp/opencode/ws-scrcpy.log 2>&1 &
  ```
  Then `http://192.168.178.5:8002/` lists the adb devices and opens a live, interactive view.
- Static web preview of the watch face design: `python3 -m http.server 8000 --bind 0.0.0.0 \
  --directory prayer-times-android/watchface` and open `preview.html`.

## Watch Face Format (WFF)

- `watchface/` contains the code-free watch face. It needs format version 4 for Wear OS 6; for a
  Wear OS 4 test build use version 1, `minSdk 33`, `targetSdk 33` and drop `GOAL_PROGRESS`.
- `<Parameter>` elements must all come after the format string (`%s, %s. %s %s` plus all parameters),
  otherwise the runtime renders `Error`. A whitespace-only `<Font>` is dropped, so put the space at
  the beginning of the next span.
- Validate with the official validator:
  ```bash
  curl -L -o /tmp/opencode/wff-validator.jar https://github.com/google/watchface/releases/download/release/wff-validator.jar
  java -jar /tmp/opencode/wff-validator.jar 4 watchface/src/main/res/raw/watchface.xml
  ```
- Legacy `androidx.wear.watchface` watch faces are not installed on Wear OS 6 at all, only WFF.

### Testing a v4 face on the Wear OS 4 emulator

The emulator only accepts format version 1. For a visual check, temporarily patch the module, build,
install, then revert:

```bash
sed -i 's/android:value="4"/android:value="1"/' watchface/src/main/AndroidManifest.xml
sed -i 's/minSdk 36/minSdk 33/; s/targetSdk 37/targetSdk 33/' watchface/build.gradle
sed -i 's/supportedTypes="GOAL_PROGRESS RANGED_VALUE/supportedTypes="RANGED_VALUE/g' \
  watchface/src/main/res/raw/watchface.xml
# build, adb install -r, then:
git checkout -- watchface/src/main/AndroidManifest.xml watchface/build.gradle
```

Set a specific time for screenshots with `adb root`, then
`adb shell "date MMDDhhmmYYYY.ss"` + `adb shell am broadcast -a android.intent.action.TIME_SET`.
`settings put global auto_time 0` first; restore with `1` afterwards.

## UI review loop

- The reviewer agent lives in `.opencode/agents/wear-ui-reviewer.md` and is read-only by design.
- It must run on a **vision-capable model**. `evren/glm-5.3` cannot read images and silently falls
  back to guessing from the XML; `opencode-go/gemini-4-argon` sees the PNGs.
- Only screenshots from the real emulator count. `watchface/preview.html` is a design proxy, never
  evidence for system rendering.
- Feed it two or three states in one round (e.g. 04:00, 13:30, 22:00) plus `watch.png`, and keep one
  session per review so it remembers earlier rounds.
- It returns severity-ranked findings with numbers in the 450x450 design space; apply, rebuild,
  re-screenshot, repeat until it explicitly approves.
