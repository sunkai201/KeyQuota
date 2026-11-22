package com.chaos.keyquota;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Build;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import org.json.JSONObject;

public final class ChaosImeService extends InputMethodService {
    private FrameLayout panel;
    private Session page;
    private Inventory inventory;
    private long binding;
    private boolean accepting, external, restoreRotation;
    private String editor = "";

    @Override public void onCreate() {
        super.onCreate();
        Theme.load(this);
        inventory = new Inventory(this);
    }
    @Override public boolean onEvaluateFullscreenMode() { return false; }
    @Override public View onCreateInputView() {
        panel = new FrameLayout(this) {
            @Override protected void onMeasure(int w, int h) {
                int heightDp=getResources().getConfiguration().screenHeightDp;
                if(heightDp<=0) heightDp=650;
                boolean landscape=getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE;
                int dp=Math.min(landscape?310:510,Math.round(heightDp*(landscape?.64f:.56f)));
                if (!landscape && page != null && "white".equals(page.theme.id)) {
                    dp = Math.min(dp, Math.max(250, Math.min(340, Math.round(heightDp * .43f))));
                }
                int height = Ui.dp(ChaosImeService.this, dp);
                if (MeasureSpec.getMode(h) != MeasureSpec.UNSPECIFIED)
                    height = Math.min(height, MeasureSpec.getSize(h));
                super.onMeasure(w, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }
        };
        return panel;
    }
    private String identity(EditorInfo info) {
        return info==null?"":info.packageName+"|"+info.fieldId+"|"+info.fieldName+"|"+info.inputType;
    }
    @Override public void onStartInput(EditorInfo info, boolean restarting) {
        super.onStartInput(info,restarting);
        String next=identity(info);
        if(!restarting || !next.equals(editor)) binding++;
        if(page!=null && !external && !restoreRotation && (!next.equals(page.editor)
                || !restarting && page.ended && (info==null || info.fieldId<=0 || page.secret))) destroyPage();
        editor=next; accepting=false;
    }
    @Override public void onStartInputView(EditorInfo info,boolean restarting) {
        super.onStartInputView(info,restarting);
        accepting=true;
        if(panel==null)return;
        if(page!=null && external && !editor.equals(page.editor)) { showReturn();return; }
        String themeId=Prefs.themeId(this);
        if(page!=null && (!editor.equals(page.editor) || !themeId.equals(page.theme.id) && !external && !restoreRotation)) destroyPage();
        if(page==null) openPage(Theme.byId(themeId),info);
        else {
            attach(page.root); page.ended=false; page.view.onResume();
            page.setKeyboardVisible(true);
            page.view.evaluateJavascript("window.kqActivate && window.kqActivate()",null);
        }
        restoreRotation=false;
        external=false;
    }
    @Override public void onConfigurationChanged(Configuration next) {
        restoreRotation=true;
        super.onConfigurationChanged(next);
    }
    @Override public void onFinishInputView(boolean finishing) {
        accepting=false;
        if(page!=null){page.setKeyboardVisible(false);page.view.onPause();}
        super.onFinishInputView(finishing);
    }
    @Override public void onFinishInput() {
        accepting=false;
        if(page!=null)page.ended=true;
        super.onFinishInput();
    }
    @Override public void onDestroy() { accepting=false; destroyPage();super.onDestroy(); }
    private void attach(View child) {
        if(child.getParent()==panel)return;
        if(child.getParent() instanceof ViewGroup)((ViewGroup)child.getParent()).removeView(child);
        panel.removeAllViews();panel.addView(child);
    }
    private void destroyPage() {
        Session old=page;page=null;
        if(old==null)return;
        if(old.root.getParent() instanceof ViewGroup)((ViewGroup)old.root.getParent()).removeView(old.root);
        old.root.removeView(old.view);old.view.stopLoading();old.view.destroy();external=false;
    }
    private void chooseIme() {
        InputMethodManager im=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if(im!=null)im.showInputMethodPicker();
    }
    private TextView label(String text,int fill,int ink) {
        TextView v=new TextView(this);v.setText(text);v.setGravity(Gravity.CENTER);v.setTextSize(12);v.setTextColor(ink);
        v.setBackgroundColor(fill);v.setPadding(Ui.dp(this,8),Ui.dp(this,3),Ui.dp(this,8),Ui.dp(this,3));return v;
    }
    private void showReturn() {
        LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setGravity(Gravity.CENTER);
        box.setBackgroundColor(Color.WHITE);
        box.addView(label("Your ad is still open. Return to the original app.\n广告仍在，返回原应用即可继续。",Color.WHITE,Color.BLACK));
        Button fresh=new Button(this);fresh.setText("Start here · 在此重新开始");
        fresh.setOnClickListener(v->{destroyPage();openPage(Theme.byId(Prefs.themeId(this)),getCurrentInputEditorInfo());});box.addView(fresh);
        Button other=new Button(this);other.setText("Switch keyboard · 切换输入法");other.setOnClickListener(v->chooseIme());box.addView(other);attach(box);
    }
    private void openPage(Theme skin,EditorInfo info) {
        if(panel==null)return;
        Session s=new Session(skin,editor,info);page=s;attach(s.root);
        s.view.loadUrl(s.url);
    }
    private final class Session {
        final Theme theme;final String editor,url;final boolean secret;
        final LinearLayout root,bar;final WebView view; final TextView stock;
        final DeliveryGate delivery=new DeliveryGate();
        boolean ended=false;
        void setKeyboardVisible(boolean visible) {
            view.evaluateJavascript("window.kqSetKeyboardVisible && window.kqSetKeyboardVisible("+visible+")", null);
        }
        Session(Theme t,String owner,EditorInfo info) {
            theme=t;editor=owner;url="file:///android_asset/keyboards/"+t.page;
            int it=info==null?0:info.inputType;
            int cls=it&InputType.TYPE_MASK_CLASS,v=it&InputType.TYPE_MASK_VARIATION;
            secret=(cls==InputType.TYPE_CLASS_TEXT && (v==InputType.TYPE_TEXT_VARIATION_PASSWORD || v==InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD || v==InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)) || (cls==InputType.TYPE_CLASS_NUMBER && v==InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            root=new LinearLayout(ChaosImeService.this);root.setOrientation(1);root.setBackgroundColor(t.shell);
            bar=new LinearLayout(ChaosImeService.this);
            TextView next=label(t.name+"  ▸",t.shell,t.ink);
            next.setSingleLine(true);next.setEllipsize(android.text.TextUtils.TruncateAt.END);
            next.setOnClickListener(a->{
                final long current=binding;
                viewCheckIdle(this,()->{if(page!=this || !accepting || binding!=current)return;Theme to=Theme.next(t);Prefs.setThemeId(ChaosImeService.this,to.id);destroyPage();openPage(to,getCurrentInputEditorInfo());});
            });
            bar.addView(next,new LinearLayout.LayoutParams(0,-1,1));
            stock=label("",t.shell,t.ink);
            if(!"mystery".equals(t.id)){stock.setText("Uses · 余 "+inventory.total());bar.addView(stock);}
            TextView other=label("Keyboards · 切换",t.panel,t.ink);other.setOnClickListener(a->chooseIme());bar.addView(other);
            root.addView(bar,-1,Ui.dp(ChaosImeService.this,32));
            view=new WebView(ChaosImeService.this);
            WebSettings settings=view.getSettings();
            settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(false);settings.setAllowContentAccess(false);
            settings.setAllowFileAccess(false);settings.setAllowFileAccessFromFileURLs(false);settings.setAllowUniversalAccessFromFileURLs(false);
            settings.setBlockNetworkLoads(true);settings.setMediaPlaybackRequiresUserGesture(false);
            settings.setTextZoom(100);settings.setSupportMultipleWindows(false);
            view.setBackgroundColor(t.shell);view.setVerticalScrollBarEnabled(false);
            view.setWebViewClient(new WebViewClient(){
                @Override public void onPageFinished(WebView w, String target) {
                    if (page==Session.this && url.equals(target)) setKeyboardVisible(accepting);
                }
                @Override public boolean shouldOverrideUrlLoading(WebView w,WebResourceRequest req) { return true; }
                @Override public boolean shouldOverrideUrlLoading(WebView w,String target){ return true; }
                @Override public boolean onRenderProcessGone(WebView w,RenderProcessGoneDetail detail) {
                    if(page==Session.this) {
                        destroyPage();
                        if(accepting && panel!=null)panel.post(()->{if(page==null && accepting)openPage(theme,getCurrentInputEditorInfo());});
                    }
                    return true;
                }
            });
            view.setWebChromeClient(new WebChromeClient(){
                @Override public boolean onJsPrompt(WebView w,String origin,String msg,String value,JsPromptResult result) {
                    String reply="";
                    if(page==Session.this && url.equals(origin) && msg.startsWith("KQ/"))try{
                        reply=handle(Session.this,msg.substring(3),value);
                    }catch(Exception ignored){ reply=""; }
                    result.confirm(reply);return true;
                }
            });
            root.addView(view,new LinearLayout.LayoutParams(-1,0,1));
        }
    }
    private void viewCheckIdle(Session s,Runnable ok) {
        s.view.evaluateJavascript("window.kqIsIdle && window.kqIsIdle()",r->{
            if("true".equals(r))ok.run();
            else Toast.makeText(this,"Finish this draft first · 请先处理当前草稿",Toast.LENGTH_SHORT).show();
        });
    }
    private String handle(Session s,String op,String data) throws Exception {
        if("wallet".equals(op)){s.stock.setText("Uses · 余 "+inventory.total());return inventory.read(s.theme.id);}
        if("private".equals(op))return s.secret?"1":"0";
        if("save".equals(op)){inventory.write(s.theme.id,new JSONObject(data));s.stock.setText("Uses · 余 "+inventory.total());return "1";}
        if(!accepting || !s.editor.equals(editor) || !s.editor.equals(identity(getCurrentInputEditorInfo())))return "";
        if("copy".equals(op)){
            if(s.secret || data.isEmpty())return "";
            android.content.ClipboardManager cb=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            if(cb==null)return "";
            cb.setPrimaryClip(android.content.ClipData.newPlainText("Key Quota",data));return "1";
        }
        if("link".equals(op)){
            if(!("https://x.com/i/flow/login".equals(data) || "https://warthunder.com/zh/game/about/".equals(data) || "https://bb.qq.com/".equals(data)))return "";
            external=true;
            try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(data)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return "1";}
            catch(android.content.ActivityNotFoundException e){external=false;return "";}
        }
        if(!"deliver".equals(op))return "";
        JSONObject m=new JSONObject(data);
        long seq=m.getLong("seq");String text=m.getString("text");
        return s.delivery.fill(seq,text,getCurrentInputConnection())?"1":"";
    }
}
