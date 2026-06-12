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

package net.pierrox.lightning_launcher.sky.edgewheel;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.pierrox.lightning_launcher.sky.SkyContext;
import net.pierrox.lightning_launcher.sky.SkyModulesActivity;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * EdgeWheel: a radial quick launcher (optional Sky module, inspired by the
 * behavior of Pie Launcher; implemented from scratch for Sky Launcher).
 *
 * Prototype scope: shows up to 8 apps arranged in a circle, tap to launch,
 * tap anywhere else (or back) to dismiss. The center hub opens the Sky
 * Modules settings.
 */
public class EdgeWheelOverlay {
    private static final int SLOTS = 8;

    private final SkyContext mSkyContext;
    private final Dialog mDialog;

    public static void show(SkyContext ctx) {
        new EdgeWheelOverlay(ctx).showInternal();
    }

    private EdgeWheelOverlay(SkyContext ctx) {
        mSkyContext = ctx;
        mDialog = new Dialog(ctx.activity);
    }

    private void showInternal() {
        final FrameLayout root = new FrameLayout(mSkyContext.activity) {
            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                layoutWheel(this);
            }
        };
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

        for (WheelEntry entry : loadEntries()) {
            root.addView(makeSlotView(entry));
        }
        root.addView(makeHubView());

        Window w = mDialog.getWindow();
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            w.setBackgroundDrawable(new ColorDrawable(0xB0000000));
        }
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
    }

    private static class WheelEntry {
        CharSequence label;
        android.graphics.drawable.Drawable icon;
        ComponentName component;
    }

    private List<WheelEntry> loadEntries() {
        PackageManager pm = mSkyContext.activity.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        final Collator collator = Collator.getInstance();
        ArrayList<WheelEntry> entries = new ArrayList<>();
        String self = mSkyContext.activity.getPackageName();
        for (ResolveInfo ri : infos) {
            if (ri.activityInfo == null || self.equals(ri.activityInfo.packageName)) continue;
            WheelEntry e = new WheelEntry();
            e.label = ri.loadLabel(pm);
            e.icon = ri.loadIcon(pm);
            e.component = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
            entries.add(e);
        }
        Collections.sort(entries, new Comparator<WheelEntry>() {
            @Override
            public int compare(WheelEntry a, WheelEntry b) {
                return collator.compare(String.valueOf(a.label), String.valueOf(b.label));
            }
        });
        return entries.size() > SLOTS ? entries.subList(0, SLOTS) : entries;
    }

    private View makeSlotView(final WheelEntry entry) {
        LinearLayout slot = new LinearLayout(mSkyContext.activity);
        slot.setOrientation(LinearLayout.VERTICAL);
        slot.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView icon = new ImageView(mSkyContext.activity);
        icon.setImageDrawable(entry.icon);
        int iconSize = dp(56);
        slot.addView(icon, new LinearLayout.LayoutParams(iconSize, iconSize));

        TextView label = new TextView(mSkyContext.activity);
        label.setText(entry.label);
        label.setTextColor(Color.WHITE);
        label.setTextSize(11);
        label.setSingleLine(true);
        slot.addView(label);

        slot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                try {
                    Intent intent = new Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .setComponent(entry.component)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    mSkyContext.activity.startActivity(intent);
                } catch (Exception e) {
                    // app gone or refuses to start: nothing useful to do
                }
            }
        });
        slot.setTag("slot");
        return slot;
    }

    private View makeHubView() {
        TextView hub = new TextView(mSkyContext.activity);
        hub.setText("Sky");
        hub.setTextColor(Color.WHITE);
        hub.setTextSize(14);
        hub.setGravity(Gravity.CENTER);
        hub.setBackgroundColor(0x60FFFFFF);
        hub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                mSkyContext.activity.startActivity(
                        new Intent(mSkyContext.activity, SkyModulesActivity.class));
            }
        });
        hub.setTag("hub");
        return hub;
    }

    private void layoutWheel(FrameLayout root) {
        int w = root.getWidth(), h = root.getHeight();
        if (w == 0 || h == 0) return;
        int cx = w / 2, cy = h / 2;
        int radius = Math.min(w, h) * 35 / 100;

        int slotIndex = 0;
        int slotCount = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            if ("slot".equals(root.getChildAt(i).getTag())) slotCount++;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            int cw = child.getMeasuredWidth(), ch = child.getMeasuredHeight();
            if (cw == 0 || ch == 0) {
                child.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                cw = child.getMeasuredWidth();
                ch = child.getMeasuredHeight();
            }
            int x, y;
            if ("hub".equals(child.getTag())) {
                int hubSize = dp(64);
                x = cx - hubSize / 2;
                y = cy - hubSize / 2;
                child.layout(x, y, x + hubSize, y + hubSize);
                continue;
            }
            double angle = 2 * Math.PI * slotIndex / Math.max(1, slotCount) - Math.PI / 2;
            x = (int) (cx + radius * Math.cos(angle)) - cw / 2;
            y = (int) (cy + radius * Math.sin(angle)) - ch / 2;
            child.layout(x, y, x + cw, y + ch);
            slotIndex++;
        }
    }

    private int dp(int v) {
        return (int) (v * mSkyContext.activity.getResources().getDisplayMetrics().density);
    }
}
