# RSC Companion (AYN Thor second-screen helper for OpenRSC)

Separate app that runs alongside your OpenRSC APK. Put the game on the top screen and this on the bottom.

## Build
1. Install Android Studio, then File > Open this folder (`RscCompanion`). Let Gradle sync (it creates the wrapper).
2. Build > Build APK(s), or run it straight to the Thor over USB/ADB.
3. Install the APK on the Thor next to your OpenRSC app.

## Use
- Open **RSC Companion**. Server defaults to `192.168.1.245:43595`.
  Change host/port there any time (e.g. when you move to a dedicated host) and tap Save.
- **Launch game (top) + companion (bottom)** starts OpenRSC on the main screen and this on the second one.
  If it picks the wrong screen, or the game doesn't start, check the package name field
  (find it with `adb shell pm list packages | grep -i rsc`).

## Tabs
- **Wiki** - live classic.runescape.wiki
- **Map** - live RuneScape Classic Map page (pinch to zoom)
- **Calc** - offline skill calculator (XP table, XP needed, actions needed, custom server XP-rate multiplier)
- **Browser** - small browser; type a search or a URL
- The coloured dot is a TCP check of your server (green = reachable), refreshed every 10 s.

## Limits
- It does NOT change the server inside the OpenRSC game app; that app's own picker is separate.
- Not compiled/tested by the author - built without an Android SDK. If Gradle complains about a version, bump the
  plugin versions in `build.gradle.kts`.

## Build without Android Studio (GitHub Actions)
1. Create a free GitHub repo and upload the CONTENTS of this folder (so `.github/` is at the repo root).
2. Open the repo's **Actions** tab > "Build APK" > wait for the green tick (a few minutes).
3. Open the run, download the **RscCompanion-debug-apk** artifact, unzip it, and install `app-debug.apk` on the Thor.
