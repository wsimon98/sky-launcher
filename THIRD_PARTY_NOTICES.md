# Third-Party Notices

Sky Launcher is a fork of Lightning Launcher.

## Lightning Launcher
- Source: https://github.com/pierrehebert/LightningLauncher
- Author: Pierre Hébert, with contributions from TrianguloY and F43nd1r
- License: MIT (see LICENSE.md)
- Role: base project — Sky Launcher is this codebase, modernized.

## lsvg
- Source: https://github.com/pierrehebert/lsvg (module `lsvg/`)
- Vendored: 2026-06-11 into `app/llx/lsvg/` (replaces unpublishable jcenter artifact `net.pierrox.android:lsvg:1.0`)
- License: Apache License 2.0 (per repo README and file headers)
- Author: Pierre Hébert

## Vendored inside the inherited Lightning Launcher tree
(shipped by upstream inside core/ and app/; original headers preserved in files)

- Rhino JavaScript engine — Mozilla Public License 2.0 — `core/src/main/java/org/mozilla/*`
- rhino-android (F43nd1r) — Apache-2.0 — `core/src/main/java/com/faendir/rhino_android`
- SystemBarTint (readystatesoftware) — Apache-2.0
- Locale/Tasker plugin API (twofortyfouram) — Apache-2.0
- ColorPickerPreference (Daniel Nilsson / margaritov) — Apache-2.0
- Tasker helpers (net.dinglisch.android)
- AdvancedEditText (fr.xgouchet, "Ted" editor)
- AppWidgetPicker (com.boombuler)
- AOSP Palette + dx.jar — Apache-2.0

## Runtime dependencies
- Remoter (com.josesamuel) — Apache-2.0 — plugin IPC
- Parceler (org.parceler) — Apache-2.0 — plugin IPC

No code has been copied from Kvaesitso, Fossify Launcher, Neo Launcher, or
Lawnchair (idea/study-only sources per LICENSE_AUDIT.md).
