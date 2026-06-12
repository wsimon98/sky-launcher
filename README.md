# Sky Launcher

A programmable free-canvas Android home screen, based on Lightning Launcher,
with optional modern modules.

Sky Launcher is a fork of
[Lightning Launcher](https://github.com/pierrehebert/LightningLauncher) by
Pierre Hébert (with contributions from TrianguloY and F43nd1r). The goal is to
keep the classic Lightning Launcher / LLX experience alive on modern Android —
free item placement, multiple desktops, panels, folders, widgets, gestures and
scripting — while adding new tools that are strictly optional.

The promise:

> If you want classic LLX, you can use Sky Launcher like classic LLX.
> If you want newer tools, you can turn them on one at a time.

## Status

Early development. The launcher builds with a modern toolchain (Gradle 8 /
AGP 8 / SDK 35) as a single, fully unlocked app: the old trial/eXtreme split,
Play licensing and in-app billing are gone.

- `PROJECT_PLAN.md` — full design and roadmap
- `MIGRATION_REPORT.md` — what changed relative to upstream and why
- `BUILD_ERRORS.md` — build status log
- `LICENSE_AUDIT.md`, `THIRD_PARTY_NOTICES.md` — licensing

## Building

```
cd app/llx
./gradlew assembleDebug
```

Requires JDK 17+, Android SDK (compileSdk 35) and NDK. The debug APK lands in
`app/build/outputs/apk/debug/`.

## Privacy

No internet permission. No telemetry, no ads, no accounts, no cloud. Backups,
layouts, scripts and logs stay on the device.

## License

MIT, same as upstream Lightning Launcher — see `LICENSE.md` and
`THIRD_PARTY_NOTICES.md`. The upstream repository remains the work of its
original authors; Sky Launcher gratefully builds on it.
