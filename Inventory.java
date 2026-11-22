package com.chaos.keyquota;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;

/** Only numeric balances and unfinished reloads are stored. Editor text stays in the active view. */
final class Inventory {
    static final String KEYS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private final SharedPreferences prefs;
    Inventory(Context ctx) { prefs = ctx.getSharedPreferences("keyquota_wallet2", Context.MODE_PRIVATE); }
    private long value(String key) { return prefs.getLong(key, 3L); }
    String read(String skin) throws Exception {
        JSONObject amounts = new JSONObject();
        for (char ch : KEYS.toCharArray()) { String key = String.valueOf(ch); amounts.put(key, value(key)); }
        JSONObject out = new JSONObject().put("keys", amounts);
        String ticket = prefs.getString("reload."+skin, "");
        if (!ticket.isEmpty()) out.put("ticket", new JSONObject(ticket));
        return out.toString();
    }
    boolean write(String skin, JSONObject msg) throws Exception {
        JSONObject deltas = msg.getJSONObject("delta");
        SharedPreferences.Editor edit = prefs.edit();
        for (char ch : KEYS.toCharArray()) {
            String key = String.valueOf(ch);
            long delta = deltas.optLong(key, 0);
            if (delta != 0) edit.putLong(key, Math.max(0L, value(key) + delta));
        }
        JSONObject ticket = msg.optJSONObject("ticket");
        if (ticket == null) edit.remove("reload."+skin); else edit.putString("reload."+skin,ticket.toString());
        edit.apply(); return true;
    }
    long total() { long count=0; for (char c:KEYS.toCharArray()) count+=value(String.valueOf(c)); return count; }
    void reset(){ prefs.edit().clear().apply(); }
}
