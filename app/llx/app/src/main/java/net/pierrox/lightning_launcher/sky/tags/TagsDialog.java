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

package net.pierrox.lightning_launcher.sky.tags;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Tag manager: a filterable list of installed apps; tap an app to edit its
 * comma-separated tags. Tags are metadata only — search for "#tag" in
 * GlobalSearch to use them.
 */
public class TagsDialog {

    private static class AppRow {
        String label;
        String component;
    }

    public static void show(final Activity activity) {
        final SkyTags tags = new SkyTags(activity);
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (8 * activity.getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(activity);
        title.setText("Tags — tap an app to edit its tags. Search \"#tag\" in GlobalSearch to use them.");
        title.setTextSize(13);
        root.addView(title);

        final EditText filter = new EditText(activity);
        filter.setHint("Filter apps or tags…");
        filter.setSingleLine(true);
        root.addView(filter, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final ListView list = new ListView(activity);
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        final PackageManager pm = activity.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        final List<AppRow> allApps = new ArrayList<>();
        for (ResolveInfo ri : pm.queryIntentActivities(main, 0)) {
            if (ri.activityInfo == null) continue;
            AppRow row = new AppRow();
            row.label = String.valueOf(ri.loadLabel(pm));
            row.component = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name)
                    .flattenToShortString();
            allApps.add(row);
        }
        final Collator collator = Collator.getInstance();
        Collections.sort(allApps, new Comparator<AppRow>() {
            @Override
            public int compare(AppRow a, AppRow b) {
                return collator.compare(a.label, b.label);
            }
        });

        final List<AppRow> shown = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_list_item_2, android.R.id.text1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                AppRow row = shown.get(position);
                ((TextView) v.findViewById(android.R.id.text1)).setText(row.label);
                TextView t2 = v.findViewById(android.R.id.text2);
                List<String> appTags = tags.getTags(row.component);
                t2.setText(appTags.isEmpty() ? "no tags" : "#" + joinTags(appTags));
                t2.setTextColor(appTags.isEmpty() ? Color.GRAY : 0xFFD9442B);
                return v;
            }
        };
        list.setAdapter(adapter);

        final Runnable refresh = new Runnable() {
            @Override
            public void run() {
                String q = filter.getText().toString().trim().toLowerCase(Locale.getDefault());
                shown.clear();
                for (AppRow row : allApps) {
                    if (q.isEmpty()
                            || row.label.toLowerCase(Locale.getDefault()).contains(q)
                            || matchesTag(tags, row.component, q)) {
                        shown.add(row);
                    }
                }
                adapter.clear();
                for (AppRow r : shown) adapter.add(r.label);
                adapter.notifyDataSetChanged();
            }
        };

        filter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { refresh.run(); }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AppRow row = shown.get(position);
                final EditText input = new EditText(activity);
                input.setHint("comma-separated tags, e.g. games, kids");
                input.setText(joinTags(tags.getTags(row.component)));
                new AlertDialog.Builder(activity)
                        .setTitle(row.label)
                        .setView(input)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                ArrayList<String> newTags = new ArrayList<>();
                                for (String t : input.getText().toString().split(",")) {
                                    newTags.add(t.trim());
                                }
                                tags.setTags(row.component, newTags);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        dialog.setContentView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (activity.getResources().getDisplayMetrics().heightPixels * 0.75f));
        }
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        refresh.run();
    }

    private static boolean matchesTag(SkyTags tags, String component, String query) {
        String q = query.startsWith("#") ? query.substring(1) : query;
        if (q.isEmpty()) return false;
        for (String t : tags.getTags(component)) {
            if (t.toLowerCase(Locale.getDefault()).startsWith(q)) return true;
        }
        return false;
    }

    private static String joinTags(List<String> tags) {
        StringBuilder sb = new StringBuilder();
        for (String t : tags) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(t);
        }
        return sb.toString();
    }
}
