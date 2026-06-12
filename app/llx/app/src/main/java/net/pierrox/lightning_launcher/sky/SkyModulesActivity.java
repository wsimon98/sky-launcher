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
    private RadioButton mRadioClassic, mRadioModern, mRadioMinimal, mRadioCustom;
    private CheckBox mCheckEdgeWheel, mCheckPalette, mCheckSearch, mCheckFsFolders, mCheckTags, mCheckDrawerButton;
    private boolean mUpdating;

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

        root.addView(header("Mode"));

        RadioGroup modes = new RadioGroup(this);
        mRadioClassic = radio(modes, "Classic LLX",
                "Pure Lightning Launcher behavior, all new modules off");
        mRadioModern = radio(modes, "Modern Sky",
                "Classic canvas plus EdgeWheel, Command Palette and GlobalSearch");
        mRadioMinimal = radio(modes, "Minimal",
                "A clean simple home screen, all new modules off");
        mRadioCustom = radio(modes, "Custom",
                "Set automatically when modules are toggled by hand");
        mRadioCustom.setEnabled(false);
        root.addView(modes);

        modes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mUpdating) return;
                String newMode;
                if (checkedId == mRadioClassic.getId()) newMode = SkyConfig.MODE_CLASSIC_LLX;
                else if (checkedId == mRadioModern.getId()) newMode = SkyConfig.MODE_MODERN_SKY;
                else if (checkedId == mRadioMinimal.getId()) newMode = SkyConfig.MODE_MINIMAL;
                else return;
                mConfig.applyMode(newMode);
                mConfig.applyDefaultBindings(getEngine());
                refresh();
            }
        });

        root.addView(header("Optional modules"));

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

        root.addView(note("\nAll modules are off in Classic LLX mode. Gestures can be "
                + "rebound under launcher settings > Events. Sky Launcher has no "
                + "internet access: everything stays on this device."));

        setContentView(scroll, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        refresh();
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
        switch (mConfig.mode) {
            case SkyConfig.MODE_CLASSIC_LLX: mRadioClassic.setChecked(true); break;
            case SkyConfig.MODE_MODERN_SKY: mRadioModern.setChecked(true); break;
            case SkyConfig.MODE_MINIMAL: mRadioMinimal.setChecked(true); break;
            default: mRadioCustom.setEnabled(true); mRadioCustom.setChecked(true); break;
        }
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
