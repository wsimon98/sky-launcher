# Sky Launcher — Migration Report

Date: 2026-06-11
Base: pierrehebert/LightningLauncher @ 1a1d482 (cloned with full history, fork lineage preserved)
Local checkout: /root/projects/sky-launcher

## Repo layout (as inherited)

```
app/llx/            <- the actual Android project (settings.gradle lives HERE, not at repo root)
  app/              <- launcher app module (flavored: extreme/trial x abi)
  core/             <- the big one: LLX engine, 368 java files, vendored Rhino JS, NDK code
  plugin-api/       <- remoter/parceler based plugin IPC api
  wear/             <- DEAD (per upstream README, fully out of date)
  wear_manager/     <- DEAD
  staging/          <- patch files, not a module
  api_doc/          <- javadoc tooling for script API
graphics/           <- source artwork
permissions/        <- helper app (separate project)
scripts/            <- sample LLX scripts (each its own tiny gradle project; NOT part of app build)
translations/       <- language packs
```

## Inherited toolchain (blockers)

| Thing | Was | Problem |
|---|---|---|
| Gradle wrapper | 4.10.2 (2017) | Won't run on Java 21 |
| AGP | 3.3.2 | Won't run on Gradle 8 / modern SDK |
| compileSdk | 28 | Too old for current platforms dir (we have 33–36) |
| minSdk | 14 | AGP 8.x floor is ~21; bumped |
| jcenter() | dependency repo | Dead/read-only; removed |
| `compile` configs | everywhere | Removed in Gradle 7+; converted |
| `net.pierrox.android:lsvg:1.0` | jcenter-only artifact | 404 on Maven Central → vendored from pierrehebert/lsvg (Apache-2.0) as local module `:lsvg` |
| manifest `package=` attr | all manifests | AGP 8 requires `namespace` in build.gradle instead |
| ABI flavors (arm/armv7/armv64/x86/x86_64) | app/build.gradle | Replaced by abiSplits/none; removed (single universal APK) |
| trial/eXtreme flavor split | app/build.gradle + src/trial, src/extreme | Removed (see below) |
| Play licensing (LVL) + IAB billing | app/src/extreme, app/src/trial | Dead Google hooks; excluded from build, replaced by LLAppSky stub |
| NDK module `ll` (ll.c) via cmake | core | Kept; CMakeLists may need cmake_minimum_required bump |
| Vendored Rhino (org.mozilla.javascript) | core/src/main/java | Kept as-is — this IS the LLX script engine |
| dx.jar (local lib) | core/libs | Kept — used by rhino_android to dex compiled scripts at runtime. May need D8 migration later on newer targetSdk. |
| android.support usage | 1 file (script/api/palette/Palette.java) | Vendored support-palette code; self-contained, kept |

## Flavor removal decision

Upstream shipped two products from one tree: LL (trial, with IAB unlock) and LLX
(extreme, with Play licensing + LWP + plugin ApiProvider). Sky Launcher is one free
app with everything unlocked. Approach:

- Single app, no `productFlavors`.
- `app/src/extreme` and `app/src/trial` are NOT registered as source sets.
  The dirs stay in the tree for reference/history, the build ignores them.
- New `LLAppSky` (app/src/main) extends LLAppPhone: everything reports as the
  full (extreme) feature set, no license check, no IAB, no trial countdown.
  `isFreeVersion()=false`, `isTrialVersion()=false`, locked-feature dialogs never fire.
- Useful extreme-only components folded into main where wanted later
  (ApiProvider for the plugin API, LightningLWPService for live wallpaper).
  v0.1 ships without them; tracked in BUILD_ERRORS.md / backlog.
- BuildConfig flags preserved: IS_TRIAL=false, IS_BETA=false, HAS_UEC=false.

## SDK targets (staged)

- compileSdk: 35 (installed)
- minSdk: 21
- targetSdk: **28 for v0.x on purpose.** Keeps classic behavior alive: no
  package-visibility `<queries>` requirement, no scoped-storage enforcement, no
  `android:exported` install-blocker, sideloads fine on Android 8–16 (Android 14+
  only blocks targetSdk < 23). Raising targetSdk is its own later phase with
  manifest exported flags, queries, storage migration.

## What is intentionally NOT changed

- Java sources stay Java. No Kotlin rewrite, no Compose.
- Classic LLX behavior untouched: canvas, desktops, panels, folders, scripts,
  gestures, item transforms, backup format.
- No optional Sky modules (EdgeWheel etc.) until Classic LLX core loop works
  (PROJECT_PLAN.md Phase 3).

## Status

See BUILD_ERRORS.md for the live error log.
