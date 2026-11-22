package com.chaos.keyquota;

/**
 * 四套皮肤，来自最初的四份独立页面：
 * 初版（纸张 + 广告）、黑白简约风、朋克风（像素街机）、动漫风（漫画贴纸）。
 */
public final class Theme {

    public final String id;
    public final String name;
    public final String tag;
    public final String intro;

    public final int bg;        // 应用外底色
    public final int shell;     // 面板底色
    public final int panel;     // 卡片底色
    public final int ink;       // 主文字
    public final int muted;     // 次文字
    public final int accent;    // 强调色
    public final int keyFill;
    public final int keyInk;
    public final int keyStroke;
    public final int ctlFill;
    public final int ctlInk;
    public final int sendFill;
    public final int sendInk;
    public final int dead;
    public final int deadInk;

    public final int radius;    // dp
    public final int shadow;    // dp，硬阴影偏移
    public final boolean mono;  // 等宽字体
    public final boolean black; // 极粗黑体
    public final boolean italic;

    private Theme(String id, String name, String tag, String intro,
                  int bg, int shell, int panel, int ink, int muted, int accent,
                  int keyFill, int keyInk, int keyStroke,
                  int ctlFill, int ctlInk, int sendFill, int sendInk,
                  int dead, int deadInk, int radius, int shadow,
                  boolean mono, boolean black, boolean italic) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.intro = intro;
        this.bg = bg;
        this.shell = shell;
        this.panel = panel;
        this.ink = ink;
        this.muted = muted;
        this.accent = accent;
        this.keyFill = keyFill;
        this.keyInk = keyInk;
        this.keyStroke = keyStroke;
        this.ctlFill = ctlFill;
        this.ctlInk = ctlInk;
        this.sendFill = sendFill;
        this.sendInk = sendInk;
        this.dead = dead;
        this.deadInk = deadInk;
        this.radius = radius;
        this.shadow = shadow;
        this.mono = mono;
        this.black = black;
        this.italic = italic;
    }

    /** 初版：纸张质感、蓝色强调、108 次额度、模拟广告换弹。 */
    public static final Theme V1 = new Theme(
            "v1", "Odd Arcade · 奇怪风", "Arcade · 街机",
            "Catch a drifting letter before it bounces away. The small number tells you how many taps you have left.\n追着字母敲。键帽上的小数字，是这个字符还剩的次数。",
            0xFFF6F4ED, 0xFFFFFFFF, 0xFFF6F4ED, 0xFF202521, 0xFF65685E, 0xFF344DDE,
            0xFFFFFFFF, 0xFF202521, 0xFFC6C8BB,
            0xFFF6F4ED, 0xFF202521, 0xFF344DDE, 0xFFFFFFFF,
            0xFFD9DBD0, 0xFF8B8E83, 6, 0, true, false, false);

    /** 黑白简约风：米白仪表盘、等宽数字、橙色动作键。 */
    public static final Theme INDUSTRIAL = new Theme(
            "industrial", "Monochrome · 黑白简约风", "LCD · 仪表",
            "Read the counter on each moving key. Spend the last use and that character needs a reload.\n看准移动键帽上的余量。用掉最后一次，这个字符就得换弹。",
            0xFFC5C2B9, 0xFFE8E4D9, 0xFFEDEAE1, 0xFF292923, 0xFF5C5B52, 0xFFED7625,
            0xFFFAF7E8, 0xFF302E28, 0xFFB0AA98,
            0xFFECE9DC, 0xFF272721, 0xFFF27D18, 0xFF2E2618,
            0xFFB9B7AD, 0xFF706E65, 7, 0, false, false, false);

    /** 朋克风：紫黑霓虹、像素键帽、硬阴影。 */
    public static final Theme PIXEL = new Theme(
            "pixel", "Pixel Punk · 朋克风", "Pixels · 像素",
            "Track the red pixel keys across the dark grid. Three matching characters at the end unlock a whole-word delete.\n盯住暗色网格上的红色像素键。草稿末尾出现三连，就能整词删除。",
            0xFF0B0611, 0xFF170A20, 0xFF2D102D, 0xFFFFEBB2, 0xFFC8ABC8, 0xFFFFD26A,
            0xFFB52446, 0xFFFFEDAB, 0xFFFFCA94,
            0xFF2D55A7, 0xFFFFEAB2, 0xFF3867BF, 0xFFFFF1B9,
            0xFF483346, 0xFFA492A7, 3, 3, true, false, false);

    /** 动漫风：高饱和撞色、粗黑描边、贴纸歪斜。 */
    public static final Theme COMIC = new Theme(
            "comic", "Pop Comic · 动漫风", "Pop · 漫画",
            "Tap the bouncing sticker you want. Miss a letter and it may take three matching taps to erase the word.\n点中弹跳的字母贴纸。拼错后，要让词尾凑出三连才能删词。",
            0xFF222126, 0xFF2678C8, 0xFFFFF5D9, 0xFF201B20, 0xFF514443, 0xFFE3493A,
            0xFFFFDB36, 0xFF151519, 0xFF1E191B,
            0xFF44A5EB, 0xFF211719, 0xFFFF5A4E, 0xFF211719,
            0xFFC5BBAA, 0xFF6D645D, 8, 2, false, true, true);

    public String page;
    public static final Theme RAINBOW = new Theme(
            "rainbow", "Prism Pulse · 彩虹爆闪风", "RGB · 八色",
            "Follow the glowing rims across the dark grid. A spent key loses its colour until you reload it.\n沿着暗色网格里的彩光追键。字符耗尽会褪色，换弹后重新亮起。",
            0xFF0A0A0E,0xFF101017,0xFF191920,0xFFFFFFFF,0xFFE6DCBE,0xFFFFD700,
            0xFFFF2D2D,0xFF101017,0xFFFFD700,0xFF3B82F6,0xFFFFFFFF,0xFF22C55E,0xFF04140A,
            0xFF232330,0xFF6A6A7A,3,2,true,true,false);
    public static Theme[] ALL = { V1, INDUSTRIAL, PIXEL, COMIC, RAINBOW };
    private static boolean loaded;
    public static synchronized void load(android.content.Context c) {
        if(loaded)return;
        try(java.io.InputStream in=c.getAssets().open("keyboards/versions.json");
            java.util.Scanner scanner=new java.util.Scanner(in,"UTF-8").useDelimiter("\\A")){
            org.json.JSONArray list=new org.json.JSONArray(scanner.next());
            java.util.ArrayList<Theme> result=new java.util.ArrayList<>();
            for(int i=0;i<list.length();i++){
                org.json.JSONObject o=list.getJSONObject(i);
                String page=o.getString("page"), id=o.getString("id");
                if(!page.matches("[a-z0-9_-]+\\.html") || !id.matches("[a-z0-9_-]+"))continue;
                Theme p=byId(o.optString("palette","pixel"));
                Theme t=new Theme(id,o.getString("name"),o.getString("tag"),o.getString("intro"),p.bg,p.shell,p.panel,p.ink,p.muted,p.accent,p.keyFill,p.keyInk,p.keyStroke,p.ctlFill,p.ctlInk,p.sendFill,p.sendInk,p.dead,p.deadInk,p.radius,p.shadow,p.mono,p.black,p.italic);
                t.page=page;result.add(t);
            }
            if(!result.isEmpty())ALL=result.toArray(new Theme[0]);
            loaded=true;
        } catch(Exception e){for(Theme t:ALL)t.page=t.id+".html";}
    }

    public static Theme byId(String id) {
        if (id != null) {
            for (Theme t : ALL) {
                if (t.id.equals(id)) {
                    return t;
                }
            }
        }
        return PIXEL;
    }

    public static Theme next(Theme current) {
        for (int i = 0; i < ALL.length; i++) {
            if (ALL[i].id.equals(current.id)) {
                return ALL[(i + 1) % ALL.length];
            }
        }
        return ALL[0];
    }
}
