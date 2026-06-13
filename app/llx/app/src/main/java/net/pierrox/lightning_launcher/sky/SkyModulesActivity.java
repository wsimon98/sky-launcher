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
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.pierrox.lightning_launcher.LLApp;
import net.pierrox.lightning_launcher.activities.AppDrawerX;
import net.pierrox.lightning_launcher.data.Item;
import net.pierrox.lightning_launcher.data.Page;
import net.pierrox.lightning_launcher.data.Shortcut;
import net.pierrox.lightning_launcher.data.Utils;
import net.pierrox.lightning_launcher.engine.LightningEngine;

import net.pierrox.lightning_launcher_extreme.R;

/**
 * Sky Launcher mode and module settings. Plain programmatic UI on purpose:
 * it must never depend on any optional module.
 */
public class SkyModulesActivity extends Activity {

    private SkyConfig mConfig;
    private CheckBox mCheckEdgeWheel, mCheckPalette, mCheckSearch, mCheckFsFolders, mCheckTags, mCheckDrawerButton;
    private boolean mUpdating;
    private static final int REQUEST_PICK_ICON_PACK = 71;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Sky Modules");
        mConfig = SkyConfig.getInstance(this);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);

        root.addView(header("Modules"));

        mCheckEdgeWheel = check(root, "EdgeWheel",
                "Radial quick launcher (two-finger swipe up when bound)", "edgeWheel");
        mCheckPalette = check(root, "Command Palette",
                "Typed commands like :edit or .app (two-finger swipe down when bound)", "commandPalette");
        mCheckSearch = check(root, "GlobalSearch",
                "Local search across apps, items and scripts (double tap when bound)", "globalSearch");
        mCheckFsFolders = check(root, "File-System Folders",
                "Organize apps, commands and links in a file tree (open via the :tree command or a bound gesture). Classic folders are untouched", "fileSystemFolders");
        mCheckTags = check(root, "Tags",
                "Tag apps with keywords; search \"#tag\" in GlobalSearch. Metadata only, never moves items", "tags");
        android.widget.Button manageTags = new android.widget.Button(this);
        manageTags.setText("Manage tags…");
        manageTags.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                if (mConfig.tags) {
                    net.pierrox.lightning_launcher.sky.tags.TagsDialog.show(SkyModulesActivity.this);
                } else {
                    Toast.makeText(SkyModulesActivity.this, "Enable Tags first", Toast.LENGTH_SHORT).show();
                }
            }
        });
        root.addView(manageTags);

        root.addView(header("Home screen"));

        mCheckDrawerButton = new CheckBox(this);
        mCheckDrawerButton.setText("App drawer button");
        root.addView(mCheckDrawerButton);
        root.addView(note("Adds an \"All apps\" button to the home desktop. "
                + "Swiping up always opens the app drawer too."));
        mCheckDrawerButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mUpdating) return;
                setAppDrawerButton(isChecked);
            }
        });

        root.addView(header("Look"));

        android.widget.Button iconPack = new android.widget.Button(this);
        iconPack.setText("Apply icon pack…");
        iconPack.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Intent i = new Intent(Intent.ACTION_PICK_ACTIVITY);
                i.putExtra(Intent.EXTRA_TITLE, "Icon packs");
                i.putExtra(Intent.EXTRA_INTENT, new Intent("org.adw.launcher.icons.ACTION_PICK_ICON"));
                try {
                    startActivityForResult(i, REQUEST_PICK_ICON_PACK);
                } catch (Exception e) {
                    Toast.makeText(SkyModulesActivity.this, "No icon pack apps installed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        root.addView(iconPack);
        root.addView(note("Applies an installed ADW-format icon pack to the app drawer and home desktop."));

        android.widget.Button headerColor = new android.widget.Button(this);
        headerColor.setText("Settings header color…");
        headerColor.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                net.margaritov.preference.colorpicker.ColorPickerDialog picker =
                        new net.margaritov.preference.colorpicker.ColorPickerDialog(
                                SkyModulesActivity.this, SkyTheme.headerColor(SkyModulesActivity.this));
                picker.setOnColorChangedListener(
                        new net.margaritov.preference.colorpicker.ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int color) {
                        // live preview while picking
                        SkyTheme.applyHeaderColor(SkyModulesActivity.this, color | 0xFF000000);
                    }

                    @Override
                    public void onColorDialogSelected(int color) {
                        mConfig.settingsHeaderColor = color | 0xFF000000;
                        mConfig.save();
                        SkyTheme.applyHeader(SkyModulesActivity.this);
                    }

                    @Override
                    public void onColorDialogCanceled() {
                        // restore the previous color
                        SkyTheme.applyHeader(SkyModulesActivity.this);
                    }
                });
                picker.show();
            }
        });
        root.addView(headerColor);

        android.widget.Button headerAuto = new android.widget.Button(this);
        headerAuto.setText("Header color: back to auto");
        headerAuto.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                mConfig.settingsHeaderColor = 0;
                mConfig.save();
                SkyTheme.applyHeader(SkyModulesActivity.this);
                Toast.makeText(SkyModulesActivity.this,
                        "Following the system accent color again", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(headerAuto);
        root.addView(note("Colors the top bar of the settings screens and the app drawer. "
                + "Auto follows the system accent (Android 12+)."));

        root.addView(note("\nGestures can be rebound under launcher settings > Events. "
                + "Sky Launcher has no internet access: everything stays on this device."));

        setContentView(scroll, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        refresh();
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
            net.pierrox.lightning_launcher.data.IconPack.applyIconPackAsync(this, packageName,
                    drawer, net.pierrox.lightning_launcher.data.Item.NO_ID,
                    new net.pierrox.lightning_launcher.data.IconPack.IconPackListener() {
                @Override
                public void onPackApplied(boolean success) {
                    if (!success) {
                        Toast.makeText(SkyModulesActivity.this,
                                "This app does not look like an icon pack", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    home.config.iconPack = packageName;
                    net.pierrox.lightning_launcher.data.IconPack.applyIconPackAsync(
                            SkyModulesActivity.this, packageName, home,
                            net.pierrox.lightning_launcher.data.Item.NO_ID,
                            new net.pierrox.lightning_launcher.data.IconPack.IconPackListener() {
                        @Override
                        public void onPackApplied(boolean success2) {
                            engine.saveData();
                            Toast.makeText(SkyModulesActivity.this, "Icon pack applied", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Could not apply the icon pack", Toast.LENGTH_SHORT).show();
        }
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
        tv.setPadding(0, (int) (12 * getResources().getDisplayMetrics().density), 0, 4);
        return tv;
    }

    private TextView note(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        return tv;
    }

    private RadioButton radio(RadioGroup group, String title, String description) {
        RadioButton rb = new RadioButton(this);
        rb.setText(title + "\n" + description);
        group.addView(rb);
        return rb;
    }

    private CheckBox check(LinearLayout root, String title, String description, final String key) {
        CheckBox cb = new CheckBox(this);
        cb.setText(title);
        root.addView(cb);
        root.addView(note(description));
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mUpdating) return;
                mConfig.setModule(key, isChecked);
                mConfig.applyDefaultBindings(getEngine());
                refresh();
            }
        });
        return cb;
    }

    private void refresh() {
        mUpdating = true;
        mCheckEdgeWheel.setChecked(mConfig.edgeWheel);
        mCheckPalette.setChecked(mConfig.commandPalette);
        mCheckSearch.setChecked(mConfig.globalSearch);
        mCheckFsFolders.setChecked(mConfig.fileSystemFolders);
        mCheckTags.setChecked(mConfig.tags);
        mCheckDrawerButton.setChecked(mConfig.appDrawerButtonItemId != -1);
        mUpdating = false;
    }

    private void setAppDrawerButton(boolean enabled) {
        LightningEngine engine = getEngine();
        if (engine == null) {
            Toast.makeText(this, "Launcher not ready", Toast.LENGTH_SHORT).show();
            refresh();
            return;
        }
        try {
            Page home = engine.getOrLoadPage(engine.getGlobalConfig().homeScreen);
            if (enabled && mConfig.appDrawerButtonItemId == -1) {
                Intent intent = new Intent(Intent.ACTION_MAIN)
                        .setComponent(new ComponentName(this, AppDrawerX.class));
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.all_apps);
                float x = getResources().getDisplayMetrics().widthPixels / 2f;
                float y = getResources().getDisplayMetrics().heightPixels * 0.85f;
                Shortcut button = Utils.addShortcut(getString(R.string.all_apps),
                        icon, intent, home, x, y, 1f, true);
                mConfig.appDrawerButtonItemId = button.getId();
            } else if (!enabled && mConfig.appDrawerButtonItemId != -1) {
                Item item = home.findItemById(mConfig.appDrawerButtonItemId);
                if (item != null) {
                    home.removeItem(item, false);
                }
                mConfig.appDrawerButtonItemId = -1;
            }
            mConfig.save();
            engine.saveData();
        } catch (Exception e) {
            Toast.makeText(this, "Could not update the home desktop", Toast.LENGTH_SHORT).show();
        }
        refresh();
    }
}
