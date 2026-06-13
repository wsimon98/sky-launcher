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

## 2026-06-12 — v0.2.0: hotword crash fix + Sky mode system + 3 module prototypes

Fixed (reported from device testing):
- Enabling the "OK Google" option crashed/broke the launcher. Root cause: the
  legacy HotwordServiceClient binds to the Google app's HOTWORD_SERVICE, which
  modern Google app versions removed/protect (SecurityException at Dashboard
  attach => crash loop; plus an unguarded NPE on detach). The whole hook was
  removed: Dashboard code, Customize checkbox, com.google.android.hotword.*
  classes deleted. SystemConfig.hotwords field kept so old configs still parse.
  Installing the fixed APK over a broken install recovers it (layout intact).

Modernization (defaults only — everything stays rebindable in Events settings):
- swipe up    -> App drawer        (was: nothing)
- swipe down  -> Notification shade (was: nothing)
- one-time migration applies these to existing layouts only if those gestures
  were still unbound
- hardware menu-key / search-key event settings hidden unless expert mode
  (the bindings still fire if a device sends those keycodes)
- optional "App drawer button" home-screen item (Sky Modules settings)

New (all optional, all off in Classic LLX mode):
- Sky mode system: first-run picker (Classic LLX / Modern Sky / Minimal),
  sky_config.json, manual module toggle => Custom mode
- EdgeWheel prototype (radial app launcher; Modern Sky binds 2-finger swipe up)
- Command Palette prototype (:edit :backup :restore :restart :settings
  :desktop :search :sky / .app .script .folder; Modern Sky binds 2-finger
  swipe down)
- GlobalSearch prototype (local providers: apps, shortcuts, folders, panels,
  scripts, commands; Modern Sky binds double-tap)
- All three exposed as classic LLX actions (bindable to any gesture/event),
  hidden from the action picker while their module is disabled
- "Sky Modules" settings app icon in the drawer (works in every mode)

DEFERRED (unchanged): targetSdk 28 staging, LWP re-wiring, plugin ApiProvider.

## 2026-06-12 — v0.3.0: swipe-up fix + File-System Folders module

Fixed (reported from device testing):
- Swipe up on empty canvas did not open the app drawer even with the binding
  set. Root cause: ItemLayout only fires single-finger vertical swipe events
  when the desktop cannot scroll vertically (!mAllowScrollY); the default
  scrollingDirection=AUTO marks the canvas scrollable whenever the items
  bounding box pokes >0.5px past the screen, so the gesture became a canvas
  scroll. Fix: migration v2 sets the HOME desktop to horizontal-only
  scrolling when still on AUTO (modern phone convention; per-desktop setting,
  reversible in launcher settings). Marker renamed so the v2 migration also
  runs on devices that already ran v1.

New (optional, off in Classic LLX mode, on in Modern Sky):
- File-System Folders prototype (module 4 of the plan): a metadata tree
  (sky_fsfolders.json) browsed in a dialog — folders, apps, palette commands
  and links; add/remove/navigate; classic LLX folders completely untouched.
  Open via the :tree command, a bound gesture/event (new bindable action),
  or anything that can run a launcher action.

Remaining roadmap after v0.3.0: Tags & Profiles module, Theme Presets,
script permission wrapper (plan Phase 4), sky-backup.zip format incl. module
configs, targetSdk raise, LWP + plugin API re-wiring, F-Droid metadata.

## 2026-06-12 — v0.3.1: old-Android audit pass

CRITICAL FIX — the rest of the swipe-up bug:
- The stock template ALSO binds swipeUp/swipe2Up -> USER_MENU at the PAGE
  level (Setup.defaultSetup), and page bindings shadow the global config
  (Screen.runActionForItemLayout resolves page -> root page -> global).
  So even with v0.3.0's scroll fix, swipe up opened the user menu, and the
  Modern Sky two-finger EdgeWheel binding was shadowed too. Migration v3
  resets those two page bindings to UNSET when they still carry the stock
  USER_MENU value (custom bindings untouched); the template no longer sets
  them for fresh installs and creates the home desktop with horizontal-only
  scrolling directly.

Audit results (2018-era code on modern Android):
- VERIFIED OK: runtime-permission infra (ResourceWrapperActivity), backup
  storage permission prompts (targetSdk 28 keeps legacy external storage),
  overlay service (canDrawOverlays + TYPE_APPLICATION_OVERLAY), WindowService
  notification channel, display cutout shortEdges (values-v28), widget
  binding (bindAppWidgetIdIfAllowed + ACTION_APPWIDGET_BIND), package
  broadcasts (PACKAGE_ADDED/REMOVED are exempt manifest broadcasts), Gmail
  unread-count hook fully try/catch-guarded (degrades to no counts).
- FIXED: first-run dock used 2014 component lists (HTC/Sony-era dialers) —
  now falls back to resolving the device's default dialer/settings/browser/
  store via intent resolution and matching by package.
- FIXED: "rate this app" nag + settings row pointed to a Play listing that
  does not exist for app.skylauncher -> rate row hidden, dialog points to
  the GitHub project page.
- KNOWN/WATCHED: script engine dexes via dx.jar into app storage — fine at
  targetSdk 28 (W^X is a warning, not an error); must be revisited when
  targetSdk is raised. Old lightninglauncher.com wiki/community links kept
  (classic docs), may rot.

## 2026-06-12 — v0.4.0: quality-care / de-branding pass

Settings cleanup:
- "Browse existing templates" removed (searched Play for lltemplate apps;
  none exist anymore). "Load a template" from an installed APK, backup,
  restore and export all stay.
- "Lightning Launcher on Google+" row replaced by "Sky Launcher on GitHub"
  linking to the project page.
- Rate-my-app: completely gone. Row removed from settings XML, the trial-only
  nag was already dead (IS_TRIAL=false), HAS_RATE_LINK=false as backstop.

De-branding (override strings in app/sky_branding.xml; core untouched):
- "Unleash the power of LL…" -> "…of Sky Launcher…" (expert mode hint)
- Settings selector Desktop / Lightning / Android -> Desktop / Sky / Android
- "Lightning settings", "Customize Lightning", "Lightning menu", "Restart
  Lightning", "Keep Lightning running", "Lightning File Manager", "LL
  Widget" add-item entry, Tasker plugin labels (Sky Script / Sky Variable),
  permission prompt, unread-workaround dialog, first-use message, hints —
  all reworded to Sky / Sky Launcher.
- Kept on purpose: the LL-import dialog (it really is about importing from
  Lightning Launcher), template compatibility wording (reworded but credits
  LL), THIRD_PARTY/About credits, wiki/community links (classic docs).

## 2026-06-12 — v0.5.0: drawer pull-past-edge gestures, fox icon, Tags module

New gesture events (classic-LLX style, fully bindable):
- "Pull beyond top" / "Pull beyond bottom": fire when a container is already
  at its scroll limit and the finger keeps pulling significantly further
  (>1/6 screen height past the edge, vertical drags only). Implemented in
  ItemLayout.checkLimits (raw clamp overshoot) + ACTION_UP dispatch; new
  PageConfig/GlobalConfig EventAction fields overscrollTop/overscrollBottom;
  rows in page events + global events settings; template-import processing.
- App drawer defaults: both events close the drawer (BACK), set for fresh
  installs in Setup.setupAppDrawer and via migration v4 for existing ones
  (only when unbound). Fully customizable per page like any other event.

New icon: flat tribal red fox (skyfox), transparent background, rendered
from graphics/skyfox-icon.svg to all densities (mdpi..xxxhdpi).

Tags module (final borrow-idea phase, Neo Launcher idea-only, original code):
- sky_tags.json maps app components to tag lists; metadata only.
- Tag manager dialog (Sky Modules > Manage tags…, or :tags command).
- GlobalSearch understands "#tag" queries via a TagsProvider (only active
  while the Tags module is enabled).
- Profiles (work profile / private space) remain deferred: needs a managed
  device to test against; tracked for a later phase.

Borrow-source phases now complete: Pie Launcher (EdgeWheel) /
T-UI (Command Palette) / Kvaesitso idea (GlobalSearch) /
SimpleFolderLauncher (FS Folders) / Neo idea (Tags) — all as original code.

## 2026-06-12 — v0.6.0: modern-only, scrubber, quick actions, icon packs, Monet

Modern-only (per operator decision):
- First-run mode picker REMOVED. Fresh installs silently enable all Sky
  modules (Modern Sky). Individual modules can still be toggled in Sky
  Modules; the classic/minimal mode quiz is gone from settings too.

App drawer alphabet scrubber (idea credit: Niagara-style ribbons; original
code): slim A–Z ribbon on the right edge, visible in the by-name drawer
layout; tap/drag to jump to the first app for the letter (uses the classic
ensureCellVisible cell scrolling).

Item long-press quick actions: LL already had App info / Play page / Kill /
Uninstall in the item menu; added "Tags…" (when the Tags module is on) to
edit an app's tags in place.

Icon packs: classic ADW-format icon pack support already existed in
Customize; added an "Apply icon pack…" one-tap flow in Sky Modules that
applies a pack to both the app drawer and the home desktop.

Shelf: the classic floating overlay desktop IS the shelf — new :shelf
command shows it (and explains how to designate an overlay desktop when
none is set). SHOW/HIDE_FLOATING_DESKTOP remain bindable to any gesture.

Material You: Sky accents (EdgeWheel hub, tag highlights, FS Folders
breadcrumb, scrubber active letter) use the system dynamic color on
Android 12+, falling back to the skyfox red elsewhere.

KWGT audit (desk check): LL's widget host uses standard AppWidgetHost
binding + the widget resize/configure flows; KWGT should behave like any
host. Needs an on-device check with a Kustom widget before calling it done.

Scripts-from-GitHub idea: ON HOLD until the Rhino script engine is verified
on-device (create + run a small script in the Script Editor). If old LLX
scripts run, a GitHub import path (TrianguloY's MIT script repository) is
worth building.

## 2026-06-12 — v0.6.1: customizable settings header color

The settings screens and the app drawer bar carried Lightning's hardcoded
orange (#FF5722, theme colorPrimary + ab_bg style). Now:
- New "Settings header color…" picker in Sky Modules (classic LL color
  picker dialog, live preview), plus "back to auto". Stored in
  sky_config.json (settingsHeaderColor, 0 = auto).
- Auto = Material You system accent on Android 12+, fox red elsewhere.
- Applied at runtime (SkyTheme.applyHeader) to the action bar + status bar
  of RootSettings, Customize, BackupRestore, ScreenManager,
  EventActionSetup, Shortcuts, Sky Modules, and to the app drawer's custom
  action bar (R.id.ab). The orange is gone by default.

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
