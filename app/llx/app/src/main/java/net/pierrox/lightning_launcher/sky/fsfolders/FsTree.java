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

import android.content.Context;

import net.pierrox.lightning_launcher.data.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * File-System Folders: a file-tree style organization layer over launcher
 * content (optional Sky module, inspired by the behavior of
 * SimpleFolderLauncher; implemented from scratch for Sky Launcher).
 *
 * This tree is pure metadata stored in sky_fsfolders.json. It never touches
 * classic LLX folders, panels or item placement.
 */
public class FsTree {

    public static final String TYPE_APP = "APP";
    public static final String TYPE_COMMAND = "COMMAND";
    public static final String TYPE_URL = "URL";

    public static class Entry {
        public String name;
        // null type = folder node with children; non-null = leaf
        public String type;
        public String data;
        public final ArrayList<Entry> children = new ArrayList<>();

        public boolean isFolder() {
            return type == null;
        }

        public static Entry folder(String name) {
            Entry e = new Entry();
            e.name = name;
            return e;
        }

        public static Entry leaf(String name, String type, String data) {
            Entry e = new Entry();
            e.name = name;
            e.type = type;
            e.data = data;
            return e;
        }
    }

    private static final String FILE_NAME = "sky_fsfolders.json";

    private final File mFile;
    public final Entry root = Entry.folder("/");

    public FsTree(Context context) {
        mFile = new File(context.getFilesDir(), FILE_NAME);
        load();
    }

    private void load() {
        JSONObject o = FileUtils.readJSONObjectFromFile(mFile);
        if (o == null) return;
        try {
            readChildren(root, o.optJSONArray("children"));
        } catch (JSONException e) {
            // corrupt tree: start empty, the file is rewritten on next save
        }
    }

    private static void readChildren(Entry parent, JSONArray children) throws JSONException {
        if (children == null) return;
        for (int i = 0; i < children.length(); i++) {
            JSONObject c = children.getJSONObject(i);
            Entry e = new Entry();
            e.name = c.optString("name", "?");
            e.type = c.has("type") ? c.getString("type") : null;
            e.data = c.optString("data", null);
            readChildren(e, c.optJSONArray("children"));
            parent.children.add(e);
        }
    }

    public void save() {
        try {
            JSONObject o = new JSONObject();
            o.put("children", writeChildren(root));
            FileUtils.saveStringToFile(o.toString(2), mFile);
        } catch (JSONException | IOException e) {
            // pass
        }
    }

    private static JSONArray writeChildren(Entry parent) throws JSONException {
        JSONArray arr = new JSONArray();
        for (Entry e : parent.children) {
            JSONObject c = new JSONObject();
            c.put("name", e.name);
            if (e.type != null) c.put("type", e.type);
            if (e.data != null) c.put("data", e.data);
            if (!e.children.isEmpty()) c.put("children", writeChildren(e));
            arr.put(c);
        }
        return arr;
    }
}
