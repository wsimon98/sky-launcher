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

package net.pierrox.lightning_launcher.sky.commands;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import net.pierrox.lightning_launcher.LLApp;
import net.pierrox.lightning_launcher.activities.BackupRestore;
import net.pierrox.lightning_launcher.configuration.GlobalConfig;
import net.pierrox.lightning_launcher.data.Folder;
import net.pierrox.lightning_launcher.data.Item;
import net.pierrox.lightning_launcher.data.Page;
import net.pierrox.lightning_launcher.script.Script;
import net.pierrox.lightning_launcher.script.ScriptManager;
import net.pierrox.lightning_launcher.sky.SkyContext;
import net.pierrox.lightning_launcher.sky.SkyModulesActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Built-in palette commands. Every command here is a shortcut to something the
 * normal UI can also do — the palette is never required for any core action.
 */
public class CommandRegistry {

    private static class BuiltIn {
        final String token;
        final String description;
        final boolean takesArgument;

        BuiltIn(String token, String description, boolean takesArgument) {
            this.token = token;
            this.description = description;
            this.takesArgument = takesArgument;
        }
    }

    private static final BuiltIn[] BUILT_INS = {
            new BuiltIn(":edit", "Enter edit mode", false),
            new BuiltIn(":backup", "Open backup & restore", false),
            new BuiltIn(":restore", "Open backup & restore", false),
            new BuiltIn(":restart", "Restart the launcher", false),
            new BuiltIn(":settings", "Open launcher settings", false),
            new BuiltIn(":colors", "Open Colors & Wallpaper", false),
            new BuiltIn(":wallpaper", "Choose a wallpaper", false),
            new BuiltIn(":desktop", "Choose a desktop to go to", false),
            // EdgeWheel retired — :wheel no longer offered (dormant).
            new BuiltIn(":search", "Open GlobalSearch (when enabled)", false),
            new BuiltIn(":tree", "Open File-System Folders (when enabled)", false),
            new BuiltIn(":tags", "Manage app tags (when enabled)", false),
            new BuiltIn(":shelf", "Show the floating desktop (set a desktop as overlay first)", false),
            new BuiltIn(":sky", "Open Sky modules settings", false),
            new BuiltIn(".app", "Launch an app by name", true),
            new BuiltIn(".script", "Run a script by name", true),
            new BuiltIn(".folder", "Open a folder by name", true),
    };

    private final SkyContext mCtx;

    public CommandRegistry(SkyContext ctx) {
        mCtx = ctx;
    }

    public List<CommandSuggestion> suggest(String input) {
        ArrayList<CommandSuggestion> out = new ArrayList<>();
        String s = input == null ? "" : input.trim();

        if (s.isEmpty()) {
            for (BuiltIn b : BUILT_INS) {
                out.add(new CommandSuggestion(b.token + (b.takesArgument ? " " : ""), b.token, b.description));
            }
            return out;
        }

        CommandParser p = CommandParser.parse(s);
        if (p == null) {
            // not command-shaped: offer .app matching as a convenience
            for (CommandSuggestion cs : suggestApps(s)) out.add(cs);
            return out;
        }

        // exact command with argument: suggest argument completions
        if (p.token.equals(".app")) return suggestApps(p.argument);
        if (p.token.equals(".script")) return suggestScripts(p.argument);
        if (p.token.equals(".folder")) return suggestFolders(p.argument);

        // otherwise: prefix-match the command list
        String lower = s.toLowerCase(Locale.getDefault());
        for (BuiltIn b : BUILT_INS) {
            if (b.token.startsWith(lower)) {
                out.add(new CommandSuggestion(b.token + (b.takesArgument ? " " : ""), b.token, b.description));
            }
        }
        return out;
    }

    public CommandResult execute(String input) {
        CommandParser p = CommandParser.parse(input);
        if (p == null) {
            return CommandResult.error("Commands start with : or .");
        }
        switch (p.token) {
            case ":edit":
                mCtx.runAction(GlobalConfig.EDIT_LAYOUT, null);
                return CommandResult.ok();
            case ":backup":
            case ":restore":
                mCtx.activity.startActivity(new Intent(mCtx.activity, BackupRestore.class));
                return CommandResult.ok();
            case ":restart":
                LLApp.get().restart(true);
                return CommandResult.ok();
            case ":settings":
                mCtx.runAction(GlobalConfig.CUSTOMIZE_LAUNCHER, null);
                return CommandResult.ok();
            case ":colors":
                mCtx.activity.startActivity(new Intent(mCtx.activity,
                        net.pierrox.lightning_launcher.sky.SkyColorsActivity.class));
                return CommandResult.ok();
            case ":wallpaper":
                try {
                    mCtx.activity.startActivity(Intent.createChooser(
                            new Intent(Intent.ACTION_SET_WALLPAPER), "Set wallpaper"));
                } catch (Exception e) {
                    return CommandResult.error("No wallpaper picker found");
                }
                return CommandResult.ok();
            case ":desktop":
                mCtx.runAction(GlobalConfig.SELECT_DESKTOP_TO_GO_TO, null);
                return CommandResult.ok();
            // EdgeWheel retired — :wheel handler kept dormant:
            // case ":wheel":
            //     if (net.pierrox.lightning_launcher.sky.SkyConfig.getInstance(mCtx.activity).edgeWheel) {
            //         mCtx.runAction(GlobalConfig.SKY_EDGE_WHEEL, null);
            //         return CommandResult.ok();
            //     }
            //     return CommandResult.error("Enable EdgeWheel in Sky Modules first");
            case ":search":
                mCtx.runAction(GlobalConfig.SKY_GLOBAL_SEARCH, null);
                return CommandResult.ok();
            case ":tree":
                mCtx.runAction(GlobalConfig.SKY_FS_FOLDERS, null);
                return CommandResult.ok();
            case ":tags":
                if (net.pierrox.lightning_launcher.sky.SkyConfig.getInstance(mCtx.activity).tags) {
                    net.pierrox.lightning_launcher.sky.tags.TagsDialog.show(mCtx.activity);
                    return CommandResult.ok();
                }
                return CommandResult.error("Enable Tags in Sky Modules first");
            case ":shelf":
                // the shelf is classic LLX's floating overlay desktop
                if (mCtx.engine.getGlobalConfig().overlayScreen == Page.NONE) {
                    return CommandResult.error("Pick a desktop and enable it as overlay in its settings first");
                }
                mCtx.runAction(GlobalConfig.SHOW_FLOATING_DESKTOP, null);
                return CommandResult.ok();
            case ":sky":
                mCtx.activity.startActivity(new Intent(mCtx.activity, SkyModulesActivity.class));
                return CommandResult.ok();
            case ".app":
                return launchApp(p.argument);
            case ".script":
                return runScript(p.argument);
            case ".folder":
                return openFolder(p.argument);
            default:
                return CommandResult.error("Unknown command " + p.token);
        }
    }

    // ----- .app -----

    private List<ResolveInfo> queryApps() {
        Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        return mCtx.activity.getPackageManager().queryIntentActivities(main, 0);
    }

    private List<CommandSuggestion> suggestApps(String query) {
        ArrayList<CommandSuggestion> out = new ArrayList<>();
        if (query == null) query = "";
        String q = query.toLowerCase(Locale.getDefault());
        PackageManager pm = mCtx.activity.getPackageManager();
        for (ResolveInfo ri : queryApps()) {
            String label = String.valueOf(ri.loadLabel(pm));
            if (label.toLowerCase(Locale.getDefault()).contains(q)) {
                out.add(new CommandSuggestion(".app " + label, label, "Launch app"));
                if (out.size() >= 12) break;
            }
        }
        return out;
    }

    private CommandResult launchApp(String query) {
        if (query.isEmpty()) return CommandResult.error("Usage: .app <name>");
        String q = query.toLowerCase(Locale.getDefault());
        PackageManager pm = mCtx.activity.getPackageManager();
        ResolveInfo best = null;
        for (ResolveInfo ri : queryApps()) {
            String label = String.valueOf(ri.loadLabel(pm)).toLowerCase(Locale.getDefault());
            if (label.equals(q)) { best = ri; break; }
            if (best == null && label.contains(q)) best = ri;
        }
        if (best == null) return CommandResult.error("No app matching \"" + query + "\"");
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setComponent(new ComponentName(best.activityInfo.packageName, best.activityInfo.name))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            mCtx.activity.startActivity(intent);
            return CommandResult.ok();
        } catch (Exception e) {
            return CommandResult.error("Could not launch app");
        }
    }

    // ----- .script -----

    private List<CommandSuggestion> suggestScripts(String query) {
        ArrayList<CommandSuggestion> out = new ArrayList<>();
        String q = query.toLowerCase(Locale.getDefault());
        ScriptManager sm = mCtx.engine.getScriptManager();
        for (Script script : sm.getAllScriptMatching(Script.FLAG_ALL)) {
            if (script.name != null && script.name.toLowerCase(Locale.getDefault()).contains(q)) {
                out.add(new CommandSuggestion(".script " + script.name, script.name, "Run script"));
                if (out.size() >= 12) break;
            }
        }
        return out;
    }

    private CommandResult runScript(String query) {
        if (query.isEmpty()) return CommandResult.error("Usage: .script <name>");
        String q = query.toLowerCase(Locale.getDefault());
        ScriptManager sm = mCtx.engine.getScriptManager();
        Script best = null;
        for (Script script : sm.getAllScriptMatching(Script.FLAG_ALL)) {
            if (script.name == null) continue;
            String name = script.name.toLowerCase(Locale.getDefault());
            if (name.equals(q)) { best = script; break; }
            if (best == null && name.contains(q)) best = script;
        }
        if (best == null) return CommandResult.error("No script matching \"" + query + "\"");
        mCtx.runAction(GlobalConfig.RUN_SCRIPT, String.valueOf(best.id));
        return CommandResult.ok();
    }

    // ----- .folder -----

    /** Folders sitting on dashboard desktops, by visible label. */
    private List<Folder> findFolders(String query) {
        ArrayList<Folder> out = new ArrayList<>();
        String q = query == null ? "" : query.toLowerCase(Locale.getDefault());
        try {
            for (int pageId : mCtx.engine.getPageManager().getAllPagesIds()) {
                if (!Page.isDashboard(pageId)) continue;
                Page page = mCtx.engine.getOrLoadPage(pageId);
                if (page == null || page.items == null) continue;
                for (Item item : page.items) {
                    if (item instanceof Folder) {
                        Folder f = (Folder) item;
                        String label = f.getLabel();
                        if (label != null && label.toLowerCase(Locale.getDefault()).contains(q)) {
                            out.add(f);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // defensive: never let layout scanning break the palette
        }
        return out;
    }

    private List<CommandSuggestion> suggestFolders(String query) {
        ArrayList<CommandSuggestion> out = new ArrayList<>();
        for (Folder f : findFolders(query)) {
            out.add(new CommandSuggestion(".folder " + f.getLabel(), f.getLabel(), "Open folder"));
            if (out.size() >= 12) break;
        }
        return out;
    }

    private CommandResult openFolder(String query) {
        if (query.isEmpty()) return CommandResult.error("Usage: .folder <name>");
        List<Folder> folders = findFolders(query);
        if (folders.isEmpty()) return CommandResult.error("No folder matching \"" + query + "\"");
        Folder best = folders.get(0);
        String q = query.toLowerCase(Locale.getDefault());
        for (Folder f : folders) {
            if (f.getLabel() != null && f.getLabel().toLowerCase(Locale.getDefault()).equals(q)) {
                best = f;
                break;
            }
        }
        mCtx.runAction(GlobalConfig.OPEN_FOLDER, String.valueOf(best.getFolderPageId()));
        return CommandResult.ok();
    }
}
