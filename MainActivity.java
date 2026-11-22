package com.chaos.keyquota;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 混沌键盘主页：选皮肤 → 启用输入法 → 去别的应用里用。 */
public class MainActivity extends Activity {

    private Theme selected;
    private ChaosBackdrop backdrop;
    private LinearLayout cardColumn;
    private LinearLayout actionColumn;
    private TextView statusChip;
    private TextView quotaChip;
    private final Map<String, FrameLayout> cardMap = new HashMap<String, FrameLayout>();
    private final Map<String, TextView> stampMap = new HashMap<String, TextView>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Theme.load(this);
        selected = Theme.byId(Prefs.themeId(this));

        FrameLayout root = new FrameLayout(this);
        backdrop = new ChaosBackdrop(this);
        backdrop.setTheme(selected);
        root.addView(backdrop, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(false);

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setClipChildren(false);
        column.setPadding(Ui.dp(this, 18), Ui.dp(this, 26), Ui.dp(this, 18), Ui.dp(this, 34));

        column.addView(buildTopBar());
        column.addView(buildTitle());
        column.addView(buildSubtitle());

        cardColumn = new LinearLayout(this);
        cardColumn.setOrientation(LinearLayout.VERTICAL);
        cardColumn.setClipChildren(false);
        column.addView(cardColumn);
        actionColumn = new LinearLayout(this);
        actionColumn.setOrientation(LinearLayout.VERTICAL);
        actionColumn.setPadding(0, Ui.dp(this, 10), 0, 0);
        column.removeView(cardColumn);
        column.addView(actionColumn);
        column.addView(buildSectionHeader());
        column.addView(cardColumn);

        scroll.addView(column);
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        rebuildCards();
        rebuildActions();
        refreshChips();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChips();
    }

    private View buildTopBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);

        TextView brand = text("KEY QUOTA", 11, selected.accent, Ui.mono());
        brand.setLetterSpacing(0.18f);
        brand.setMaxLines(2);
        bar.addView(brand,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));

        statusChip = pill("", 10, 22, 0xFF120B18, 0xFFFFEBB2);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 28));
        sp.rightMargin = Ui.dp(this, 8);
        bar.addView(statusChip, sp);

        quotaChip = pill("", 10, 22, 0xFF120B18, 0xFFFFD26A);
        quotaChip.setTextColor(0xFFFFD26A);
        bar.addView(quotaChip, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, Ui.dp(this, 28)));
        TextView help = pill("?", 23, 40, 0xFF201522, 0xFFFFD26A);
        help.setTypeface(Ui.mono(), Typeface.BOLD);
        help.setContentDescription("Typing guide · 打字规则");
        help.setFocusable(true);
        help.setOnClickListener(v -> startActivity(new Intent(this, RulesActivity.class)));
        LinearLayout.LayoutParams helpLp = new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48));
        helpLp.leftMargin = Ui.dp(this,6);
        bar.addView(help, helpLp);
        return bar;
    }

    private View buildTitle() {
        ChaosTitle title = new ChaosTitle(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 92));
        lp.topMargin = Ui.dp(this, 14);
        title.setLayoutParams(lp);
        return title;
    }

    private View buildSubtitle() {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(this, -4);

        TextView sub = text("Set up Key Quota, then try a message in the demo chat or your own app.\n启用键盘后，可在示例会话或其他应用里试打一条消息。", 13, 0xFFFFEAC0, null);
        sub.setPadding(Ui.dp(this,12),Ui.dp(this,8),Ui.dp(this,12),Ui.dp(this,8));
        sub.setBackground(Ui.box(0xFF211628,0xFF544052,1f,8,this));
        sub.setLineSpacing(Ui.dp(this, 4), 1f);
        wrap.addView(sub, lp);
        return wrap;
    }

    private View buildSectionHeader() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.BOTTOM);
        row.setPadding(0, Ui.dp(this, 22), 0, Ui.dp(this, 12));

        TextView head = text("Choose a keyboard · 选择版本", 18, 0xFFFFEBB2, Ui.heavy());
        row.addView(head);

        View spacer = new View(this);
        row.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));

        TextView hint = text("Tap a card · 点选", 10, 0xFF9E8FA8, null);
        row.addView(hint);
        return row;
    }

    private void rebuildCards() {
        cardColumn.removeAllViews();
        cardMap.clear();
        stampMap.clear();
        for (int i = 0; i < Theme.ALL.length; i++) {
            Theme t = Theme.ALL[i];
            FrameLayout card = buildCard(t, i);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = Ui.dp(this, 18);
            lp.topMargin = Ui.dp(this, 4);
            cardColumn.addView(card, lp);
            cardMap.put(t.id, card);
        }
        applySelectionStyles();
    }

    private FrameLayout buildCard(final Theme t, int index) {
        FrameLayout card = new FrameLayout(this);
        card.setBackground(Ui.box(t.panel, Ui.alpha(t.ink, 0.45f), 1.5f, t.radius + 10, this));
        card.setElevation(Ui.dp(this, 4));
        float[] rotations = { -1.2f, 0.9f, -0.8f, 1.4f };
        card.setRotation(rotations[index % rotations.length]);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(Ui.dp(this, 16), Ui.dp(this, 14), Ui.dp(this, 16), Ui.dp(this, 14));
        card.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 名称 + 标签
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = text(t.name, 21, t.ink, Ui.heavy());
        if (t.italic) {
            name.setTypeface(Typeface.create(Ui.heavy(), Typeface.ITALIC));
        }
        head.addView(name,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        TextView tag = pill(t.tag, 9, 18, t.accent, t.sendInk);
        LinearLayout.LayoutParams tagLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tagLp.leftMargin = Ui.dp(this, 10);
        head.addView(tag, tagLp);
        content.addView(head);

        // 介绍
        String[] lines = t.intro.split("\n", 2);
        TextView intro = text(lines[0], 14, t.ink, null);
        intro.setLineSpacing(Ui.dp(this, 2), 1f);
        intro.setPadding(0, Ui.dp(this, 8), 0, 0);
        content.addView(intro);
        if (lines.length > 1) {
            TextView translation = text(lines[1], 12, t.muted, null);
            translation.setPadding(0, Ui.dp(this, 3), 0, 0);
            content.addView(translation);
        }

        // 键盘预览
        MiniKeyboardView mini = new MiniKeyboardView(this);
        mini.setTheme(t);
        LinearLayout.LayoutParams miniLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 78));
        miniLp.topMargin = Ui.dp(this, 12);
        content.addView(mini, miniLp);

        // 小字说明
        TextView meta = text("Local keyboard · 本地输入法", 9.5f,
                Ui.alpha(t.muted, 0.9f), Ui.mono());
        LinearLayout.LayoutParams metaLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        metaLp.topMargin = Ui.dp(this, 8);
        content.addView(meta, metaLp);

        // 已启用印章
        TextView stamp = pill("Active · 使用中", 9, 20, t.sendFill, t.sendInk);
        stamp.setRotation(-8f);
        FrameLayout.LayoutParams stampLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stampLp.gravity = Gravity.BOTTOM | Gravity.END;
        stampLp.bottomMargin = Ui.dp(this, 8);
        stampLp.rightMargin = Ui.dp(this, 14);
        card.addView(stamp, stampLp);
        stampMap.put(t.id, stamp);

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTheme(t);
            }
        });
        return card;
    }

    private void rebuildActions() {
        actionColumn.removeAllViews();
        TextView enable = actionButton("1   Enable keyboard · 启用输入法", selected.sendFill, selected.sendInk);
        enable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Open system keyboard settings · 请手动打开输入法设置",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        actionColumn.addView(enable);

        TextView pick = actionButton("2   Choose input method · 选择输入法", selected.ctlFill, selected.ctlInk);
        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showInputMethodPicker();
                }
            }
        });
        LinearLayout.LayoutParams pickLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pickLp.topMargin = Ui.dp(this, 10);
        actionColumn.addView(pick, pickLp);

        TextView demo = actionButton("3   Try the demo chat · 进入示例会话", 0xFF120B18, 0xFFFFD26A);
        demo.setBackground(Ui.box(Ui.alpha(0xFFFFD26A, 0.14f), 0xFFFFD26A, 1.2f, 10, this));
        demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DemoActivity.class));
            }
        });
        LinearLayout.LayoutParams demoLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        demoLp.topMargin = Ui.dp(this, 10);
        actionColumn.addView(demo, demoLp);

        TextView reset = text("Reset key balances · 重置字符余量", 11, 0xFF9E8FA8, null);
        reset.setPadding(0, Ui.dp(this, 16), 0, 0);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Inventory(MainActivity.this).reset();
                refreshChips();
                Toast.makeText(MainActivity.this, "Balances reset · 已重置", Toast.LENGTH_SHORT).show();
            }
        });
        actionColumn.addView(reset);
    }

    private void selectTheme(Theme t) {
        if (t.id.equals(selected.id)) {
            Toast.makeText(this, "Already selected · 已选「" + t.name + "」了", Toast.LENGTH_SHORT).show();
            return;
        }
        selected = t;
        Prefs.setThemeId(this, t.id);
        applySelectionStyles();
        rebuildActions();
        backdrop.setTheme(t);
        setChrome();
        Toast.makeText(this, "Selected · 已选 " + t.name, Toast.LENGTH_SHORT).show();
        cardColumn.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
    }

    private void applySelectionStyles() {
        for (Theme t : Theme.ALL) {
            FrameLayout card = cardMap.get(t.id);
            TextView stamp = stampMap.get(t.id);
            if (card == null) {
                continue;
            }
            boolean on = t.id.equals(selected.id);
            card.setBackground(Ui.box(t.panel, on ? t.accent : Ui.alpha(t.ink, 0.35f),
                    on ? 2.4f : 1.4f, t.radius + 10, this));
            card.setElevation(Ui.dp(this, on ? 10 : 4));
            card.setRotation(on ? 0f : card.getRotation());
            if (stamp != null) {
                stamp.setVisibility(on ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void refreshChips() {
        if (statusChip != null) {
            boolean enabled = isImeEnabled();
            statusChip.setText(enabled ? "Enabled · 已启用" : "Off · 未启用");
            statusChip.setTextColor(enabled ? 0xFFB4EF9D : 0xFFFB8793);
        }
        if (quotaChip != null) {
            quotaChip.setText("Keys · 余量 " + new Inventory(this).total());
        }
        setChrome();
    }

    private void setChrome() {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(selected.bg);
            getWindow().setNavigationBarColor(selected.bg);
        }
    }

    private boolean isImeEnabled() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm == null) {
            return false;
        }
        List<InputMethodInfo> list = imm.getEnabledInputMethodList();
        if (list == null) {
            return false;
        }
        for (InputMethodInfo info : list) {
            if (getPackageName().equals(info.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private TextView text(String value, float sp, int color, Typeface tf) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        if (tf != null) {
            tv.setTypeface(tf);
        }
        return tv;
    }

    private TextView pill(String value, float sp, int heightDp, int fill, int ink) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(sp);
        tv.setTextColor(ink);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(Ui.bold());
        tv.setPadding(Ui.dp(this, 10), 0, Ui.dp(this, 10), 0);
        tv.setBackground(Ui.box(fill, Ui.alpha(ink, 0.35f), 1f, 8, this));
        tv.setMinHeight(Ui.dp(this, heightDp));
        return tv;
    }

    private TextView actionButton(String value, int fill, int ink) {
        TextView tv = new TextView(this);
        tv.setText(value);
        tv.setTextSize(15);
        tv.setTextColor(ink);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(Ui.heavy());
        tv.setPadding(Ui.dp(this, 12), Ui.dp(this, 15), Ui.dp(this, 12), Ui.dp(this, 15));
        tv.setBackground(Ui.box(fill, Ui.alpha(ink, 0.45f), 1.2f, 12, this));
        tv.setClickable(true);
        tv.setFocusable(true);
        return tv;
    }
}
