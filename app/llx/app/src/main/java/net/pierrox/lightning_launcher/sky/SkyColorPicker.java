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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A simple, reliable color picker for Sky Launcher: a grid of modern preset
 * swatches plus a hex field, with an obvious OK / Cancel. Replaces the
 * inherited color dialog whose confirm gesture was non-obvious.
 */
public final class SkyColorPicker {

    public interface OnPicked {
        void onPicked(int color);
    }

    // a modern, mostly matte/neutral palette + a few accents
    private static final int[] PRESETS = {
            0xFF000000, 0xFF1E1E1E, 0xFF424242, 0xFF757575, 0xFFBDBDBD, 0xFFFFFFFF,
            0xFFD9442B, 0xFFE0A030, 0xFF3B8C4D, 0xFF1FA39A, 0xFF2D6CDF, 0xFF6A4CB0,
            0xFFD05A8C, 0xFF8D6E63, 0xFF263238, 0xFF0D1B2A, 0xFF1B5E20, 0xFFB00020,
    };

    private SkyColorPicker() {}

    public static void show(final Activity activity, int initialColor, final OnPicked callback) {
        final int[] selected = { initialColor | 0xFF000000 };

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * activity.getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        final View preview = new View(activity);
        LinearLayout.LayoutParams pl = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (40 * activity.getResources().getDisplayMetrics().density));
        preview.setLayoutParams(pl);
        preview.setBackgroundColor(selected[0]);
        root.addView(preview);

        final EditText hex = new EditText(activity);
        hex.setInputType(InputType.TYPE_CLASS_TEXT);
        hex.setHint("#RRGGBB");
        hex.setText(String.format("#%06X", selected[0] & 0xFFFFFF));
        root.addView(hex);

        GridLayout grid = new GridLayout(activity);
        grid.setColumnCount(6);
        int sw = (int) (40 * activity.getResources().getDisplayMetrics().density);
        int m = (int) (4 * activity.getResources().getDisplayMetrics().density);
        for (final int c : PRESETS) {
            View swatch = new View(activity);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = sw;
            lp.height = sw;
            lp.setMargins(m, m, m, m);
            swatch.setLayoutParams(lp);
            GradientDrawable g = new GradientDrawable();
            g.setColor(c);
            g.setStroke((int) (1 * activity.getResources().getDisplayMetrics().density), 0xFF888888);
            swatch.setBackground(g);
            swatch.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    selected[0] = c;
                    preview.setBackgroundColor(c);
                    hex.setText(String.format("#%06X", c & 0xFFFFFF));
                }
            });
            grid.addView(swatch);
        }
        root.addView(grid);

        hex.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    String t = s.toString().trim();
                    if (!t.startsWith("#")) t = "#" + t;
                    if (t.length() == 7) {
                        int c = Color.parseColor(t) | 0xFF000000;
                        selected[0] = c;
                        preview.setBackgroundColor(c);
                    }
                } catch (Exception e) {
                    // invalid hex in progress: ignore
                }
            }
        });

        new AlertDialog.Builder(activity)
                .setTitle("Pick a color")
                .setView(root)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int which) {
                        callback.onPicked(selected[0]);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
