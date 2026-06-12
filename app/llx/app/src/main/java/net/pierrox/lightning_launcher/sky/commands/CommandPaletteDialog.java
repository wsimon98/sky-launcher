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

import android.app.Dialog;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.pierrox.lightning_launcher.sky.SkyContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Command Palette: a typed command line for the launcher (optional Sky
 * module, inspired by the behavior of T-UI ConsoleLauncher; implemented from
 * scratch for Sky Launcher).
 */
public class CommandPaletteDialog {

    public static void show(final SkyContext ctx) {
        final CommandRegistry registry = new CommandRegistry(ctx);
        final Dialog dialog = new Dialog(ctx.activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(ctx.activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (8 * ctx.activity.getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        final EditText input = new EditText(ctx.activity);
        input.setHint(": for commands, . for targets");
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_GO);
        root.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final ListView list = new ListView(ctx.activity);
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        final List<CommandSuggestion> suggestions = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                ctx.activity, android.R.layout.simple_list_item_2, android.R.id.text1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                CommandSuggestion cs = suggestions.get(position);
                ((TextView) v.findViewById(android.R.id.text1)).setText(cs.title);
                TextView t2 = v.findViewById(android.R.id.text2);
                t2.setText(cs.description);
                t2.setTextColor(Color.GRAY);
                return v;
            }
        };
        list.setAdapter(adapter);

        final Runnable refresh = new Runnable() {
            @Override
            public void run() {
                suggestions.clear();
                suggestions.addAll(registry.suggest(input.getText().toString()));
                adapter.clear();
                for (CommandSuggestion cs : suggestions) adapter.add(cs.title);
                adapter.notifyDataSetChanged();
            }
        };

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { refresh.run(); }
        });

        final Runnable execute = new Runnable() {
            @Override
            public void run() {
                String line = input.getText().toString().trim();
                if (line.isEmpty() && !suggestions.isEmpty()) {
                    line = suggestions.get(0).input.trim();
                }
                CommandResult result = registry.execute(line);
                if (result.success) {
                    dialog.dismiss();
                } else if (result.message != null) {
                    Toast.makeText(ctx.activity, result.message, Toast.LENGTH_SHORT).show();
                }
            }
        };

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    execute.run();
                    return true;
                }
                return false;
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CommandSuggestion cs = suggestions.get(position);
                String line = cs.input.trim();
                if (cs.input.endsWith(" ")) {
                    // command expects an argument: fill the input instead of executing
                    input.setText(cs.input);
                    input.setSelection(cs.input.length());
                    return;
                }
                CommandResult result = registry.execute(line);
                if (result.success) {
                    dialog.dismiss();
                } else if (result.message != null) {
                    Toast.makeText(ctx.activity, result.message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.setContentView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Window w = dialog.getWindow();
        if (w != null) {
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (ctx.activity.getResources().getDisplayMetrics().heightPixels * 0.6f));
            w.setGravity(Gravity.TOP);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        refresh.run();
        input.requestFocus();
    }
}
