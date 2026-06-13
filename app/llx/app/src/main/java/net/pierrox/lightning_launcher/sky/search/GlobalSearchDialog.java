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

import android.app.Dialog;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.pierrox.lightning_launcher.sky.SkyContext;

import java.util.ArrayList;
import java.util.List;

/**
 * GlobalSearch: local search over launcher content (optional Sky module,
 * inspired by the behavior of search-first launchers such as Kvaesitso —
 * implemented from scratch for Sky Launcher; no code from GPL projects).
 *
 * Local providers only. No internet, no telemetry, nothing leaves the device.
 */
public class GlobalSearchDialog {
    private static final int MAX_PER_PROVIDER = 6;

    public static void show(final SkyContext ctx) {
        final List<SearchProvider> providers = SkyProviders.createDefault(ctx);
        final Dialog dialog = new Dialog(ctx.activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(ctx.activity);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (8 * ctx.activity.getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        final EditText input = new EditText(ctx.activity);
        input.setHint("Search apps, items, scripts…");
        input.setSingleLine(true);
        input.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_GO);
        root.addView(input, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final ListView list = new ListView(ctx.activity);
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        final List<SearchResult> results = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                ctx.activity, android.R.layout.simple_list_item_2, android.R.id.text1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                SearchResult r = results.get(position);
                ((TextView) v.findViewById(android.R.id.text1)).setText(r.title);
                TextView t2 = v.findViewById(android.R.id.text2);
                t2.setText(r.kind);
                t2.setTextColor(Color.GRAY);
                return v;
            }
        };
        list.setAdapter(adapter);

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                results.clear();
                if (!query.isEmpty()) {
                    for (SearchProvider provider : providers) {
                        try {
                            results.addAll(provider.search(query, MAX_PER_PROVIDER));
                        } catch (Exception e) {
                            // one broken provider must not break the others
                        }
                    }
                }
                adapter.clear();
                for (SearchResult r : results) adapter.add(r.title);
                adapter.notifyDataSetChanged();
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchResult r = results.get(position);
                dialog.dismiss();
                r.open.run();
            }
        });

        // pressing Go/Enter launches the top result
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (!results.isEmpty()) {
                    SearchResult r = results.get(0);
                    dialog.dismiss();
                    r.open.run();
                    return true;
                }
                return false;
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
        input.requestFocus();
    }
}
