package com.chaos.keyquota;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class RulesActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        int ink = 0xffffd26a, paper = 0xff191320;
        getWindow().setStatusBarColor(paper);
        getWindow().setNavigationBarColor(paper);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setBackgroundColor(paper);
        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(Ui.dp(this,14),Ui.dp(this,4),Ui.dp(this,16),Ui.dp(this,4));
        TextView back = new TextView(this);
        back.setText("‹  Home · 主页");
        back.setContentDescription("Back to home · 返回主页");
        back.setGravity(Gravity.CENTER);
        back.setTextSize(15);
        back.setTypeface(Ui.bold());
        back.setTextColor(ink);
        back.setMinHeight(Ui.dp(this,48));
        back.setPadding(Ui.dp(this,12),0,Ui.dp(this,12),0);
        back.setBackground(Ui.box(0xff251c2c,0xff785d50,1,8,this));
        back.setFocusable(true);
        back.setOnClickListener(v -> finish());
        bar.addView(back);
        TextView brand = new TextView(this);
        brand.setText("KEY QUOTA");
        brand.setTypeface(Ui.mono());
        brand.setLetterSpacing(.14f);
        brand.setTextSize(12);
        brand.setTextColor(ink);
        brand.setGravity(Gravity.END);
        bar.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        column.addView(bar,-1,-2);
        WebView guide = new WebView(this);
        guide.setBackgroundColor(paper);
        guide.getSettings().setBlockNetworkLoads(true);
        guide.getSettings().setAllowContentAccess(false);
        guide.getSettings().setAllowFileAccess(false);
        column.addView(guide,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(column);
        guide.loadUrl("file:///android_asset/guide/rules.html");
    }
}
