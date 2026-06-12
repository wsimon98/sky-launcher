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
import android.app.AlertDialog;
import android.content.DialogInterface;

import net.pierrox.lightning_launcher.engine.LightningEngine;

/**
 * First-run mode picker and one-time upgrades, hooked from the Dashboard.
 * Shows once; choosing nothing means Classic LLX.
 */
public class SkySetup {

    private static boolean sShownThisProcess;

    public static void onDashboardResume(final Activity dashboard, final LightningEngine engine) {
        final SkyConfig config = SkyConfig.getInstance(dashboard);

        // one-time gesture modernization for layouts from older versions
        config.ensureModernGestureDefaults(dashboard, engine);

        if (!config.isFirstRun() || sShownThisProcess) {
            return;
        }
        sShownThisProcess = true;

        final String[] labels = {
                "Classic LLX — pure Lightning Launcher, no new modules",
                "Modern Sky — classic canvas + EdgeWheel, Command Palette, GlobalSearch",
                "Minimal — just a clean home screen",
        };
        final String[] modes = {
                SkyConfig.MODE_CLASSIC_LLX,
                SkyConfig.MODE_MODERN_SKY,
                SkyConfig.MODE_MINIMAL,
        };

        try {
            new AlertDialog.Builder(dashboard)
                    .setTitle("Welcome to Sky Launcher")
                    .setItems(labels, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            config.applyMode(modes[which]);
                            config.applyDefaultBindings(engine);
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // dismissing keeps classic behavior and stops the nag
                            config.applyMode(SkyConfig.MODE_CLASSIC_LLX);
                        }
                    })
                    .show();
        } catch (Exception e) {
            // never let the welcome dialog harm launcher startup
        }
    }
}
