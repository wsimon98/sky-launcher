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

package net.pierrox.lightning_launcher.sky.fsfolders;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.pierrox.lightning_launcher.sky.SkyContext;
import net.pierrox.lightning_launcher.sky.commands.CommandRegistry;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Browser UI for File-System Folders. Navigate the tree, launch leaves,
 * create folders, add apps / commands / links, long-press to remove.
 * The tree is an extra organization layer — classic LLX folders are untouched.
 */
public class FsFoldersDialog {

    private final SkyContext mCtx;
    private final FsTree mTree;
    private final ArrayList<FsTree.Entry> mPath = new ArrayList<>();
    private Dialog mDialog;
    private TextView mBreadcrumb;
    private ListView mList;
    private final ArrayList<FsTree.Entry> mShown = new ArrayList<>();
    private ArrayAdapter<String> mAdapter;

    public static void show(SkyContext ctx) {
        new FsFoldersDialog(ctx).showInternal();
    }

    private FsFoldersDialog(SkyContext ctx) {
        mCtx = ctx;
        mTree = new FsTree(ctx.activity);
        mPath.add(mTree.root);
    }

    private FsTree.Entry current() {
        return mPath.get(mPath.size() - 1);
    }

    private void showInternal() {
        mDialog = new Dialog(mCtx.activity);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(mCtx.activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (8 * mCtx.activity.getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        mBreadcrumb = new TextView(mCtx.activity);
        mBreadcrumb.setTextSize(16);
        mBreadcrumb.setPadding(0, 0, 0, pad);
        root.addView(mBreadcrumb);

        mList = new ListView(mCtx.activity);
        root.addView(mList, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        mAdapter = new ArrayAdapter<String>(mCtx.activity,
                android.R.layout.simple_list_item_2, android.R.id.text1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                Row row = rowAt(position);
                ((TextView) v.findViewById(android.R.id.text1)).setText(row.title);
                TextView t2 = v.findViewById(android.R.id.text2);
                t2.setText(row.subtitle);
                t2.setTextColor(Color.GRAY);
                return v;
            }
        };
        mList.setAdapter(mAdapter);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onRowTap(position);
            }
        });
        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return onRowLongTap(position);
            }
        });

        LinearLayout buttons = new LinearLayout(mCtx.activity);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER_HORIZONTAL);
        buttons.addView(button("+ Folder", new Runnable() {
            @Override public void run() { promptNewFolder(); }
        }));
        buttons.addView(button("+ App", new Runnable() {
            @Override public void run() { promptAddApp(); }
        }));
        buttons.addView(button("+ Command", new Runnable() {
            @Override public void run() { promptAddText("Command (e.g. :edit or .app maps)", FsTree.TYPE_COMMAND); }
        }));
        buttons.addView(button("+ Link", new Runnable() {
            @Override public void run() { promptAddText("URL (e.g. https://…)", FsTree.TYPE_URL); }
        }));
        root.addView(buttons);

        mDialog.setContentView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Window w = mDialog.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (mCtx.activity.getResources().getDisplayMetrics().heightPixels * 0.7f));
        }
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.show();
        refresh();
    }

    private Button button(String text, final Runnable action) {
        Button b = new Button(mCtx.activity);
        b.setText(text);
        b.setTextSize(12);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { action.run(); }
        });
        return b;
    }

    // ----- rows -----

    private static class Row {
        final String title;
        final String subtitle;
        final FsTree.Entry entry; // null = ".." row

        Row(String title, String subtitle, FsTree.Entry entry) {
            this.title = title;
            this.subtitle = subtitle;
            this.entry = entry;
        }
    }

    private final ArrayList<Row> mRows = new ArrayList<>();

    private Row rowAt(int position) {
        return mRows.get(position);
    }

    private void refresh() {
        StringBuilder crumb = new StringBuilder();
        for (FsTree.Entry e : mPath) {
            if (crumb.length() > 1) crumb.append("/");
            crumb.append("/".equals(e.name) ? "" : e.name);
        }
        mBreadcrumb.setText("FS Folders:  /" + crumb);

        mRows.clear();
        if (mPath.size() > 1) {
            mRows.add(new Row("..", "Up one level", null));
        }
        ArrayList<FsTree.Entry> sorted = new ArrayList<>(current().children);
        final Collator collator = Collator.getInstance();
        Collections.sort(sorted, new Comparator<FsTree.Entry>() {
            @Override
            public int compare(FsTree.Entry a, FsTree.Entry b) {
                if (a.isFolder() != b.isFolder()) return a.isFolder() ? -1 : 1;
                return collator.compare(a.name, b.name);
            }
        });
        for (FsTree.Entry e : sorted) {
            if (e.isFolder()) {
                mRows.add(new Row(e.name, "Folder (" + e.children.size() + ")", e));
            } else {
                String kind = e.type;
                if (FsTree.TYPE_APP.equals(e.type)) kind = "App";
                else if (FsTree.TYPE_COMMAND.equals(e.type)) kind = "Command — " + e.data;
                else if (FsTree.TYPE_URL.equals(e.type)) kind = "Link — " + e.data;
                mRows.add(new Row(e.name, kind, e));
            }
        }

        mAdapter.clear();
        for (Row r : mRows) mAdapter.add(r.title);
        mAdapter.notifyDataSetChanged();
        mShown.clear();
    }

    private void onRowTap(int position) {
        Row row = rowAt(position);
        if (row.entry == null) {
            mPath.remove(mPath.size() - 1);
            refresh();
            return;
        }
        FsTree.Entry e = row.entry;
        if (e.isFolder()) {
            mPath.add(e);
            refresh();
            return;
        }
        if (FsTree.TYPE_APP.equals(e.type)) {
            try {
                ComponentName cn = ComponentName.unflattenFromString(e.data);
                mCtx.activity.startActivity(new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setComponent(cn)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
                mDialog.dismiss();
            } catch (Exception ex) {
                Toast.makeText(mCtx.activity, "App not found", Toast.LENGTH_SHORT).show();
            }
        } else if (FsTree.TYPE_COMMAND.equals(e.type)) {
            mDialog.dismiss();
            new CommandRegistry(mCtx).execute(e.data);
        } else if (FsTree.TYPE_URL.equals(e.type)) {
            try {
                mCtx.activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(e.data))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                mDialog.dismiss();
            } catch (Exception ex) {
                Toast.makeText(mCtx.activity, "No app can open this link", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean onRowLongTap(int position) {
        final Row row = rowAt(position);
        if (row.entry == null) return false;
        new AlertDialog.Builder(mCtx.activity)
                .setTitle("Remove \"" + row.entry.name + "\"?")
                .setMessage(row.entry.isFolder()
                        ? "The folder and everything inside it is removed from the tree. Apps and launcher items are not affected."
                        : "Removed from the tree only.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        current().children.remove(row.entry);
                        mTree.save();
                        refresh();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        return true;
    }

    // ----- add entries -----

    private void promptNewFolder() {
        final EditText input = new EditText(mCtx.activity);
        input.setHint("Folder name");
        new AlertDialog.Builder(mCtx.activity)
                .setTitle("New folder")
                .setView(input)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            current().children.add(FsTree.Entry.folder(name));
                            mTree.save();
                            refresh();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void promptAddApp() {
        final PackageManager pm = mCtx.activity.getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        final Collator collator = Collator.getInstance();
        Collections.sort(infos, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo a, ResolveInfo b) {
                return collator.compare(String.valueOf(a.loadLabel(pm)), String.valueOf(b.loadLabel(pm)));
            }
        });
        final String[] labels = new String[infos.size()];
        for (int i = 0; i < infos.size(); i++) labels[i] = String.valueOf(infos.get(i).loadLabel(pm));
        new AlertDialog.Builder(mCtx.activity)
                .setTitle("Add app")
                .setItems(labels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ResolveInfo ri = infos.get(which);
                        ComponentName cn = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
                        current().children.add(FsTree.Entry.leaf(labels[which],
                                FsTree.TYPE_APP, cn.flattenToShortString()));
                        mTree.save();
                        refresh();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void promptAddText(String hint, final String type) {
        final EditText name = new EditText(mCtx.activity);
        name.setHint("Display name");
        final EditText data = new EditText(mCtx.activity);
        data.setHint(hint);
        data.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout box = new LinearLayout(mCtx.activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(name);
        box.addView(data);
        new AlertDialog.Builder(mCtx.activity)
                .setTitle(FsTree.TYPE_COMMAND.equals(type) ? "Add command" : "Add link")
                .setView(box)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String n = name.getText().toString().trim();
                        String d = data.getText().toString().trim();
                        if (!d.isEmpty()) {
                            current().children.add(FsTree.Entry.leaf(n.isEmpty() ? d : n, type, d));
                            mTree.save();
                            refresh();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
