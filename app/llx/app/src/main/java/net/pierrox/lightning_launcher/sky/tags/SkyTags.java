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

import android.content.Context;

import net.pierrox.lightning_launcher.data.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

/**
 * Tags: app metadata only (optional Sky module, inspired by the grouping
 * ideas of Neo Launcher and Kvaesitso; implemented from scratch — no code
 * from GPL projects). An app can carry any number of tags; tags never affect
 * item placement, folders or desktops. Stored in sky_tags.json as
 * componentName -> [tag, ...].
 */
public class SkyTags {

    private static final String FILE_NAME = "sky_tags.json";

    private final File mFile;
    private final Map<String, List<String>> mTags = new HashMap<>();

    public SkyTags(Context context) {
        mFile = new File(context.getFilesDir(), FILE_NAME);
        load();
    }

    private void load() {
        JSONObject o = FileUtils.readJSONObjectFromFile(mFile);
        if (o == null) return;
        for (java.util.Iterator<String> it = o.keys(); it.hasNext(); ) {
            String component = it.next();
            JSONArray arr = o.optJSONArray(component);
            if (arr == null) continue;
            ArrayList<String> tags = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                String t = arr.optString(i, "").trim();
                if (!t.isEmpty()) tags.add(t);
            }
            if (!tags.isEmpty()) mTags.put(component, tags);
        }
    }

    public void save() {
        try {
            JSONObject o = new JSONObject();
            for (Map.Entry<String, List<String>> e : mTags.entrySet()) {
                o.put(e.getKey(), new JSONArray(e.getValue()));
            }
            FileUtils.saveStringToFile(o.toString(2), mFile);
        } catch (JSONException | IOException e) {
            // pass
        }
    }

    /** Tags for a component (flattened ComponentName string), never null. */
    public List<String> getTags(String component) {
        List<String> tags = mTags.get(component);
        return tags == null ? new ArrayList<String>() : new ArrayList<>(tags);
    }

    public void setTags(String component, List<String> tags) {
        ArrayList<String> clean = new ArrayList<>();
        for (String t : tags) {
            t = t.trim();
            if (!t.isEmpty() && !clean.contains(t)) clean.add(t);
        }
        if (clean.isEmpty()) mTags.remove(component);
        else mTags.put(component, clean);
        save();
    }

    public boolean hasTag(String component, String tag) {
        List<String> tags = mTags.get(component);
        if (tags == null) return false;
        for (String t : tags) {
            if (t.equalsIgnoreCase(tag)) return true;
        }
        return false;
    }

    /** All known tags, sorted, case-preserving first occurrence. */
    public List<String> allTags() {
        TreeSet<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (List<String> tags : mTags.values()) set.addAll(tags);
        return new ArrayList<>(set);
    }

    /** Components carrying a tag matching the query (prefix, case-insensitive). */
    public List<String> componentsForTagPrefix(String tagPrefix) {
        String q = tagPrefix.toLowerCase(Locale.getDefault());
        ArrayList<String> out = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : mTags.entrySet()) {
            for (String t : e.getValue()) {
                if (t.toLowerCase(Locale.getDefault()).startsWith(q)) {
                    out.add(e.getKey());
                    break;
                }
            }
        }
        return out;
    }
}
