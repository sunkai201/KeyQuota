package com.chaos.keyquota;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.ArrayList;

/** A real Android editor. It receives exactly the same IME transactions as a third-party app. */
public final class DemoActivity extends Activity {
    private final ArrayList<String> messages=new ArrayList<>();
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved); Theme.load(this);
        Theme t=Theme.byId(Prefs.themeId(this));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(t.panel);
        int p=Ui.dp(this,12);root.setPadding(p,p,p,p);
        LinearLayout head=new LinearLayout(this);root.addView(head);
        TextView back=new TextView(this);back.setText("‹  Home · 主页");back.setPadding(0,p,p,p);back.setTextColor(t.ink);back.setOnClickListener(v->finish());head.addView(back);
        TextView title=new TextView(this);title.setText("Demo chat · 示例会话");title.setTextSize(18);title.setTextColor(t.ink);head.addView(title);
        ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(1);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView empty=new TextView(this);empty.setText("Tap Send after the dial fills your message.\n判定通过后，点应用的发送键。");empty.setTextColor(t.muted);list.addView(empty);
        EditText field=new EditText(this);field.setId(0x1020041);field.setSingleLine(true);field.setTextColor(t.ink);field.setHintTextColor(t.muted);
        field.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        field.setImeOptions(EditorInfo.IME_ACTION_SEND);field.setHint("Message · 消息");LinearLayout composer=new LinearLayout(this);root.addView(composer);
        composer.addView(field,new LinearLayout.LayoutParams(0,-2,1));
        Button send=new Button(this);send.setText("Send · 发送");composer.addView(send);
        java.util.function.Consumer<String> show=text->{empty.setVisibility(android.view.View.GONE);TextView bubble=new TextView(this);bubble.setText(text);
            bubble.setTextColor(t.ink);bubble.setBackground(Ui.box(t.shell,t.accent,1,8,this));bubble.setPadding(p,p,p,p);bubble.setTextSize(16);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);lp.gravity=Gravity.END;lp.bottomMargin=p/2;list.addView(bubble,lp);
            scroll.post(()->scroll.fullScroll(android.view.View.FOCUS_DOWN));};
        if(saved!=null && saved.getStringArrayList("sent")!=null){messages.addAll(saved.getStringArrayList("sent"));for(String m:messages)show.accept(m);}
        Runnable publish=()->{
            String text=field.getText().toString();if(!text.trim().isEmpty()){messages.add(text);show.accept(text);field.setText("");}
        };
        send.setOnClickListener(v->publish.run());
        field.setOnEditorActionListener((v,action,e)->{
            if(action!=EditorInfo.IME_ACTION_SEND)return false;
            publish.run();return true;
        });
        setContentView(root);field.requestFocus();field.postDelayed(()->{if(!isFinishing())((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(field,InputMethodManager.SHOW_IMPLICIT);},180);
    }
    @Override public void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putStringArrayList("sent",messages);}
}
