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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.margaritov.preference.colorpicker.ColorPickerDialog;
import net.pierrox.lightning_launcher.LLApp;
import net.pierrox.lightning_launcher.data.IconPack;
import net.pierrox.lightning_launcher.data.Item;
import net.pierrox.lightning_launcher.data.Page;
import net.pierrox.lightning_launcher.engine.LightningEngine;

/**
 * Colors &amp; Wallpaper: one place for everything that changes how Sky
 * Launcher looks — wallpaper, the settings/drawer header color, the built-in
 * icon styles, and external icon packs. Plain programmatic UI, no module
 * dependency.
 */
public class SkyColorsActivity extends Activity {

    private static final int REQUEST_PICK_ICON_PACK = 81;

    private SkyConfig mConfig;
    private TextView mIconStyleValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SkyTheme.applyHeader(this);
        setTitle("Colors & Wallpaper");
        mConfig = SkyConfig.getInstance(this);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);

        // ---- Wallpaper ----
        root.addView(header("Wallpaper"));
        root.addView(button("Choose wallpaper…", new Runnable() {
            @Override public void run() {
                try {
                    startActivity(Intent.createChooser(
                            new Intent(Intent.ACTION_SET_WALLPAPER), "Set wallpaper"));
                } catch (Exception e) {
                    Toast.makeText(SkyColorsActivity.this, "No wallpaper picker found", Toast.LENGTH_SHORT).show();
                }
            }
        }));

        // ---- Header / bar color ----
        root.addView(header("Header & bar color"));
        root.addView(note("Colors the top bar of the settings screens and the app drawer."));
        root.addView(button("Pick header color…", new Runnable() {
            @Override public void run() { pickHeaderColor(); }
        }));
        root.addView(button("Use system accent (auto)", new Runnable() {
            @Override public void run() {
                mConfig.settingsHeaderColor = 0;
                mConfig.save();
                SkyTheme.applyHeader(SkyColorsActivity.this);
                Toast.makeText(SkyColorsActivity.this,
                        "Following the system accent again", Toast.LENGTH_SHORT).show();
            }
        }));

        // ---- System bars ----
        root.addView(header("Status & navigation bars"));
        root.addView(note("Colors the system bars on the home desktop and app drawer."));
        root.addView(button("Status bar color…", new Runnable() {
            @Override public void run() { pickBarColor(true); }
        }));
        root.addView(button("Navigation bar color…", new Runnable() {
            @Override public void run() { pickBarColor(false); }
        }));

        // ---- Icon style ----
        root.addView(header("Icon style"));
        root.addView(note("Built-in, offline icon looks. No icon-pack app needed."));
        Button iconStyle = button("", new Runnable() {
            @Override public void run() { pickIconStyle(); }
        });
        mIconStyleValue = (TextView) iconStyle;
        updateIconStyleLabel();
        root.addView(iconStyle);

        // ---- External icon pack ----
        root.addView(header("Icon pack"));
        root.addView(button("Apply icon pack…", new Runnable() {
            @Override public void run() {
                Intent i = new Intent(Intent.ACTION_PICK_ACTIVITY);
                i.putExtra(Intent.EXTRA_TITLE, "Icon packs");
                i.putExtra(Intent.EXTRA_INTENT, new Intent("org.adw.launcher.icons.ACTION_PICK_ICON"));
                try {
                    startActivityForResult(i, REQUEST_PICK_ICON_PACK);
                } catch (Exception e) {
                    Toast.makeText(SkyColorsActivity.this, "No icon pack apps installed", Toast.LENGTH_SHORT).show();
                }
            }
        }));
        root.addView(note("Applies an installed ADW-format icon pack to the app drawer and home desktop."));

        setContentView(scroll, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private LightningEngine getEngine() {
        try {
            return LLApp.get().getAppEngine();
        } catch (Exception e) {
            return null;
        }
    }

    private TextView header(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(18);
        tv.setTextColor(SkyTheme.accent(this));
        tv.setPadding(0, (int) (14 * getResources().getDisplayMetrics().density), 0, 4);
        return tv;
    }

    private TextView note(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        return tv;
    }

    private Button button(String text, final Runnable action) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return b;
    }

    private void pickHeaderColor() {
        ColorPickerDialog picker = new ColorPickerDialog(this, SkyTheme.headerColor(this));
        picker.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
            @Override public void onColorChanged(int color) {
                SkyTheme.applyHeaderColor(SkyColorsActivity.this, color | 0xFF000000);
            }
            @Override public void onColorDialogSelected(int color) {
                mConfig.settingsHeaderColor = color | 0xFF000000;
                mConfig.save();
                SkyTheme.applyHeader(SkyColorsActivity.this);
            }
            @Override public void onColorDialogCanceled() {
                SkyTheme.applyHeader(SkyColorsActivity.this);
            }
        });
        picker.show();
    }

    private void pickBarColor(final boolean statusBar) {
        final LightningEngine engine = getEngine();
        if (engine == null) {
            Toast.makeText(this, "Launcher not ready", Toast.LENGTH_SHORT).show();
            return;
        }
        int current;
        try {
            Page home = engine.getOrLoadPage(engine.getGlobalConfig().homeScreen);
            current = statusBar ? home.config.statusBarColor : home.config.navigationBarColor;
        } catch (Exception e) {
            current = 0xFF000000;
        }
        ColorPickerDialog picker = new ColorPickerDialog(this, current);
        picker.setAlphaSliderVisible(true);
        picker.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
            @Override public void onColorChanged(int color) {}
            @Override public void onColorDialogSelected(int color) {
                applyBarColor(engine, statusBar, color);
            }
            @Override public void onColorDialogCanceled() {}
        });
        picker.show();
    }

    private void applyBarColor(LightningEngine engine, boolean statusBar, int color) {
        try {
            int[] pages = { engine.getGlobalConfig().homeScreen, Page.APP_DRAWER_PAGE };
            for (int pid : pages) {
                Page p = engine.getOrLoadPage(pid);
                if (p == null) continue;
                if (statusBar) p.config.statusBarColor = color;
                else p.config.navigationBarColor = color;
                p.setModified();
                p.saveConfig();
                p.notifyModified();
            }
            engine.saveData();
            Toast.makeText(this, (statusBar ? "Status" : "Navigation") + " bar color set",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not set the bar color", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateIconStyleLabel() {
        int s = mConfig.iconStyle;
        String name = (s >= 0 && s < SkyIcons.STYLE_LABELS.length) ? SkyIcons.STYLE_LABELS[s] : "Original";
        mIconStyleValue.setText("Icon style:  " + name);
    }

    private void pickIconStyle() {
        new AlertDialog.Builder(this)
                .setTitle("Icon style")
                .setItems(SkyIcons.STYLE_LABELS, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        mConfig.iconStyle = which;
                        mConfig.save();
                        updateIconStyleLabel();
                        Toast.makeText(SkyColorsActivity.this, "Applying…", Toast.LENGTH_SHORT).show();
                        SkyIcons.applyStyle(getEngine(), which);
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_ICON_PACK) {
            if (resultCode == RESULT_OK && data != null && data.getComponent() != null) {
                applyIconPack(data.getComponent().getPackageName());
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void applyIconPack(final String packageName) {
        final LightningEngine engine = getEngine();
        if (engine == null) {
            Toast.makeText(this, "Launcher not ready", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Applying icon pack…", Toast.LENGTH_SHORT).show();
        try {
            final Page drawer = engine.getOrLoadPage(Page.APP_DRAWER_PAGE);
            final Page home = engine.getOrLoadPage(engine.getGlobalConfig().homeScreen);
            drawer.config.iconPack = packageName;
            IconPack.applyIconPackAsync(this, packageName, drawer, Item.NO_ID,
                    new IconPack.IconPackListener() {
                @Override public void onPackApplied(boolean success) {
                    if (!success) {
                        Toast.makeText(SkyColorsActivity.this,
                                "This app does not look like an icon pack", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    home.config.iconPack = packageName;
                    IconPack.applyIconPackAsync(SkyColorsActivity.this, packageName, home, Item.NO_ID,
                            new IconPack.IconPackListener() {
                        @Override public void onPackApplied(boolean success2) {
                            engine.saveData();
                            Toast.makeText(SkyColorsActivity.this, "Icon pack applied", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Could not apply the icon pack", Toast.LENGTH_SHORT).show();
        }
    }
}
