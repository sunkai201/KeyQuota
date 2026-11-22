package com.chaos.keyquota;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;

/** 一些到处都要用的小工具。 */
final class Ui {

    private Ui() {
    }

    static int dp(Context context, float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics()));
    }

    static GradientDrawable box(int fill, int strokeColor, float strokeDp, float radiusDp,
                                Context context) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(fill);
        d.setCornerRadius(dp(context, radiusDp));
        if (strokeDp > 0) {
            d.setStroke(Math.max(1, dp(context, strokeDp)), strokeColor);
        }
        return d;
    }

    static Typeface mono() {
        return Typeface.MONOSPACE;
    }

    static Typeface heavy() {
        return Typeface.create("sans-serif-black", Typeface.BOLD);
    }

    static Typeface bold() {
        return Typeface.create("sans-serif", Typeface.BOLD);
    }

    static int alpha(int color, float alpha) {
        return Color.argb((int) (255 * Math.max(0f, Math.min(1f, alpha))),
                Color.red(color), Color.green(color), Color.blue(color));
    }
}
