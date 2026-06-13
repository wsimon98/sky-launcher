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
import android.os.Build;

/**
 * Sky accent colors. On Android 12+ the Material You dynamic accent is used;
 * elsewhere (and as fallback) the skyfox red.
 */
public final class SkyTheme {

    public static final int FOX_RED = 0xFFD9442B;

    private SkyTheme() {}

    public static int accent(Context context) {
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                return context.getColor(android.R.color.system_accent1_400);
            } catch (Exception e) {
                // fall through
            }
        }
        return FOX_RED;
    }

    /** Accent with custom alpha (0-255). */
    public static int accentWithAlpha(Context context, int alpha) {
        return (accent(context) & 0x00FFFFFF) | (alpha << 24);
    }

    /** Header color for settings screens and the app drawer bar. */
    public static int headerColor(Context context) {
        int custom = SkyConfig.getInstance(context).settingsHeaderColor;
        return custom != 0 ? custom : accent(context);
    }

    /**
     * Recolor an activity's action bar (and status bar) with the header
     * color. Replaces the hardcoded orange of the inherited theme.
     */
    public static void applyHeader(android.app.Activity activity) {
        applyHeaderColor(activity, headerColor(activity));
    }

    public static void applyHeaderColor(android.app.Activity activity, int color) {
        try {
            android.app.ActionBar ab = activity.getActionBar();
            if (ab != null) {
                ab.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(color));
            }
            activity.getWindow().setStatusBarColor(darken(color));
        } catch (Exception e) {
            // cosmetic only: never break a settings screen over a color
        }
    }

    private static int darken(int color) {
        int r = (int) (((color >> 16) & 0xFF) * 0.8f);
        int g = (int) (((color >> 8) & 0xFF) * 0.8f);
        int b = (int) ((color & 0xFF) * 0.8f);
        return (color & 0xFF000000) | (r << 16) | (g << 8) | b;
    }
}
