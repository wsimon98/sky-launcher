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

package net.pierrox.lightning_launcher.sky.drawer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import net.pierrox.lightning_launcher.sky.SkyTheme;

/**
 * Alphabet scrubber for the app drawer (idea seen in launchers like Niagara;
 * implemented from scratch for Sky Launcher). A slim A–Z ribbon: tap or drag
 * to jump to apps starting with that letter.
 */
public class SkyScrubberView extends View {

    public interface OnLetterListener {
        void onLetter(char letter);
    }

    private static final String LETTERS = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final Paint mPaint;
    private final OnLetterListener mListener;
    private int mActiveIndex = -1;

    public SkyScrubberView(Context context, OnLetterListener listener) {
        super(context);
        mListener = listener;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(11 * context.getResources().getDisplayMetrics().scaledDensity);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int h = getHeight(), w = getWidth();
        if (h == 0) return;
        float step = h / (float) LETTERS.length();
        int accent = SkyTheme.accent(getContext());
        for (int i = 0; i < LETTERS.length(); i++) {
            boolean active = i == mActiveIndex;
            mPaint.setColor(active ? accent : 0xAAFFFFFF);
            mPaint.setShadowLayer(2f, 0, 0, Color.BLACK);
            mPaint.setFakeBoldText(active);
            float y = step * i + step * 0.8f;
            canvas.drawText(String.valueOf(LETTERS.charAt(i)), w / 2f, y, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                int h = getHeight();
                if (h == 0) return true;
                int index = (int) (event.getY() / h * LETTERS.length());
                if (index < 0) index = 0;
                if (index >= LETTERS.length()) index = LETTERS.length() - 1;
                if (index != mActiveIndex) {
                    mActiveIndex = index;
                    invalidate();
                    if (mListener != null) mListener.onLetter(LETTERS.charAt(index));
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mActiveIndex = -1;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }
}
