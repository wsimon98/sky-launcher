# Sky Launcher

A programmable free-canvas Android home screen, based on Lightning Launcher,
modernized for today's devices with optional modern modules.

Sky Launcher keeps the unique Lightning Launcher / LLX experience — free item
placement, multiple desktops, panels, folders, widgets, gestures and a full
scripting engine — alive on modern Android, and adds modern conveniences on
top: a radial app wheel (EdgeWheel), a typed command palette, local search,
file-tree folders, app tags, an app-drawer alphabet scrubber, built-in icon
styles, and Material You theming. Everything modern is optional and can be
toggled per feature.

## Features

- **Free canvas** — place, scale, rotate and style items anywhere; multiple
  desktops, panels, folders and widgets.
- **Modern gestures** — swipe up for the app drawer, swipe down for
  notifications, two-finger swipes and double-tap for optional tools, plus
  pull-past-edge gestures in the app drawer.
- **EdgeWheel** — radial quick launcher with a chosen set of favorite apps.
- **Command Palette** — typed commands like `:edit`, `:backup`, `.app maps`.
- **GlobalSearch** — fast local search across apps, shortcuts, folders,
  panels, scripts and tags. No internet.
- **File-System Folders** — organize apps, commands and links in a tree,
  alongside (not replacing) classic folders.
- **Tags** — tag apps with keywords; search `#tag`, or tag an app `hidden` to
  keep it out of the drawer and search.
- **Colors & Wallpaper** — pick the header/bar color, status and navigation
  bar colors, wallpaper, built-in icon styles (Black & White, Matte, Dark
  mono) and external icon packs, all in one place.
- **Scripting** — the classic LLX JavaScript engine is preserved.
- **Privacy-first** — no internet permission, no telemetry, no ads, no
  accounts, no cloud. Backups, layouts, scripts and logs stay on the device.

## Building

```
cd app/llx
./gradlew assembleDebug
```

Requires JDK 17+, the Android SDK (compileSdk 35) and the NDK. The debug APK
is written to `app/llx/app/build/outputs/apk/debug/`.

## Credits

Sky Launcher is a fork of
[Lightning Launcher](https://github.com/pierrehebert/LightningLauncher) by
**Pierre Hébert**, with contributions from **TrianguloY** and **F43nd1r**.
It builds gratefully on that work.

Bundled within the Lightning Launcher source tree (under their own licenses):

- [lsvg](https://github.com/pierrehebert/lsvg) — SVG library (Apache-2.0), Pierre Hébert
- Rhino JavaScript engine (MPL 2.0) and rhino-android by F43nd1r (Apache-2.0)
- SystemBarTint, the Locale/Tasker plugin API, and a ColorPicker preference (Apache-2.0)

Several optional modules were built from scratch, inspired by the behavior of
other open-source launchers (no code copied):

- EdgeWheel — inspired by [Pie Launcher](https://github.com/markusfisch/PieLauncher)
- Command Palette — inspired by [T-UI Console Launcher](https://github.com/fandreuz/TUI-ConsoleLauncher)
- File-System Folders — inspired by [SimpleFolderLauncher](https://github.com/Robby-Blue/SimpleFolderLauncher)
- GlobalSearch — inspired by [Kvaesitso](https://github.com/MM2-0/Kvaesitso)
- Tags & app hiding — inspired by [Neo Launcher](https://github.com/NeoApplications/Neo-Launcher)

## License

MIT, the same license as upstream Lightning Launcher — see `LICENSE.md`.
