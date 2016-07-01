package org.taitascioredev.fractal;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

/**
 * Created by roberto on 26/05/15.
 */
public class MyClickableSpan extends ClickableSpan {

    private int color;

    public MyClickableSpan(int color) { this.color = color; }

    public MyClickableSpan() { this(-1); }

    @Override
    public void onClick(View widget) { }

    @Override
    public void updateDrawState(TextPaint ds) {
        /*
        if (color == -1)
            ds.setColor(ds.linkColor);
        else
            ds.setColor(color);
            */
        if (color != -1)
            ds.setColor(color);
        ds.setUnderlineText(false);
    }
}
