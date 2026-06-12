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

import net.pierrox.lightning_launcher.data.EventAction;
import net.pierrox.lightning_launcher.engine.LightningEngine;
import net.pierrox.lightning_launcher.engine.Screen;

/**
 * What a Sky module needs to talk to the launcher: the host activity, the
 * screen (action executor) and the engine (data). Modules never reach into
 * launcher internals beyond this.
 */
public class SkyContext {
    public final Activity activity;
    public final Screen screen;
    public final LightningEngine engine;

    public SkyContext(Activity activity, Screen screen, LightningEngine engine) {
        this.activity = activity;
        this.screen = screen;
        this.engine = engine;
    }

    /** Run any classic LLX action, exactly as if a gesture triggered it. */
    public void runAction(int action, String data) {
        screen.runAction(engine, "SKY", new EventAction(action, data));
    }
}
