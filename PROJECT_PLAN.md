# Project: Sky Launcher

## Mission

Build Sky Launcher as a modern Android launcher forked from Lightning Launcher.

Primary rule:

```text
Sky Launcher must be able to behave like classic Lightning Launcher / LLX.
```

All new features must be optional.

Nothing borrowed from other launchers should replace the classic LLX experience.

The default user promise:

```text
If the user wants classic LLX, they can use Sky Launcher like classic LLX.
If the user wants newer tools, they can turn them on one at a time.
```

Sky Launcher is not a Pixel Launcher clone.

Sky Launcher is not a search-only launcher.

Sky Launcher is not a normal icon-grid launcher.

Sky Launcher is:

```text
A programmable free-canvas Android home screen, based on Lightning Launcher, with optional modern modules.
```

## Project names

Public app name:

```text
Sky Launcher
```

Repo name:

```text
sky-launcher
```

Internal codename:

```text
Sky
```

Package name (chosen):

```text
app.skylauncher
```

Use `Sky Launcher` in all user-facing text.

Use `Sky` in internal docs and module names where short naming helps.

## Base repo

Primary fork:

```text
https://github.com/pierrehebert/LightningLauncher
```

Use this as the base.

Reason:

* It is the real Lightning Launcher source.
* It contains the unique LLX canvas behavior.
* It contains the old panel, widget, folder, and script systems.
* It is the closest possible base for preserving the LLX feel.

Do not start from Launcher3.

Do not start from Lawnchair.

Do not rebuild the launcher from scratch unless the old code becomes impossible to rescue.

## Core design rule

Sky Launcher has two layers.

```text
Layer 1: Classic LLX Core
Layer 2: Optional Sky Modules
```

Layer 1 must work by itself.

Layer 2 must never be required.

If every optional Sky module is turned off, the launcher should still behave like a modernized version of classic LLX.

## Classic LLX Core

The Classic LLX Core must preserve:

* Free canvas
* Multiple desktops
* Panels
* Folders
* Widgets
* Widgets inside containers where old LLX supported it
* App shortcuts
* Custom shortcuts
* Scripts
* Gestures
* Per-item settings
* Per-desktop settings
* Item position
* Item scale
* Item rotation
* Item transparency
* Item pinning
* Item labels
* Item backgrounds
* Scrolling behavior
* Stop points, if still present in old code
* Edit mode
* Backup and restore

The Classic LLX Core must not depend on:

* EdgeWheel
* Command Palette
* GlobalSearch
* file-system folders
* tags
* work profile features
* web search
* online services
* cloud sync
* account login

## Optional Sky Modules

These features are optional modules:

```text
EdgeWheel
Command Palette
GlobalSearch
File-System Folders
Tags
Work Profile Tools
Private Space Tools
Theme Presets
Advanced Privacy Controls
Web Search Providers
```

Each optional module must have:

* A setting to enable it
* A setting to disable it
* A safe default state
* No effect on Classic LLX mode when disabled
* Clear import/export behavior
* No forced permissions
* No forced UI changes

## First-run setup

On first launch, show a simple mode picker.

Modes:

```text
Classic LLX
Modern Sky
Minimal
```

### Classic LLX mode

Classic LLX mode is for users who want old LLX behavior.

Defaults:

```text
EdgeWheel: off
Command Palette: off
GlobalSearch: basic app search only, or off if that better matches classic behavior
File-System Folders: off
Tags: off
Work Profile Tools: off unless Android exposes them normally
Private Space Tools: off
Web Search Providers: off
Internet permission: absent
```

Classic LLX mode should keep:

* Old-style desktop behavior
* Old-style editing flow
* Old-style panels
* Old-style folders
* Old-style widget flow where possible
* Old-style gestures where possible

Classic LLX mode must not push the user into a search-first workflow.

### Modern Sky mode

Modern Sky mode enables selected new features but still keeps the free canvas.

Suggested defaults:

```text
EdgeWheel: on
Command Palette: on
GlobalSearch: on for local providers
File-System Folders: on
Tags: on
Work Profile Tools: on if available
Web Search Providers: off
Internet permission: absent
```

Modern Sky mode should feel like LLX with newer tools added.

It must not become a normal grid launcher.

### Minimal mode

Minimal mode is for users who want a clean home screen first.

Suggested defaults:

```text
One desktop
Simple app drawer or basic search
No EdgeWheel
No command UI
No tags
No file-system folders
No scripts exposed unless enabled
No internet permission
```

Minimal mode still uses the LLX canvas underneath.

## Settings rule

Every optional feature must appear in settings.

Settings sections:

```text
Mode
Classic LLX Core
Canvas
Items
Folders
Panels
Widgets
Gestures
Scripts
Optional Modules
EdgeWheel
Command Palette
Search
File-System Folders
Tags
Profiles
Themes
Backup
Privacy
Debug
About
```

The `Mode` page must allow switching between:

```text
Classic LLX
Modern Sky
Minimal
Custom
```

If a user manually changes optional module settings, switch displayed mode to:

```text
Custom
```

## License rule

This project must respect source licenses.

### Code-copy candidates

These repos are MIT or treated as direct code-copy candidates after local license check:

```text
pierrehebert/LightningLauncher
Robby-Blue/SimpleFolderLauncher
markusfisch/PieLauncher
fandreuz/TUI-ConsoleLauncher
```

Before copying any code:

1. Check the repo license.
2. Check the file header.
3. Copy the license text into third-party notices.
4. Add source comments to copied files.
5. Record source repo, file path, commit hash, and license.

Copied file header format:

```text
// Source: owner/repo/path/FileName.ext
// Source commit: <commit hash>
// Original license: MIT
// Modified for Sky Launcher
```

Update:

```text
THIRD_PARTY_NOTICES.md
```

### Idea-only sources unless project license changes

Do not copy code from these unless the whole app license is changed to match:

```text
MM2-0/Kvaesitso
FossifyOrg/Launcher
NeoApplications/Neo-Launcher
```

Allowed:

* Study behavior
* Write clean-room specs
* Rebuild ideas with original Sky code
* Credit inspiration in README

Not allowed under MIT-only plan:

* Copying source files
* Copying functions
* Copying UI code
* Copying parser code
* Copying provider code
* Copying database code

### Lawnchair rule

Use Lawnchair as a study source only until file-by-file license status is checked.

Study:

```text
LawnchairLauncher/lawnchair
```

Use for ideas about:

* Modern Android launcher behavior
* Launcher3 compatibility
* Icon packs
* Themed icons
* Insets
* Gesture navigation
* Work profile behavior
* App shortcuts
* Android version handling

Do not copy Lawnchair code until license review is complete.

## Source repos and planned borrowing

### 1. Lightning Launcher

Repo:

```text
https://github.com/pierrehebert/LightningLauncher
```

Use as:

```text
BASE
```

Keep:

* Canvas engine
* Item model
* Desktop model
* Panel model
* Folder model
* App shortcut handling
* Widget host behavior
* Script behavior
* Gesture bindings
* Backup/import/export ideas
* Item transforms
* Per-desktop settings
* Per-item settings

Remove, replace, or isolate:

* Old Google hooks
* Dead billing
* Old licensing checks
* Trial/eXtreme split
* Wear modules
* Dead support libraries
* Deprecated storage paths
* Broken Play Store-specific assumptions

Output goal:

```text
Classic LLX behavior running on modern Android.
```

### 2. Pie Launcher

Repo:

```text
https://github.com/markusfisch/PieLauncher
```

Use as:

```text
OPTIONAL MODULE: EdgeWheel
```

Borrowed idea:

```text
Hold or edge-swipe to open a radial launcher.
```

Sky version:

```text
EdgeWheel
```

EdgeWheel must be optional.

Classic LLX mode default:

```text
off
```

Modern Sky mode default:

```text
on
```

EdgeWheel can launch:

* Apps
* Shortcuts
* Folders
* Panels
* Scripts
* Commands
* Desktop switch actions

Do not require EdgeWheel for any core launcher action.

### 3. SimpleFolderLauncher

Repo:

```text
https://github.com/Robby-Blue/SimpleFolderLauncher
```

Use as:

```text
OPTIONAL MODULE: File-System Folders
```

Borrowed idea:

```text
Organize launcher content like a file tree.
```

Sky version:

```text
File-System Folders
```

File-System Folders must be optional.

Classic LLX mode default:

```text
off
```

Modern Sky mode default:

```text
on
```

Folder item types:

```text
APP
SHORTCUT
WIDGET
SCRIPT
COMMAND
PANEL
DESKTOP
URL
CONTACT
FILE
```

Important rule:

```text
File-System Folders must not replace classic LLX folders.
```

Classic LLX folders stay available.

File-System Folders are an added organization layer.

### 4. T-UI ConsoleLauncher

Repo:

```text
https://github.com/fandreuz/TUI-ConsoleLauncher
```

Use as:

```text
OPTIONAL MODULE: Command Palette
```

Borrowed idea:

```text
Control the launcher through typed commands.
```

Sky version:

```text
Command Palette
```

Command Palette must be optional.

Classic LLX mode default:

```text
off
```

Modern Sky mode default:

```text
on
```

Core classes:

```text
CommandRegistry
CommandParser
CommandResult
CommandSuggestion
CommandPermission
```

Example commands:

```text
:edit
:backup
:restore
:restart
:theme classic-llx
:desktop main
:panel tools
.app signal
.folder games
.script refresh_weather
```

Do not require Command Palette for settings, editing, backup, folders, panels, or scripts.

Every command must have a normal UI path where practical.

### 5. Kvaesitso

Repo:

```text
https://github.com/MM2-0/Kvaesitso
```

Use as:

```text
IDEA ONLY: GlobalSearch
```

Borrowed idea:

```text
Search-first access to apps, actions, and local content.
```

Sky version:

```text
GlobalSearch
```

GlobalSearch must be optional or limited in Classic LLX mode.

Classic LLX mode default:

```text
off or basic app search only
```

Modern Sky mode default:

```text
on for local launcher content
```

GlobalSearch providers:

```text
AppsProvider
ShortcutsProvider
FoldersProvider
PanelsProvider
ScriptsProvider
CommandsProvider
ContactsProvider
CalendarProvider
FilesProvider
WebProvider
```

Default enabled providers:

```text
AppsProvider
ShortcutsProvider
FoldersProvider
PanelsProvider
ScriptsProvider
CommandsProvider
```

Permission-gated providers:

```text
ContactsProvider
CalendarProvider
FilesProvider
```

Disabled by default:

```text
WebProvider
```

No internet permission by default.

### 6. Fossify Launcher

Repo:

```text
https://github.com/FossifyOrg/Launcher
```

Use as:

```text
IDEA ONLY: Privacy Defaults
```

Borrowed idea:

```text
Simple, private, offline-first app behavior.
```

Sky version:

```text
Privacy-first defaults
```

Rules:

* No telemetry
* No ads
* No account login
* No cloud sync
* No internet permission by default
* Local backup
* Local restore
* Local crash logs
* Optional permissions only after user enables a feature

### 7. Neo Launcher

Repo:

```text
https://github.com/NeoApplications/Neo-Launcher
```

Use as:

```text
IDEA ONLY: Profiles and Tags
```

Borrowed idea:

```text
Better app grouping, profile handling, and hidden app control.
```

Sky version:

```text
Profiles and Tags
```

Profiles and Tags must be optional.

Classic LLX mode default:

```text
off
```

Modern Sky mode default:

```text
on if stable
```

Profiles:

* Personal
* Work
* Private Space, if Android exposes it
* Hidden apps

Tags:

* Apps can appear in multiple groups.
* Tags must not break free placement.
* Tags are metadata only.
* Tags must not replace classic folders.

### 8. Lawnchair

Repo:

```text
https://github.com/LawnchairLauncher/lawnchair
```

Use as:

```text
STUDY ONLY: Modern Android launcher behavior
```

Study only:

* Icon packs
* Themed icons
* Android 12+ behavior
* Insets
* Gesture navigation
* Work profile handling
* Shortcut behavior
* Launcher3 compatibility patterns
* Tablet behavior

Do not make Sky Launcher into Lawnchair.

Do not make Sky Launcher into Launcher3.

Sky Launcher stays LLX-based.

## Project module layout

Target modules:

```text
:app
:core-llx
:canvas
:widget-host
:gestures
:scripting
:backup
:settings
:optional-edgewheel
:optional-commands
:optional-search
:optional-filefolders
:optional-tags
:themes
:compat
:third-party
```

(Current state: inherited modules `:app :core :plugin-api` plus vendored `:lsvg`.
The target split happens gradually; `:core` plays the role of `:core-llx` for now.)

## Build modernization

### Phase 0: Audit — DONE 2026-06-11

See MIGRATION_REPORT.md, LICENSE_AUDIT.md, BUILD_ERRORS.md, THIRD_PARTY_NOTICES.md.

### Phase 1: Make the repo import — DONE 2026-06-11

Gradle 8.9 / AGP 8.7.3 / Java 17 bytecode on JDK 21 / NDK 28. Trial/eXtreme and
ABI flavors removed, billing/licensing excluded, lsvg vendored, AndroidX not
required (framework APIs only).

### Phase 2: Make the launcher install — IN PROGRESS

Debug APK builds and is deployed locally. Device install/set-as-home testing is next.

### Phase 3: Restore Classic LLX Core

Tasks:

* Load one desktop.
* Enter edit mode.
* Add app shortcut.
* Move item freely.
* Resize item.
* Rotate item.
* Add folder.
* Add panel.
* Add widget.
* Save layout.
* Restore layout after restart.
* Export backup.
* Restore backup.

Output target:

```text
Classic LLX user loop works on modern Android.
```

Do not start optional modules until this works.

### Phase 4: Script system safety wrapper

Do not rewrite scripts first.

Wrap the existing Rhino engine behind clean interfaces:

```text
ScriptEngine
ScriptRuntime
ScriptApi
ScriptPermissionGate
```

Script permission levels:

```text
layout-only
launcher-actions
app-launch
system-intent
network-disabled-by-default
```

Keep old scripts working where possible. Add script error viewer, import/export.
Block unsafe behavior by default. Do not add internet permission.

### Phase 5: Add optional modules one by one

Required order:

```text
1. EdgeWheel
2. Command Palette
3. GlobalSearch
4. File-System Folders
5. Tags and Profiles
6. Theme Presets
```

Each optional module must be:

* Disabled in Classic LLX mode
* Tested with the module off
* Tested with the module on
* Exportable if it stores config
* Safe to remove from layout if disabled
* Listed in settings

## Backup and restore

Backup format:

```text
sky-backup.zip
```

Contents:

```text
backup.json
desktops/
panels/
folders/
scripts/
icons/
wallpaper/
widgets.json
module-config/
third-party-notice-snapshot.txt
import-report.txt
```

Backup must include:

* Classic LLX layout
* Classic LLX folders
* Classic LLX panels
* Scripts
* Widgets where Android allows
* Optional module configs only if enabled
* Mode setting
* Theme setting

Restore rules:

* Never delete user data without confirmation.
* If optional module data exists but module is disabled, keep it dormant.
* If a widget cannot be restored, create a broken widget placeholder.
* If legacy LLX import partly fails, create `import-report.txt`.
* If an optional module is missing, skip its config safely.

## Privacy rules

Default build:

```text
No internet permission.
```

Always forbidden unless user chooses an optional path:

* Telemetry
* Analytics
* Ads
* Account login
* Cloud sync
* Remote config

Local-only by default:

* Backups
* Crash logs
* Scripts
* Layouts
* Search index
* Settings

Permission rules:

```text
Ask only when feature is enabled.
Explain why the permission is needed.
Allow feature to be disabled later.
Do not nag.
```

## User modes and module settings

Store mode as:

```text
classic_llx
modern_sky
minimal
custom
```

Store optional module settings as:

```json
{
  "mode": "classic_llx",
  "modules": {
    "edgeWheel": false,
    "commandPalette": false,
    "globalSearch": false,
    "fileSystemFolders": false,
    "tags": false,
    "profiles": false,
    "webProviders": false
  }
}
```

If user changes a module setting manually:

```text
mode becomes custom
```

## Android target

Current state (staged — see MIGRATION_REPORT.md):

```text
minSdk: 21
targetSdk: 28 (deliberate, classic behavior; raising it is its own phase)
compileSdk: 35
Java: 17 bytecode, builds on JDK 21
Kotlin: allowed for new modules only
```

Test on:

```text
Android 8 through Android 16 where available
```

Must eventually handle:

* Scoped storage
* Package visibility
* Work profile
* Private Space if available
* Runtime permissions
* Widget binding
* Predictive back
* Gesture navigation
* Display cutouts
* Tablets
* Foldables
* Multi-window
* Orientation changes

## Engineering rules

### Rule 1: Preserve classic LLX before adding Sky modules

### Rule 2: Do not rewrite everything

### Rule 3: New features must be optional

### Rule 4: Classic LLX mode must remain clean

### Rule 5: Copy code only when license allows

### Rule 6: No internet permission by default

### Rule 7: Keep old layouts safe

Any old LLX layout import must be safe. If import fails: keep original file
untouched, create partial import, create import report, tell user what failed,
do not crash.

## Definition of done for v0.1

v0.1 is done when:

* App builds. ✅
* App installs.
* App can be set as default launcher.
* Classic LLX mode exists.
* Optional module settings exist.
* Optional modules are off in Classic LLX mode.
* One desktop loads.
* Edit mode works.
* App shortcut can be added.
* Widget can be added.
* Folder can be added.
* Panel can be added.
* Items can be moved freely.
* Items can be resized.
* Items can be rotated.
* Layout saves.
* Layout restores after restart.
* Basic backup export works.
* About screen credits Lightning Launcher.
* No internet permission exists. ✅
* Dead wear modules are gone or isolated. ✅ (not in build)
* eXtreme/trial split is gone. ✅
* Old Google hooks are gone or stubbed. ✅ (billing/licensing excluded)

## Definition of done for v0.2

v0.2 is done when:

* Classic LLX mode still works with all optional modules disabled.
* Modern Sky mode exists.
* EdgeWheel prototype works when enabled.
* Command Palette prototype works when enabled.
* Basic GlobalSearch works when enabled.
* App search works.
* Script search works.
* Folder search works.
* Panel search works.
* EdgeWheel can launch apps.
* Command Palette can run basic commands.
* Every optional module can be disabled.
* No optional module breaks layout save/restore.
* No internet permission exists.

## Definition of done for v1.0

v1.0 is done when:

* Classic LLX mode is stable.
* Modern Sky mode is stable.
* Minimal mode is stable.
* Android 11 through current stable Android pass manual testing.
* Old LLX layouts import at least partially.
* Core LLX features are preserved.
* EdgeWheel is stable and optional.
* Command Palette is stable and optional.
* GlobalSearch is stable and optional.
* File-System Folders are stable and optional.
* Tags are stable and optional.
* Widgets survive restart where Android allows.
* Backup/restore is safe.
* App has no tracking.
* App has no ads.
* Internet is absent by default.
* All copied code has source comments.
* README credits all source projects.
* F-Droid metadata exists.
* Local debug export exists.
* Crash logs stay local unless user exports them.
