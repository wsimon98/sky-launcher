# Sky Launcher — Build Errors / Status Log

Newest entries at top. "OPEN" items block or degrade the build; "DEFERRED" items
are accepted for v0.x and tracked for later phases.

## 2026-06-11 — initial migration (in progress)

DEFERRED:
- targetSdk pinned at 28 for v0.x (classic behavior; sideload-safe). Raising it
  needs: android:exported on all manifest components, <queries> for package
  visibility, scoped-storage migration for backup paths, POST_NOTIFICATIONS etc.
- extreme-only features not yet re-wired: plugin ApiProvider, Lightning Live
  Wallpaper service, LWPSettings. App builds without them; plugin API and LWP
  are disabled until re-folded into main.
- wear/ and wear_manager/ remain in tree but are not referenced by settings.gradle
  (upstream already excluded them). Marked dead.
- scripts/ sample projects and permissions/ helper app are separate gradle
  projects; not part of the app build, untouched.
- dx.jar-based runtime script dexing untested on Android 14+ ART.

OPEN:
- (none — debug build green)

## 2026-06-11 — RESULT: BUILD SUCCESSFUL

Toolchain: Gradle 8.9 / AGP 8.7.3 / JDK 21 / NDK 28.2.13676358 / cmake 3.22.1
Modules built: :app :core :plugin-api :lsvg (vendored)
APK: app/llx/app/build/outputs/apk/debug/app-debug.apk (~2.0 MB)
  package=app.skylauncher versionName="0.1.0 (LL 14.3 base)"
  label="Sky Launcher" minSdk=21 targetSdk=28 compileSdk=35
  NO internet permission (verified via aapt2 badging)
Deployed: http://10.0.0.103/apks/sky-launcher.apk (+ versioned copy)

Fixes applied during iteration:
1. Missing layouts (event_action_item, two_lines_list_item_font,
   add_dialog_item): main java referenced layouts that lived in
   src/extreme/res → registered src/extreme/res as extra res dir of main
   (java from src/extreme stays excluded).
2. ll.c: implicit memcpy/memset declarations are hard errors on modern
   clang/NDK 28 → added #include <string.h>.

NOT yet runtime-tested on a device: install, set-as-home, edit mode,
layout persistence. That is the next phase (Classic LLX core loop).
