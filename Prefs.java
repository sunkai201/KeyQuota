package com.chaos.keyquota;

import android.content.Context;
import android.content.SharedPreferences;

/** 极简配置：当前皮肤 + 剩余配额。 */
public final class Prefs {

    private static final String FILE = "chaos_keyboard_prefs";
    private static final String KEY_THEME = "theme_id";

    private Prefs() {
    }

    private static SharedPreferences sp(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static String themeId(Context context) {
        return sp(context).getString(KEY_THEME, Theme.PIXEL.id);
    }

    public static void setThemeId(Context context, String id) {
        sp(context).edit().putString(KEY_THEME, id).apply();
    }

}
