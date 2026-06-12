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
}
