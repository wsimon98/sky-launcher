/*
MIT License

Copyright (c) 2026 Sky Launcher contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package net.pierrox.lightning_launcher.sky;

import android.content.Context;

import net.pierrox.lightning_launcher.configuration.GlobalConfig;
import net.pierrox.lightning_launcher.configuration.PageConfig;
import net.pierrox.lightning_launcher.data.EventAction;
import net.pierrox.lightning_launcher.data.FileUtils;
import net.pierrox.lightning_launcher.data.Page;
import net.pierrox.lightning_launcher.engine.LightningEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Sky Launcher mode and optional module configuration.
 *
 * Modes: classic_llx (everything off, behaves like classic LLX), modern_sky
 * (selected modules on), minimal (everything off, one clean desktop), custom
 * (set automatically when a module is toggled by hand).
 *
 * The config is a small JSON file in the app files directory. Optional modules
 * have no effect when disabled: their actions do nothing and they are hidden
 * from the action picker.
 */
public class SkyConfig {
    public static final String MODE_CLASSIC_LLX = "classic_llx";
    public static final String MODE_MODERN_SKY = "modern_sky";
    public static final String MODE_MINIMAL = "minimal";
    public static final String MODE_CUSTOM = "custom";

    private static final String FILE_NAME = "sky_config.json";

    private static SkyConfig sInstance;

    private final File mFile;

    public String mode = MODE_CLASSIC_LLX;
    public boolean edgeWheel = false;
    public boolean commandPalette = false;
    public boolean globalSearch = false;
    // declared in the plan, not implemented yet: stored so backups round-trip
    public boolean fileSystemFolders = false;
    public boolean tags = false;
    public boolean profiles = false;
    public boolean webProviders = false;

    /** Item id of the optional on-screen app drawer button, or -1 when absent. */
    public int appDrawerButtonItemId = -1;

    /** Settings/app-drawer header color; 0 = auto (Material You or fox red). */
    public int settingsHeaderColor = 0;

    /** Built-in icon style (see SkyIcons.STYLE_*). */
    public int iconStyle = 0;

    /** EdgeWheel: chosen favorite app components (empty = alphabetical auto). */
    public final java.util.ArrayList<String> edgeWheelApps = new java.util.ArrayList<>();
    /** EdgeWheel: number of slots shown (4-12). */
    public int edgeWheelSlots = 8;

    public static synchronized SkyConfig getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SkyConfig(context.getApplicationContext());
            sInstance.load();
        }
        return sInstance;
    }

    private SkyConfig(Context context) {
        mFile = new File(context.getFilesDir(), FILE_NAME);
    }

    public boolean isFirstRun() {
        return !mFile.exists();
    }

    private void load() {
        JSONObject o = FileUtils.readJSONObjectFromFile(mFile);
        if (o == null) return;
        mode = o.optString("mode", MODE_CLASSIC_LLX);
        appDrawerButtonItemId = o.optInt("appDrawerButtonItemId", -1);
        settingsHeaderColor = o.optInt("settingsHeaderColor", 0);
        iconStyle = o.optInt("iconStyle", 0);
        edgeWheelSlots = o.optInt("edgeWheelSlots", 8);
        edgeWheelApps.clear();
        JSONArray ewa = o.optJSONArray("edgeWheelApps");
        if (ewa != null) {
            for (int i = 0; i < ewa.length(); i++) {
                String c = ewa.optString(i, "").trim();
                if (!c.isEmpty()) edgeWheelApps.add(c);
            }
        }
        JSONObject m = o.optJSONObject("modules");
        if (m != null) {
            edgeWheel = m.optBoolean("edgeWheel", false);
            commandPalette = m.optBoolean("commandPalette", false);
            globalSearch = m.optBoolean("globalSearch", false);
            fileSystemFolders = m.optBoolean("fileSystemFolders", false);
            tags = m.optBoolean("tags", false);
            profiles = m.optBoolean("profiles", false);
            webProviders = m.optBoolean("webProviders", false);
        }
    }

    public void save() {
        try {
            JSONObject m = new JSONObject();
            m.put("edgeWheel", edgeWheel);
            m.put("commandPalette", commandPalette);
            m.put("globalSearch", globalSearch);
            m.put("fileSystemFolders", fileSystemFolders);
            m.put("tags", tags);
            m.put("profiles", profiles);
            m.put("webProviders", webProviders);
            JSONObject o = new JSONObject();
            o.put("mode", mode);
            o.put("modules", m);
            o.put("appDrawerButtonItemId", appDrawerButtonItemId);
            o.put("settingsHeaderColor", settingsHeaderColor);
            o.put("iconStyle", iconStyle);
            o.put("edgeWheelSlots", edgeWheelSlots);
            o.put("edgeWheelApps", new JSONArray(edgeWheelApps));
            FileUtils.saveStringToFile(o.toString(2), mFile);
        } catch (JSONException | java.io.IOException e) {
            // pass
        }
    }

    /** Apply a mode's default module set and save. */
    public void applyMode(String newMode) {
        mode = newMode;
        boolean modern = MODE_MODERN_SKY.equals(newMode);
        edgeWheel = modern;
        commandPalette = modern;
        globalSearch = modern;
        fileSystemFolders = modern;
        // not implemented yet, always off
        tags = false;
        profiles = false;
        webProviders = false;
        save();
    }

    /** Toggle one module by hand: the mode becomes custom. */
    public void setModule(String key, boolean enabled) {
        switch (key) {
            case "edgeWheel": edgeWheel = enabled; break;
            case "commandPalette": commandPalette = enabled; break;
            case "globalSearch": globalSearch = enabled; break;
            case "fileSystemFolders": fileSystemFolders = enabled; break;
            case "tags": tags = enabled; break;
            default: return;
        }
        mode = MODE_CUSTOM;
        save();
    }

    public static boolean isSkyAction(int action) {
        return action == GlobalConfig.SKY_EDGE_WHEEL
                || action == GlobalConfig.SKY_COMMAND_PALETTE
                || action == GlobalConfig.SKY_GLOBAL_SEARCH
                || action == GlobalConfig.SKY_FS_FOLDERS;
    }

    /** Whether a sky action is currently available (its module is enabled). */
    public boolean isActionEnabled(int action) {
        switch (action) {
            case GlobalConfig.SKY_EDGE_WHEEL: return edgeWheel;
            case GlobalConfig.SKY_COMMAND_PALETTE: return commandPalette;
            case GlobalConfig.SKY_GLOBAL_SEARCH: return globalSearch;
            case GlobalConfig.SKY_FS_FOLDERS: return fileSystemFolders;
            default: return true;
        }
    }

    /**
     * Bind default gestures for enabled modules, without overwriting anything
     * the user customized: a binding is only touched if it still carries the
     * stock default (or nothing). Swipe up (app drawer), swipe down
     * (notifications) and long tap (launcher menu) are never taken over —
     * sky modules live on the two-finger swipes and double tap.
     * Classic/minimal modes restore stock defaults where a sky action is bound.
     */
    public void applyDefaultBindings(LightningEngine engine) {
        if (engine == null) return;
        GlobalConfig gc = engine.getGlobalConfig();
        boolean changed = false;

        if (MODE_MODERN_SKY.equals(mode) || MODE_CUSTOM.equals(mode)) {
            changed = bindEnabledModuleGestures(gc);
        } else if (MODE_CLASSIC_LLX.equals(mode) || MODE_MINIMAL.equals(mode)) {
            if (isSkyAction(gc.swipe2Up.action)) {
                gc.swipe2Up = EventAction.NOTHING();
                changed = true;
            }
            if (isSkyAction(gc.swipe2Down.action)) {
                gc.swipe2Down = EventAction.NOTHING();
                changed = true;
            }
            if (isSkyAction(gc.bgDoubleTap.action)) {
                gc.bgDoubleTap = new EventAction(GlobalConfig.SWITCH_FULL_SCALE_OR_ORIGIN, null);
                changed = true;
            }
        }

        if (changed) {
            engine.notifyGlobalConfigChanged();
        }
    }

    /**
     * Bind each enabled module's default gesture, only filling a slot that
     * still carries its stock default. Two-finger swipe up = EdgeWheel,
     * two-finger swipe down = Command Palette, double-tap = GlobalSearch.
     */
    private boolean bindEnabledModuleGestures(GlobalConfig gc) {
        boolean changed = false;
        if (edgeWheel && isUntouched(gc.swipe2Up, GlobalConfig.NOTHING)) {
            gc.swipe2Up = new EventAction(GlobalConfig.SKY_EDGE_WHEEL, null);
            changed = true;
        }
        if (commandPalette && isUntouched(gc.swipe2Down, GlobalConfig.NOTHING)) {
            gc.swipe2Down = new EventAction(GlobalConfig.SKY_COMMAND_PALETTE, null);
            changed = true;
        }
        if (globalSearch && isUntouched(gc.bgDoubleTap, GlobalConfig.SWITCH_FULL_SCALE_OR_ORIGIN)) {
            gc.bgDoubleTap = new EventAction(GlobalConfig.SKY_GLOBAL_SEARCH, null);
            changed = true;
        }
        return changed;
    }

    /**
     * One-time upgrade for layouts created before the modern gesture defaults:
     * if swipe up / swipe down still carry the old empty defaults, bind the
     * app drawer and the notification shade. Never touches customized bindings.
     *
     * v2 also sets the home desktop to horizontal-only scrolling when it is
     * still on AUTO: with vertical scrolling allowed, the gesture engine turns
     * a single-finger vertical swipe into a canvas scroll and the swipe events
     * never fire (classic LLX semantics). Horizontal-only is the modern phone
     * convention; it stays changeable per desktop in the launcher settings.
     */
    public void ensureModernGestureDefaults(Context context, LightningEngine engine) {
        // v3: the old default template also bound swipeUp/swipe2Up to the user
        // menu at the PAGE level, which shadows the global config entirely.
        // v4: app drawer pull-past-edge events default to closing the drawer.
        // v5: ensure enabled module gestures are bound on upgraded installs
        //     (first-run-only binding meant upgraders never got them).
        File marker = new File(context.getFilesDir(), "sky_modern_defaults_applied_v5");
        if (marker.exists() || engine == null) return;
        GlobalConfig gc = engine.getGlobalConfig();
        boolean changed = false;
        if (isUntouched(gc.swipeUp, GlobalConfig.NOTHING)
                && gc.swipeUp.action != GlobalConfig.APP_DRAWER) {
            gc.swipeUp = new EventAction(GlobalConfig.APP_DRAWER, null);
            changed = true;
        }
        if (isUntouched(gc.swipeDown, GlobalConfig.NOTHING)
                && gc.swipeDown.action != GlobalConfig.SHOW_NOTIFICATIONS) {
            gc.swipeDown = new EventAction(GlobalConfig.SHOW_NOTIFICATIONS, null);
            changed = true;
        }
        if (bindEnabledModuleGestures(gc)) {
            changed = true;
        }
        if (changed) {
            engine.notifyGlobalConfigChanged();
        }
        try {
            Page home = engine.getOrLoadPage(gc.homeScreen);
            if (home != null && home.config != null) {
                boolean pageChanged = false;
                if (home.config.scrollingDirection == PageConfig.ScrollingDirection.AUTO) {
                    home.config.scrollingDirection = PageConfig.ScrollingDirection.X;
                    pageChanged = true;
                }
                // clear the stock template's page-level swipe bindings so the
                // global ones (app drawer / Sky modules) can take effect; only
                // the old USER_MENU defaults are touched, custom bindings stay
                if (home.config.swipeUp != null
                        && home.config.swipeUp.action == GlobalConfig.USER_MENU
                        && home.config.swipeUp.next == null) {
                    home.config.swipeUp = EventAction.UNSET();
                    pageChanged = true;
                }
                if (home.config.swipe2Up != null
                        && home.config.swipe2Up.action == GlobalConfig.USER_MENU
                        && home.config.swipe2Up.next == null) {
                    home.config.swipe2Up = EventAction.UNSET();
                    pageChanged = true;
                }
                if (pageChanged) {
                    home.setModified();
                    home.saveConfig();
                    home.notifyModified();
                }
            }
            // v4: drawer pull-past-edge defaults (close the drawer), only when
            // the events are still unbound
            Page drawer = engine.getOrLoadPage(Page.APP_DRAWER_PAGE);
            if (drawer != null && drawer.config != null) {
                boolean drawerChanged = false;
                if (drawer.config.overscrollTop == null
                        || drawer.config.overscrollTop.action == GlobalConfig.UNSET) {
                    drawer.config.overscrollTop = new EventAction(GlobalConfig.BACK, null);
                    drawerChanged = true;
                }
                if (drawer.config.overscrollBottom == null
                        || drawer.config.overscrollBottom.action == GlobalConfig.UNSET) {
                    drawer.config.overscrollBottom = new EventAction(GlobalConfig.BACK, null);
                    drawerChanged = true;
                }
                if (drawerChanged) {
                    drawer.setModified();
                    drawer.saveConfig();
                    drawer.notifyModified();
                }
            }
        } catch (Exception e) {
            // never let the migration harm startup
        }
        try {
            FileUtils.saveStringToFile("1", marker);
        } catch (java.io.IOException e) {
            // pass
        }
    }

    private static boolean isUntouched(EventAction ea, int stockAction) {
        return ea == null || ea.action == GlobalConfig.UNSET || ea.action == GlobalConfig.NOTHING
                || (ea.action == stockAction && ea.next == null);
    }
}
