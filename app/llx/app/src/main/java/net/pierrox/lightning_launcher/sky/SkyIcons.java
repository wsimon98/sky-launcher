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

import net.pierrox.lightning_launcher.data.Item;
import net.pierrox.lightning_launcher.data.Page;
import net.pierrox.lightning_launcher.data.Shortcut;
import net.pierrox.lightning_launcher.engine.LightningEngine;

/**
 * Built-in icon styles for Sky Launcher. These are simple, offline icon
 * treatments — no external icon-pack APK needed. They reuse Lightning's own
 * icon color filter (an ARGB value where alpha is saturation and RGB is a
 * tint multiplier, applied via a ColorMatrix when the icon bitmap is built).
 */
public final class SkyIcons {

    public static final int STYLE_NONE = 0;
    public static final int STYLE_MONO = 1;   // black & white
    public static final int STYLE_MATTE = 2;  // muted / flat
    public static final int STYLE_DARK = 3;   // dark monochrome

    public static final String[] STYLE_LABELS = {
            "Original", "Black & White", "Matte", "Dark mono"
    };

    private SkyIcons() {}

    /** The icon color filter value (see ShortcutConfig.iconColorFilter). */
    public static int filterForStyle(int style) {
        switch (style) {
            case STYLE_MONO:  return 0x00ffffff; // alpha 0 = full desaturation
            case STYLE_MATTE: return 0x66dedede; // low saturation, slightly dimmed
            case STYLE_DARK:  return 0x00808080; // grayscale + darkened
            case STYLE_NONE:
            default:          return 0xffffffff; // unchanged
        }
    }

    /** Apply a built-in style to the app drawer and home desktop. */
    public static void applyStyle(LightningEngine engine, int style) {
        if (engine == null) return;
        int filter = filterForStyle(style);
        applyToPage(engine, Page.APP_DRAWER_PAGE, filter);
        applyToPage(engine, engine.getGlobalConfig().homeScreen, filter);
        engine.saveData();
    }

    private static void applyToPage(LightningEngine engine, int pageId, int filter) {
        try {
            Page page = engine.getOrLoadPage(pageId);
            if (page == null) return;
            page.config.defaultShortcutConfig.iconColorFilter = filter;
            if (page.items != null) {
                for (Item item : page.items) {
                    if (item instanceof Shortcut) {
                        Shortcut s = (Shortcut) item;
                        s.modifyShortcutConfig().iconColorFilter = filter;
                        s.recreateSharedAsyncGraphicsDrawable();
                    }
                }
            }
            page.reload();
        } catch (Exception e) {
            // cosmetic: never break over an icon restyle
        }
    }
}
