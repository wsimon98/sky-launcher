# Sky Launcher — License Audit

Date: 2026-06-11

## Base project

| Source | License | Status |
|---|---|---|
| pierrehebert/LightningLauncher | **MIT** (LICENSE.md, © 2022 Pierre Hébert) | BASE — fork. Sky Launcher stays MIT. |

Upstream README credits original author Pierre with contributions from TrianguloY
and F43nd1r. Keep LICENSE.md and credit in README/About.

## Code already vendored inside the LL tree (inherited)

These were shipped inside `core/src/main/java` by upstream; they ride along under
their own headers:

| Path | What | License (per file headers) |
|---|---|---|
| org/mozilla/javascript, org/mozilla/classfile | Rhino JS engine (script system) | MPL 2.0 |
| com/faendir/rhino_android | Rhino-on-Android glue (F43nd1r) | Apache-2.0 |
| com/readystatesoftware/systembartint | SystemBarTint | Apache-2.0 |
| com/twofortyfouram/locale | Tasker/Locale plugin API | Apache-2.0 |
| net/margaritov/preference | ColorPicker preference | Apache-2.0 |
| net/dinglisch/android | Tasker intent helpers | (permissive, header in files) |
| app/src/main/java/fr/xgouchet/texteditor | AdvancedEditText (script editor) | GPL? — header says "works derived from Ted" — VERIFY before redistribution beyond what upstream already did |
| core/libs/dx.jar | AOSP dx tool | Apache-2.0 |
| app: com/boombuler/appwidgetpicker | Widget picker dialog | (header in files) |
| script/api/palette/Palette.java | AOSP support-lib Palette copy | Apache-2.0 |

Action: fr/xgouchet header check is a TODO before GitHub publish (upstream already
distributed it under MIT repo umbrella; we keep its header intact regardless).

## Newly vendored by Sky Launcher

| Source | Commit | License | Where |
|---|---|---|---|
| pierrehebert/lsvg (lsvg library module) | master @ clone 2026-06-11 | **Apache-2.0** (per README + file headers; no LICENSE file in repo) | `app/llx/lsvg/` as Gradle module `:lsvg`, replaces dead jcenter artifact `net.pierrox.android:lsvg:1.0` |

## Planned borrow sources (from PROJECT_PLAN.md) — status

| Repo | License | Ruling |
|---|---|---|
| markusfisch/PieLauncher | **MIT** (verified on repo) — re-verify file headers before copy | code-copy candidate (EdgeWheel) |
| Robby-Blue/SimpleFolderLauncher | check at copy time | code-copy candidate |
| fandreuz/TUI-ConsoleLauncher | check at copy time | code-copy candidate |
| MM2-0/Kvaesitso | GPL — | idea-only |
| FossifyOrg/Launcher | GPL — | idea-only |
| NeoApplications/Neo-Launcher | GPL — | idea-only |
| LawnchairLauncher/lawnchair | mixed | study-only |

No code copied from any of these yet.

## Rules in force

1. Check repo license AND file headers before any copy.
2. Copied files get the source-comment header (repo/path/commit/license).
3. Every copy gets a THIRD_PARTY_NOTICES.md entry.
4. GPL sources stay idea-only while the project is MIT.
