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

package net.pierrox.lightning_launcher.sky.search;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import net.pierrox.lightning_launcher.configuration.GlobalConfig;
import net.pierrox.lightning_launcher.data.EmbeddedFolder;
import net.pierrox.lightning_launcher.data.Folder;
import net.pierrox.lightning_launcher.data.Item;
import net.pierrox.lightning_launcher.data.Page;
import net.pierrox.lightning_launcher.data.Shortcut;
import net.pierrox.lightning_launcher.script.Script;
import net.pierrox.lightning_launcher.sky.SkyContext;
import net.pierrox.lightning_launcher.sky.commands.CommandRegistry;
import net.pierrox.lightning_launcher.sky.commands.CommandSuggestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The bundled local search providers. */
public final class SkyProviders {

    private SkyProviders() {}

    public static List<SearchProvider> createDefault(SkyContext ctx) {
        ArrayList<SearchProvider> providers = new ArrayList<>();
        providers.add(new AppsProvider(ctx));
        providers.add(new ShortcutsProvider(ctx));
        providers.add(new FoldersProvider(ctx));
        providers.add(new PanelsProvider(ctx));
        providers.add(new ScriptsProvider(ctx));
        providers.add(new CommandsProvider(ctx));
        if (net.pierrox.lightning_launcher.sky.SkyConfig.getInstance(ctx.activity).tags) {
            providers.add(new TagsProvider(ctx));
        }
        return providers;
    }

    private static boolean matches(String label, String query) {
        return label != null
                && label.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()));
    }

    /** Iterate items sitting on dashboard desktops, defensively. */
    private static List<Item> desktopItems(SkyContext ctx) {
        ArrayList<Item> out = new ArrayList<>();
        try {
            for (int pageId : ctx.engine.getPageManager().getAllPagesIds()) {
                if (!Page.isDashboard(pageId)) continue;
                Page page = ctx.engine.getOrLoadPage(pageId);
                if (page != null && page.items != null) out.addAll(page.items);
            }
        } catch (Exception e) {
            // never let layout scanning break search
        }
        return out;
    }

    public static class AppsProvider implements SearchProvider {
        private final SkyContext mCtx;

        public AppsProvider(SkyContext ctx) { mCtx = ctx; }

        @Override public String name() { return "Apps"; }

        @Override
        public List<SearchResult> search(String query, int max) {
            ArrayList<SearchResult> out = new ArrayList<>();
            final PackageManager pm = mCtx.activity.getPackageManager();
            net.pierrox.lightning_launcher.sky.SkyConfig skyConfig =
                    net.pierrox.lightning_launcher.sky.SkyConfig.getInstance(mCtx.activity);
            net.pierrox.lightning_launcher.sky.tags.SkyTags skyTags =
                    skyConfig.tags ? new net.pierrox.lightning_launcher.sky.tags.SkyTags(mCtx.activity) : null;
            Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            for (ResolveInfo ri : pm.queryIntentActivities(main, 0)) {
                if (ri.activityInfo == null) continue;
                String label = String.valueOf(ri.loadLabel(pm));
                if (!matches(label, query)) continue;
                final ComponentName cn =
                        new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
                if (skyTags != null && skyTags.hasTag(cn.flattenToShortString(), "hidden")) continue;
                out.add(new SearchResult(label, "App", new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mCtx.activity.startActivity(new Intent(Intent.ACTION_MAIN)
                                    .addCategory(Intent.CATEGORY_LAUNCHER)
                                    .setComponent(cn)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
                        } catch (Exception e) {
                            // pass
                        }
                    }
                }));
                if (out.size() >= max) break;
            }
            return out;
        }
    }

    public static class ShortcutsProvider implements SearchProvider {
        private final SkyContext mCtx;

        public ShortcutsProvider(SkyContext ctx) { mCtx = ctx; }

        @Override public String name() { return "Shortcuts"; }

        @Override
        public List<SearchResult> search(String query, int max) {
            ArrayList<SearchResult> out = new ArrayList<>();
            for (Item item : desktopItems(mCtx)) {
                if (!(item instanceof Shortcut) || item instanceof Folder) continue;
                final Shortcut s = (Shortcut) item;
                if (!matches(s.getLabel(), query)) continue;
                out.add(new SearchResult(s.getLabel(), "Shortcut", new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Intent intent = s.getIntent();
                            if (intent != null) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mCtx.activity.startActivity(intent);
                            }
                        } catch (Exception e) {
                            // pass
                        }
                    }
                }));
                if (out.size() >= max) break;
            }
            return out;
        }
    }

    public static class FoldersProvider implements SearchProvider {
        private final SkyContext mCtx;

        public FoldersProvider(SkyContext ctx) { mCtx = ctx; }

        @Override public String name() { return "Folders"; }

        @Override
        public List<SearchResult> search(String query, int max) {
            ArrayList<SearchResult> out = new ArrayList<>();
            for (Item item : desktopItems(mCtx)) {
                if (!(item instanceof Folder) || item instanceof EmbeddedFolder) continue;
                final Folder f = (Folder) item;
                if (!matches(f.getLabel(), query)) continue;
                out.add(new SearchResult(f.getLabel(), "Folder", new Runnable() {
                    @Override
                    public void run() {
                        mCtx.runAction(GlobalConfig.OPEN_FOLDER, String.valueOf(f.getFolderPageId()));
                    }
                }));
                if (out.size() >= max) break;
            }
            return out;
        }
    }

    public static class PanelsProvider implements SearchProvider {
        private final SkyContext mCtx;

        public PanelsProvider(SkyContext ctx) { mCtx = ctx; }

        @Override public String name() { return "Panels"; }

        @Override
        public List<SearchResult> search(String query, int max) {
            ArrayList<SearchResult> out = new ArrayList<>();
            for (Item item : desktopItems(mCtx)) {
                if (!(item instanceof EmbeddedFolder)) continue;
                final EmbeddedFolder p = (EmbeddedFolder) item;
                if (!matches(p.getLabel(), query)) continue;
                out.add(new SearchResult(p.getLabel(), "Panel", new Runnable() {
                    @Override
                    public void run() {
                        mCtx.runAction(GlobalConfig.OPEN_FOLDER, String.valueOf(p.getFolderPageId()));
                    }
                }));
                if (out.size() >= max) break;
            }
            return out;
        }
    }

    public static class ScriptsProvider implements SearchProvider {
        private final SkyContext mCtx;

        public ScriptsProvider(SkyContext ctx) { mCtx = ctx; }

        @Override public String name() { return "Scripts"; }

        @Override
        public List<SearchResult> search(String query, int max) {
            ArrayList<SearchResult> out = new ArrayList<>();
            try {
                for (final Script script : mCtx.engine.getScriptManager()
                        .getAllScriptMatching(Script.FLAG_ALL)) {
                    if (!matches(script.name, query)) continue;
                    out.add(new SearchResult(script.name, "Script", new Runnable() {
                        @Override
                        public void run() {
                            mCtx.runAction(GlobalConfig.RUN_SCRIPT, String.valueOf(script.id));
                        }
                    }));
                    if (out.size() >= max) break;
                }
            } catch (Exception e) {
                // pass
            }
            return out;
        }
    }

    /** Apps by tag: query "#games" lists every app tagged games. */
    public static class TagsProvider implements SearchProvider {
        private final SkyContext mCtx;

        public TagsProvider(SkyContext ctx) { mCtx = ctx; }

        @Override public String name() { return "Tags"; }

        @Override
        public List<SearchResult> search(String query, int max) {
            ArrayList<SearchResult> out = new ArrayList<>();
            if (query.isEmpty() || query.charAt(0) != '#' || query.length() < 2) return out;
            net.pierrox.lightning_launcher.sky.tags.SkyTags tags =
                    new net.pierrox.lightning_launcher.sky.tags.SkyTags(mCtx.activity);
            final PackageManager pm = mCtx.activity.getPackageManager();
            for (String component : tags.componentsForTagPrefix(query.substring(1))) {
                final ComponentName cn = ComponentName.unflattenFromString(component);
                if (cn == null) continue;
                String label;
                try {
                    label = String.valueOf(pm.getActivityInfo(cn, 0).loadLabel(pm));
                } catch (Exception e) {
                    continue; // app gone: skip
                }
                out.add(new SearchResult(label, "Tagged app", new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mCtx.activity.startActivity(new Intent(Intent.ACTION_MAIN)
                                    .addCategory(Intent.CATEGORY_LAUNCHER)
                                    .setComponent(cn)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
                        } catch (Exception e) {
                            // pass
                        }
                    }
                }));
                if (out.size() >= max) break;
            }
            return out;
        }
    }

    /** Surfaces palette commands when the query is command-shaped (: or .). */
    public static class CommandsProvider implements SearchProvider {
        private final SkyContext mCtx;

        public CommandsProvider(SkyContext ctx) { mCtx = ctx; }

        @Override public String name() { return "Commands"; }

        @Override
        public List<SearchResult> search(String query, int max) {
            ArrayList<SearchResult> out = new ArrayList<>();
            if (query.isEmpty() || (query.charAt(0) != ':' && query.charAt(0) != '.')) {
                return out;
            }
            final CommandRegistry registry = new CommandRegistry(mCtx);
            for (CommandSuggestion cs : registry.suggest(query)) {
                final String line = cs.input.trim();
                out.add(new SearchResult(cs.title, "Command", new Runnable() {
                    @Override
                    public void run() {
                        registry.execute(line);
                    }
                }));
                if (out.size() >= max) break;
            }
            return out;
        }
    }
}
