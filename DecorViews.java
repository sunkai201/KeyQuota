package com.chaos.keyquota;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;

/** 主页背景：斜条纹 + 圆点 + 散落的键帽，故意乱一点。 */
class ChaosBackdrop extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Theme theme = Theme.PIXEL;

    ChaosBackdrop(Context context) {
        super(context);
        textPaint.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
    }

    void setTheme(Theme t) {
        this.theme = t;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, 0, w, h,
                theme.bg, mix(theme.bg, Color.BLACK, 0.45f), Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);

        // 斜条纹
        canvas.save();
        canvas.rotate(-24, w * 0.5f, h * 0.4f);
        int[] accents = {
                Theme.COMIC.keyFill, Theme.PIXEL.accent, Theme.INDUSTRIAL.accent, Theme.V1.accent
        };
        float stripe = h * 0.32f;
        for (int i = 0; i < 6; i++) {
            paint.setColor(withAlpha(accents[i % accents.length], 0.07f));
            canvas.drawRect(-w, h * 0.5f + i * stripe, w * 2f, h * 0.5f + i * stripe + stripe * 0.4f, paint);
        }
        canvas.restore();

        // 圆点
        paint.setColor(withAlpha(theme.muted, 0.28f));
        float step = 18f * getResources().getDisplayMetrics().density;
        for (float y = step; y < h; y += step) {
            for (float x = step; x < w; x += step) {
                canvas.drawCircle(x, y, 1.1f, paint);
            }
        }

        // 散落键帽
        float d = getResources().getDisplayMetrics().density;
        float[][] caps = {
                {w * 0.08f, h * 0.18f, 46f, -8f, 0},
                {w * 0.86f, h * 0.10f, 38f, 12f, 1},
                {w * 0.78f, h * 0.46f, 54f, -5f, 2},
                {w * 0.16f, h * 0.62f, 42f, 9f, 3},
        };
        Theme[] palettes = Theme.ALL;
        for (int i = 0; i < caps.length; i++) {
            float cx = caps[i][0];
            float cy = caps[i][1];
            float size = caps[i][2] * d * 0.6f;
            float rot = caps[i][3];
            Theme p = palettes[(int) caps[i][4]];
            canvas.save();
            canvas.rotate(rot, cx, cy);
            paint.setColor(withAlpha(p.keyFill, 0.16f));
            RectF r = new RectF(cx - size * 0.7f, cy - size * 0.7f, cx + size * 0.7f, cy + size * 0.7f);
            canvas.drawRoundRect(r, size * 0.18f, size * 0.18f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.4f * d);
            paint.setColor(withAlpha(p.keyStroke == 0 ? p.accent : p.keyStroke, 0.30f));
            canvas.drawRoundRect(r, size * 0.18f, size * 0.18f, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.restore();
        }

        // 巨大水印
        textPaint.setColor(withAlpha(theme.accent, 0.06f));
        textPaint.setTextSize(150f * d);
        canvas.drawText("KQ", w * 0.52f, h * 0.88f, textPaint);
        textPaint.setTextSize(46f * d);
        canvas.drawText("108", w * 0.04f, h * 0.20f, textPaint);
    }

    private static int withAlpha(int color, float alpha) {
        return Color.argb((int) (255 * Math.max(0f, Math.min(1f, alpha))),
                Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int mix(int a, int b, float ratio) {
        float r = Color.red(a) + (Color.red(b) - Color.red(a)) * ratio;
        float g = Color.green(a) + (Color.green(b) - Color.green(a)) * ratio;
        float bl = Color.blue(a) + (Color.blue(b) - Color.blue(a)) * ratio;
        return Color.rgb((int) r, (int) g, (int) bl);
    }
}

/** 大标题：四个字四种皮肤，歪歪扭扭地拼在一起。 */
class ChaosTitle extends View {

    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String text = "KEY QUOTA";
    private final int[] colors = {
            Theme.COMIC.keyFill, Theme.PIXEL.keyFill, Theme.INDUSTRIAL.accent, Theme.V1.accent
    };
    private final float[] tilts = { -6f, 4f, -3f, 5f };

    ChaosTitle(Context context) {
        super(context);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(6f * context.getResources().getDisplayMetrics().density);
        stroke.setColor(0xFF120B18);
        fill.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        stroke.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        shadow.setColor(0xFF05030A);
        shadow.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        float d = getResources().getDisplayMetrics().density;
        setMeasuredDimension(width, (int) (86f * d));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float d = getResources().getDisplayMetrics().density;
        float size = Math.min(46f * d, getWidth()/6.6f);
        fill.setTextSize(size);
        stroke.setTextSize(size);
        shadow.setTextSize(size);
        float total = 0;
        for (int i = 0; i < text.length(); i++) {
            total += fill.measureText(text.substring(i, i + 1)) + 2f * d;
        }
        float x = (getWidth() - total) / 2f;
        float base = getHeight() * 0.72f;
        for (int i = 0; i < text.length(); i++) {
            String ch = text.substring(i, i + 1);
            float wch = fill.measureText(ch);
            canvas.save();
            canvas.rotate(tilts[i % tilts.length], x + wch / 2f, base - size * 0.3f);
            shadow.setAlpha(150);
            canvas.drawText(ch, x + 3f * d, base + 4f * d, shadow);
            fill.setColor(colors[i % colors.length]);
            canvas.drawText(ch, x, base, fill);
            canvas.drawText(ch, x, base, stroke);
            canvas.restore();
            x += wch + 2f * d;
        }
    }
}

/** 主页卡片里的小键盘预览。 */
class MiniKeyboardView extends View {
    private static final int[] PRISM_COLORS = {0xFFFF2D2D,0xFFFF7A00,0xFFFFD700,0xFF22C55E,
            0xFF22D3EE,0xFF3B82F6,0xFF8B5CF6,0xFFEC4899};

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Theme theme = Theme.PIXEL;

    MiniKeyboardView(Context context) {
        super(context);
    }

    void setTheme(Theme t) {
        theme = t;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        float d = getResources().getDisplayMetrics().density;
        fill.setColor(theme.shell);
        canvas.drawRoundRect(new RectF(0, 0, w, h), 6f * d, 6f * d, fill);

        String row1 = "1234567890";
        String row2 = "qwertyuiop";
        String row3 = "asdfghjkl";
        float pad = 4f * d;
        float gap = 2.5f * d;
        float rowH = (h - pad * 2 - gap * 2) / 3f;
        float keyW = (w - pad * 2 - gap * 9f) / 10f;
        text.setTextSize(Math.min(rowH * 0.62f, 6f * d));
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(theme.mono ? Typeface.MONOSPACE : Typeface.DEFAULT_BOLD);

        for (int r = 0; r < 3; r++) {
            String row = r == 0 ? row1 : (r == 1 ? row2 : row3);
            float top = pad + r * (rowH + gap);
            for (int i = 0; i < row.length(); i++) {
                float left = pad + i * (keyW + gap);
                RectF key = new RectF(left, top, left + keyW, top + rowH);
                int paletteIndex = (r * 10 + i) % PRISM_COLORS.length;
                boolean prism = Theme.RAINBOW.id.equals(theme.id);
                fill.setColor(prism ? PRISM_COLORS[paletteIndex] : theme.keyFill);
                canvas.drawRoundRect(key, Math.min(3f * d, theme.radius * d * 0.5f),
                        Math.min(3f * d, theme.radius * d * 0.5f), fill);
                line.setStyle(Paint.Style.STROKE);
                line.setStrokeWidth(0.7f * d);
                line.setColor(prism ? PRISM_COLORS[(paletteIndex + 3) % PRISM_COLORS.length] : theme.keyStroke);
                canvas.drawRoundRect(key, Math.min(3f * d, theme.radius * d * 0.5f),
                        Math.min(3f * d, theme.radius * d * 0.5f), line);
                text.setColor(theme.keyInk);
                String label = String.valueOf(row.charAt(i));
                if (theme.mono) {
                    label = label.toUpperCase();
                }
                canvas.drawText(label, key.centerX(), key.centerY() + text.getTextSize() * 0.35f, text);
            }
        }
    }
}
